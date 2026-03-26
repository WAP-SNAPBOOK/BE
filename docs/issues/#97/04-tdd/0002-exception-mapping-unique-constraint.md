# #97 — (동시성 레이스 포함) 유니크 제약 위반을 “정확히” 도메인 예외로 매핑하기

## 이 문서의 목적

이 문서는 Snapbook(easybooking) 백엔드에서 **예약 확정(confirm)** 시 생성·저장되는 `reservation_time_blocks`가 **동일 Staff + 동일 10분 블록 시작 시각**에 대해 중복 저장될 경우,

- **항상**(동시성 레이스 상황 포함) 동일한 도메인 예외로 변환되어
- API가 **일관된 에러 코드/메시지(409 Conflict)** 를 응답하도록

“예외 매핑을 `flush 포함 + constraint 식별` 수준으로 탄탄하게” 만드는 가이드를 제공한다.

> 목표 독자: 이 맥락을 모르는 개발자(신규 투입자)도 읽고 “무슨 문제가 있고, 무엇을 어떻게 바꾸려는지” 이해할 수 있도록 작성한다.

---

## 배경(현재 구조 요약)

### 1) 예약 확정 시 동작 흐름

예약 확정은 `ReservationService.confirmReservation()`에서 다음 과정을 수행한다.

- `Reservation.confirm(...)`으로 예약 상태를 `CONFIRMED`로 변경
- `TimeBlockGenerator.generate(...)`로 10분 단위 블록들을 생성
- `ReservationTimeBlockWriter.saveAll(blocks)`로 블록들을 DB에 저장

관련 코드(발췌):

```225:270:src/main/java/com/example/easybooking/reservation/service/ReservationService.java
    @Transactional
    public ReservationStatusResponse confirmReservation(Long reservationId, Long ownerUserId,
                                                        ReservationConfirmRequest request) {
        // ...
        reservation.confirm(request.getMessage(), request.getDurationMinutes());
        // ...
        List<ReservationTimeBlock> blocks = timeBlockGenerator.generate(
                reservationId,
                reservation.getStaffId(),
                reservation.getStartAt(),
                request.getDurationMinutes()
        );

        reservationTimeBlockWriter.saveAll(blocks);
        // ...
    }
```

### 2) 중복 점유를 막는 “최종 장치”: DB 유니크 제약

`ReservationTimeBlock` 엔티티는 아래 유니크 제약을 가진다.

- 유니크 키: `(staff_id, block_start_at)`
- 이름: `uq_rtb_staff_block_start_at`

```16:52:src/main/java/com/example/easybooking/reservation/domain/ReservationTimeBlock.java
@Table(
        name = "reservation_time_blocks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_rtb_staff_block_start_at", columnNames = {"staff_id", "block_start_at"})
        }
)
public class ReservationTimeBlock {
    // ...
}
```

즉, 어떤 경로로든 동일 staff + 동일 블록 시각이 DB에 이미 존재하면 **저장 자체가 실패**한다.

---

## 문제 정의: “사전 조회를 해도 레이스는 남고, 레이스는 DB 예외로 터진다”

### 동시성 레이스 시나리오(자주 발생할 수 있는 패턴)

1. A 트랜잭션: 사전 조회로 “충돌 없음” 확인
2. B 트랜잭션: 동일 staff + 동일 블록을 먼저 저장(커밋)
3. A 트랜잭션: 뒤늦게 저장을 시도 → **유니크 제약 위반** 발생

즉 **사전 조회는 UX를 개선**하지만, 동시성 레이스를 0으로 만들지 못한다.

따라서 레이스 상황에서도 API가 안정적으로 409(“이미 점유됨”)을 응답하려면,
레이스가 유발하는 **DB 유니크 제약 위반 예외를 “정확히” 도메인 예외로 매핑**해야 한다.

---

## 왜 “예외를 정확히 못 잡는” 문제가 생기나?

다음 2가지가 핵심 원인이다.

