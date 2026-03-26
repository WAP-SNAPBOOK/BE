## #97 설계 가이드(Object): 확정(Confirm) 시 점유 블록 생성 책임 분리

- 날짜: 2026-02-09
- 브랜치: `feature/jiseob/#97-reservation-erd-v2-follow-up`
- 관련 plan: `docs/issues/#97/04-tdd/plan.md` (특히 `1-F-4`, `1-F-5`)
- 관련 ADR: `docs/issues/#97/03-adr/0001-adr-occupancy-granularity-10min.md`

---

## 1) 논리 흐름(요약)

- “예약 확정” 유스케이스를 완결하려면 **상태 변경(CONFIRMED)** 뿐 아니라 **점유(ReservationTimeBlock) 생성**까지 같은 트랜잭션에서 수행되어야 한다.
- Object(오브젝트) 관점에서, 서비스는 “절차”를 직접 수행하기보다 **협력 객체에게 메시지를 보내 책임을 위임**한다.
- 따라서 `ReservationService`는 오케스트레이션만 수행하고, “10분 단위 점유 블록 생성 규칙”은 별도 객체(`ReservationTimeBlockAllocator`)의 책임으로 분리한다.

---

## 2) 어떻게 변경할지 (설계/접근 방식)

### A. 객체/책임/협력(오브젝트식 표현)

- **`ReservationService` (Application Service)**
  - 책임: 유스케이스 오케스트레이션(권한검증 → 상태전이 → 점유생성 트리거 → 이벤트/응답)
  - 협력: `Reservation`(상태 전이), `ReservationTimeBlockAllocator`(점유 생성), `ReservationTimeBlockWriter`(저장)

- **`Reservation` (Domain Entity)**
  - 책임: “확정 가능한 상태인지”를 스스로 검증하고 상태를 변경한다(현재 `confirm()`가 수행).

- **`ReservationTimeBlockAllocator` (Domain Service/Policy Object)**
  - 책임: “점유 블록 생성 정책(10분 단위)”을 알고, 필요한 정보를 내부에서 조회해 `ReservationTimeBlock` 리스트를 만든다.
  - 메시지 예: `allocateFor(reservationId, reservation, durationMinutes)` 또는 `allocateFor(reservationId, reservation)`

- **`ReservationTimeBlockWriter` (Persistence)**
  - 책임: 만들어진 블록 리스트를 저장한다.

### B. 메시지 중심(“Tell, Don’t Ask”)으로 바꾸는 포인트

- 서비스가 `reservation.getStartAt()` 등으로 값을 꺼내 계산(ask)하기보다는,
  - 서비스는 “점유를 만들어라”라고 **알로케이터에게 tell**한다.
  - 알로케이터가 필요한 정보를 `reservation`에서 조회해 정책을 적용한다.

### C. TDD 관점에서의 최소 단위 테스트 전략

- **정책 테스트(순수 단위)**: 알로케이터가 duration=60이면 6개(14:00~14:50)를 생성하는지 검증
- **유스케이스 테스트(서비스 단위)**: confirm 시 writer가 saveAll로 “생성된 블록”을 저장 요청하는지 검증
  - 이후 `1-F-5`에서 UNIQUE 위반/예외 변환이 추가되면 이 서비스 단위 테스트가 근거가 된다.

---

## 3) 변경 근거 (이유, 장단점)

### 왜 SRP/오브젝트 관점에서 분리하는가

- `ReservationService.confirmReservation()`은 이미 권한검사/상태전이/이벤트발행/응답구성 책임을 가진다.
- 여기에 “10분 단위 블록 생성 규칙”을 직접 적으면, 서비스가 **정책 변경(10분→5분, 라운딩 규칙 추가, 리스케줄 정책 변경)** 까지 떠안는다.
- 정책은 별도 객체에 “응집”시키고, 서비스는 협력 객체를 통해 유스케이스를 완결하는 편이 책임 분리가 선명하다.

### 장점

- 정책 변경이 서비스로 전파되지 않음(변경 격리)
- 정책 단위 테스트가 쉬움(순수 로직)
- `1-F-5`, `1-F-6`로 확장 시 재사용 가능

### 단점/주의

- 클래스 1개가 추가되어 구조가 늘어난다.
- “정책 객체가 엔티티에 접근한다”는 형태가 싫다면, 이후 `ReservationOccupancy` 같은 값 객체(First-class collection)로 진화시키는 선택지가 있다.

---

## 4) 코드 수정 예시 (diff 또는 예시 코드)

> 아래는 “예시”이며 실제 적용은 TDD `go`로 진행한다.

### A. 알로케이터(정책 객체) 예시

```java
// 예시: ReservationTimeBlockAllocator
// 책임: durationMinutes를 10분 단위 블록으로 쪼개 ReservationTimeBlock 목록을 생성
public List<ReservationTimeBlock> allocateFor(Long reservationId, Reservation reservation) {
    LocalDateTime startAt = (reservation.getStartAt() != null)
            ? reservation.getStartAt()
            : LocalDateTime.of(reservation.getDate(), reservation.getTime());

    Long staffId = reservation.getStaffId(); // 1-F-3 이후에는 null이 아니도록 보정될 예정
    int durationMinutes = reservation.getDurationMinutes(); // confirm 이후 셋팅됨

    int count = durationMinutes / 10;
    return IntStream.range(0, count)
            .mapToObj(i -> ReservationTimeBlock.create(reservationId, staffId, startAt.plusMinutes(i * 10L)))
            .toList();
}
```

### B. 서비스는 “협력 객체에게 메시지”만 보냄

```diff
 reservation.confirm(request.getMessage(), request.getDurationMinutes());

+ List<ReservationTimeBlock> blocks = reservationTimeBlockAllocator.allocateFor(reservationId, reservation);
+ reservationTimeBlockWriter.saveAll(blocks);
```

---

## 5) 변경 후 예상 결과 (동작, 성능, 영향 범위)

- 확정 시 `reservation_time_blocks`에 10분 단위 row가 생성된다. (예: 60분 → 6개)
- 이후 `1-F-5`에서 동일 staff+동일 blockStartAt 중복 확정 시 DB UNIQUE 위반이 발생하며, 이를 “도메인 예외”로 매핑하는 작업이 자연스럽다.
- 영향 범위
  - `ReservationService.confirmReservation()`에 “알로케이터 호출 + 저장” 추가
  - 신규 클래스(정책 객체) + 신규 테스트(단위/서비스 중 택1) 추가

