# #97 1-F-5 테스트 설계 — 확정 시 점유 겹침 + Race(유니크 제약) 처리

Created: 2026-02-10  
Issue: `#97`  
Scope: `plan.md` 1-F-5 (확정 시 동일 staff + 동일 시간대 점유 충돌 예외)

---

## 0) 요구사항(SSOT 요약)

`docs/issues/#97/04-tdd/plan.md` 1-F-5:

- 동일 staffId + 동일 시간대(10분 블록 단위)에 이미 점유가 있으면 확정 시 예외
- 관측: 트랜잭션 롤백, 예약 상태 `PENDING` 유지
- 엣지케이스: 다른 staffId면 동일 시간 확정 가능
- 추가: 동시성 레이스(사전조회 통과 후 DB 유니크 제약 위반)도 동일 의미로 처리되어야 함

---

## 1) 중요한 전제(현재 코드 구조로 인해 생기는 테스트 레벨 구분)

### 1.1 단위테스트로 “롤백 후 PENDING 유지”를 직접 검증하기 어려운 이유

서비스 로직이 `reservation.confirm(...)`을 먼저 수행하고 이후 타임블록 저장 과정에서 예외가 발생하면,
단위테스트(순수 자바 객체)에서는 예외가 나더라도 **in-memory 객체의 status는 CONFIRMED로 바뀐 상태**로 남는다.

실제 런타임에서는 `@Transactional`에 의해 DB 반영이 롤백되어 “영속 상태는 PENDING 유지”가 되지만,
이 “DB 관측”은 **통합 테스트에서 검증**하는 편이 정확하다.

결론:
- **단위테스트**: “어떤 예외가 던져지는가 / 어떤 컴포넌트를 호출하는가 / 예외 매핑이 정확한가”에 집중
- **통합테스트**: “트랜잭션 롤백으로 DB 상태(PENDING)가 유지되는가”를 검증

---

## 2) 테스트 구성을 3개 축으로 나누기(권장)

### (A) 사전 조회로 겹침 감지(UX 개선 경로)

목표:
- `existsByStaffIdAndBlockStartAtIn(...) == true`면 즉시 `ReservationException(TIME_BLOCK_ALREADY_BOOKED)` 발생
- DB 유니크 예외까지 가지 않는 “빠른 실패” 경로를 보장

권장 테스트 레벨:
- **Writer/Allocator 단위테스트**(가장 적합): Repository exists를 true로 만들고 도메인 예외가 던져지는지 검증
- (옵션) Service 단위테스트: writer가 예외 던지면 그대로 전파되는지만 확인

테스트 시나리오:
- 입력: staffId=1, blocks=[14:00, 14:10, …]
- 스텁: repository.existsByStaffIdAndBlockStartAtIn(1, blockStarts) -> true
- 기대: `ReservationException` with errorCode=`TIME_BLOCK_ALREADY_BOOKED`

### (B) 레이스로 DB 유니크 제약 위반(최종 안전장치 경로)

목표:
- 사전 조회는 통과(exists=false)했지만,
- 저장(saveAllAndFlush)에서 `DataIntegrityViolationException`이 터지면
- constraint 식별(`uq_rtb_staff_block_start_at`) 후 동일한 도메인 예외로 번역

권장 테스트 레벨:
- **Writer/Allocator 단위테스트**가 핵심

테스트 시나리오:
- 입력: staffId=1, blocks=[14:00, 14:10, …]
- 스텁: exists -> false
- 스텁: saveAllAndFlush -> throw DataIntegrityViolationException(cause chain includes constraintName=uq_rtb_staff_block_start_at)
- 기대: `ReservationException` with errorCode=`TIME_BLOCK_ALREADY_BOOKED`

추가 보호 테스트(오매핑 방지):
- saveAllAndFlush -> throw DataIntegrityViolationException(다른 constraintName 또는 constraintName null + 다른 종류)
- 기대: 예외를 TIME_BLOCK으로 매핑하지 않고 그대로 throw (또는 정책에 맞게)

### (C) 롤백 관측(예약 상태 PENDING 유지)

목표:
- 예외 발생 시 DB에 저장된 예약 상태는 `PENDING`으로 유지

권장 테스트 레벨:
- **통합 테스트**(SpringBootTest 또는 slice + real DB(H2))로 확인

통합 시나리오(개념):
- given: reservation(PENDING) persist
- given: reservation_time_blocks에 (staff=1, 14:00) 이미 존재하도록 seed
- when: confirmReservation 호출
- then: 예외 발생
- and: reservation 다시 조회 -> status == PENDING

주의:
- 서비스가 확정 과정에서 reservation 엔티티를 먼저 변경하더라도,
  트랜잭션 롤백이 일어나면 DB 상태는 유지되어야 함(이 테스트가 그 사실을 보장)

---

## 3) 테스트 네이밍(프로젝트 규칙 준수)

- TDD 단계(Red/Green/Refactor) 표기 금지
- “행위/조건/결과” 중심으로 명명

예시:
- `allocateOrThrowOnConflict_whenOverlapExists_throwsTimeBlockAlreadyBooked`
- `allocateOrThrowOnConflict_whenUniqueConstraintViolated_throwsTimeBlockAlreadyBooked`
- `confirmReservation_whenTimeBlockAlreadyOccupied_rollsBackAndKeepsPendingStatus` (통합)

---

## 4) 구현 가이드(테스트가 가리키는 설계 포인트)

### 4.1 “겹침이면 ReservationException, 레이스면 DataIntegrityViolationException”을 외부로 노출하지 않는다

레이스가 나면 DB는 내부적으로 `DataIntegrityViolationException`을 던지지만,
도메인 의미는 동일(이미 점유됨)이므로 **최종적으로는 동일한 도메인 예외(409)** 로 통일한다.

즉,
- 사전조회 충돌 -> `ReservationException(TIME_BLOCK_ALREADY_BOOKED)`
- DB 유니크 위반(레이스) -> 내부 예외를 constraint 식별 후 `ReservationException(TIME_BLOCK_ALREADY_BOOKED)`로 번역

### 4.2 flush를 “예외 번역 범위 안으로” 끌어오기

`saveAll`만 호출하면 커밋 직전 flush에서 예외가 터져 매핑이 누락될 수 있으므로,
Writer/Allocator에서 `saveAllAndFlush`(또는 saveAll + flush)로 예외 발생 시점을 통제한다.

---

## 5) 최소 테스트 세트(우선순위)

1. (B) 레이스/유니크 제약 위반 → 도메인 예외 번역 (Writer 단위) **최우선**
2. (A) 사전 조회 충돌 → 빠른 도메인 예외 (Writer 단위)
3. (C) 롤백 후 PENDING 유지 (통합) — plan.md의 “관측”을 실제로 보장하는 테스트