### 1) flush 타이밍(예외가 try/catch 바깥에서 터질 수 있음)

JPA는 `saveAll()` 호출 시점에 SQL이 즉시 나가지 않고,
**트랜잭션 커밋 직전 flush**에서 INSERT가 실행되며 그때 예외가 터질 수 있다.

이 경우 `saveAll()` 주변에만 try/catch를 두면 예외가 그 블록 밖으로 튀어 나가서,
서비스 레벨에서 의도한 매핑이 실패할 수 있다.

### 2) DataIntegrityViolationException은 “중복키”만 의미하지 않음(오매핑 위험)

Spring의 `DataIntegrityViolationException`은 유니크 위반뿐 아니라
FK/NOT NULL/체크 제약 등 다양한 무결성 오류를 포괄한다.

따라서 “DataIntegrityViolationException이면 무조건 TIME_SLOT_ALREADY_BOOKED”로 매핑하면
다른 종류의 데이터 오류까지 예약 충돌로 **오매핑**할 수 있다.

---

## 목표(정확한 매핑의 정의)

아래 조건을 만족해야 한다.

- **언제 터지든**(saveAll 시점이든 commit flush 시점이든) 예외를 잡아낼 수 있어야 한다.
- 잡아낸 예외가
  - “(staff_id, block_start_at) 유니크 제약 위반”인 경우에만
  - `ReservationException(ReservationErrorCode.TIME_SLOT_ALREADY_BOOKED)`로 변환되어야 한다.
- 그 외 무결성 오류(FK, NOT NULL 등)는 TIME_SLOT으로 숨기지 않고 적절히 드러나야 한다(최소한 500으로 뭉개지지 않도록).

---

## 제안 전략(핵심): “flush 포함 + constraint 식별”

### 큰 그림

예외 매핑을 탄탄하게 하려면 아래 2가지를 동시에 만족해야 한다.

1. **예외가 try/catch 안에서 터지도록** flush 타이밍을 통제한다.
2. 터진 예외가 “우리 유니크 제약(`uq_rtb_staff_block_start_at`)”인지 **식별**한 뒤에만 도메인 예외로 매핑한다.

---

## 구현 가이드(예시 코드)

> 아래는 “어떻게 구현할지”를 설명하기 위한 예시 코드이다. 실제 적용은 TDD `plan.md` 흐름과 승인(동의/go)에 맞춰 진행한다.

### A안) 서비스 레벨에서 저장을 “saveAll + flush”로 감싸기(권장)

#### 1) flush를 try/catch 안으로 끌어오기

가능한 구현 선택지:

- `JpaRepository.saveAllAndFlush(...)`를 사용한다.
- 또는 `saveAll(...)` 후 `flush()`를 명시 호출한다.

중요한 건 “예외를 이 블록 안에서 발생시키는 것”이다.

#### 2) constraint 식별 후 매핑

식별 우선순위(권장):

1) Hibernate `org.hibernate.exception.ConstraintViolationException`의 `constraintName`이
   `uq_rtb_staff_block_start_at`인지 확인  
2) (대안) SQLState / vendor error code로 “duplicate key” 여부 판단  
3) (최후의 수단) 메시지 파싱(환경별로 깨지기 쉬움 → 가급적 지양)

서비스 레벨 예시(의사코드):

```java
try {
    reservationTimeBlockRepository.saveAllAndFlush(blocks); // 또는 saveAll + flush
} catch (DataIntegrityViolationException e) {
    if (isUniqueConstraintViolation(e, "uq_rtb_staff_block_start_at")) {
        throw new ReservationException(ReservationErrorCode.TIME_SLOT_ALREADY_BOOKED);
    }
    throw e; // 다른 무결성 오류는 그대로
}
```

> 포인트: “레이스가 터지면 어차피 DB 예외가 최종적으로 발생한다” → 그래서 이 예외를 **반드시** 잡아 도메인 예외로 일관되게 변환해야 한다.

---

### B안) 전역 예외 처리(GlobalExceptionHandler)에 보강(보조 안전망)

현재 `GlobalExceptionHandler`는 `BaseBusinessException`은 처리하지만,
`DataIntegrityViolationException`을 명시적으로 다루지 않는다. 그 결과 예상치 못한 무결성 오류는
`Exception.class` 핸들러로 떨어져 500이 될 수 있다.

전역 핸들러에 “특정 constraint만 409로 매핑”을 추가하면,
서비스 레벨에서 놓치는 케이스(예: 예상치 못한 flush 타이밍, 다른 경로에서의 저장)도 안전망이 된다.

> 단, 전역에서 constraint 식별 로직을 너무 광범위하게 적용하면 오매핑 위험이 있으므로 “constraint name 기반”으로 제한하는 것이 중요하다.

---

## 장단점(왜 이 전략이 필요한가?)

### 장점

- **동시성 레이스가 발생해도** API 응답이 일관됨(항상 409 + 동일 에러코드)
- 예외가 발생하는 시점(saveAll vs commit flush)에 덜 민감
- “duplicate key”와 “그 외 무결성 오류”를 구분해 오매핑을 줄임

### 단점/주의

- constraint 식별 로직이 환경(DB/드라이버)에 따라 다를 수 있음 → “constraint name 기반”을 1순위로 두는 이유
- 배치 insert(여러 블록 저장)에서는 “어느 블록이 충돌인지” 정보가 제한적일 수 있음  
  - 운영 편의를 위해서는 사전 조회(충돌 블록 목록 로깅)와 함께 사용하면 좋다.

---

## 기대 결과(변경 후 동작)

### 정상 케이스

- 동일 staff + 동일 블록에 점유가 없으면 확정 성공
- `reservation_time_blocks`가 정상 저장됨

### 충돌 케이스(사전 조회로 잡히는 경우)

- 저장 전에 “이미 점유됨”을 감지
- `ReservationException(TIME_SLOT_ALREADY_BOOKED)`로 즉시 종료

### 동시성 레이스 케이스(사전 조회를 통과했지만 저장에서 터지는 경우)

- DB 유니크 제약 위반이 발생하더라도
- `flush 포함 + constraint 식별` 매핑으로 인해
- **항상** `ReservationException(TIME_SLOT_ALREADY_BOOKED)`로 변환되어 409 응답

---

## 테스트 전략(권장)

TDD 관점에서 최소한 아래를 커버한다.

- **레이스 시나리오 흉내**: 저장 시점에 “유니크 제약 위반 예외”가 발생한다고 가정했을 때,
  - 우리 constraint면 `TIME_SLOT_ALREADY_BOOKED`로 매핑되는지
  - 다른 제약이면 오매핑하지 않는지
- **flush 타이밍**: `saveAll`이 아니라 `flush`에서 예외가 터지는 케이스도 잡히는지

> 단위 테스트에서는 Repository/Writer를 mock으로 두고 예외 체인을 구성해 분기 로직을 검증할 수 있다.
> 통합 테스트에서는 실제 DB(H2/MySQL)에서 유니크 위반을 발생시키고 응답을 검증하는 방향도 가능하다.

---

## 적용 체크리스트(요약)

- [ ] 저장 경로에서 예외가 “try/catch 안에서” 발생하도록 **saveAllAndFlush 또는 flush 호출**을 포함한다.
- [ ] `DataIntegrityViolationException`에서 “우리 constraint(`uq_rtb_staff_block_start_at`)인지” **식별**한다.
- [ ] 식별 성공 시에만 `ReservationException(TIME_SLOT_ALREADY_BOOKED)`로 매핑한다.
- [ ] 서비스 레벨 매핑 외에, 필요하면 전역 예외 처리에 **보조 안전망**을 둔다.
- [ ] 레이스 케이스를 포함한 테스트로 “항상 409 + 동일 에러코드”를 보장한다.

