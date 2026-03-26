## 요약(결론)
- `1-F-6`(확정 시 reschedule)은 **확정 요청 DTO에 “변경 희망 시작시간”을 옵션으로 추가**하고, `confirmReservation()`에서 **예약의 `time/startAt`을 갱신한 뒤 점유 블록을 생성/할당**하는 방식으로 구현한다.
- 점유 충돌은 기존 `ReservationTimeBlockWriter.allocateOrThrowOnConflict()`의 예외/롤백 메커니즘을 그대로 사용한다.
- 사용자 요청으로 **TDD(새 테스트 추가) 없이 구현만** 진행하되, 리스크를 줄이기 위해 **관련 기존 테스트/검증 시나리오**는 실행한다.

---

## Context
- 이슈: `#97`
- 대상 요구사항: `docs/issues/#97/04-tdd/plan.md`의 **`1-F-6`**
  - 확정(confirm) 시 점주가 시간을 변경(reschedule)할 수 있어야 함
  - 변경된 시간이 이미 점유면 예외
  - 변경 성공 시 예약의 `startAt` 및 점유 블록도 변경 시간 기준으로 생성되어야 함

현재 서비스 로직:
- `ReservationService.confirmReservation()`은
  - `reservation.confirm(message, durationMinutes)`로 상태를 `CONFIRMED`로 바꾸고
  - `timeBlockGenerator.generate(reservationId, staffId, reservation.getStartAt(), duration)`로 블록을 만든 뒤
  - `reservationTimeBlockWriter.allocateOrThrowOnConflict()`로 점유를 시도한다.

즉, reschedule은 **확정 직전에 `reservation.startAt`을 변경**해주면 자연스럽게 블록 생성 기준이 바뀐다.

---

## Decision
### API/DTO
- `ReservationConfirmRequest`에 **선택 필드** 추가:
  - `LocalTime startTime` (nullable)
  - 의미: “이 예약을 확정하면서 적용할 시작 시간(시간대만)”
  - 날짜 변경은 본 ADR에서는 다루지 않는다(예약의 `date`는 유지).

### Service
- `confirmReservation()`에서:
  1) 권한/소유자 검증(현행 유지)
  2) `request.startTime != null`이면
     - 10분 경계 검증(분 단위/초/나노)
     - `reservation.time = startTime`, `reservation.startAt = LocalDateTime.of(reservation.date, startTime)`로 갱신
  3) `reservation.confirm(message, durationMinutes)` 수행
  4) 갱신된 `reservation.startAt` 기준으로 블록 생성 → 점유(현행 유지)

### 충돌/롤백
- 점유 충돌은 기존과 동일하게 `allocateOrThrowOnConflict()`에서 예외 발생
- 트랜잭션 롤백으로 `Reservation`의 상태/시간 변경도 함께 되돌아가야 한다
  - (기존 통합 테스트가 “충돌 시 PENDING 유지”를 검증하고 있음)

---

## Alternatives (Rejected)
- **A. 별도 reschedule API**(`/reschedule` 후 `/confirm`)  
  - 장점: 상태/책임 분리
  - 단점: 호출/상태 복잡도 증가, 충돌 처리 시나리오가 더 많아짐
- **B. `LocalDateTime startAt`을 DTO로 받기**  
  - 장점: 날짜 변경까지 확장 용이
  - 단점: 현재 요구는 “시간 변경”이 핵심이고, 날짜 변경은 정책/검증이 더 필요

---

## Risks
- `ReservationConfirmRequest` 스키마 변경으로 프론트/클라이언트가 영향을 받을 수 있음
  - 필드는 nullable로 추가하여 **기존 호출은 그대로 동작**하도록 한다.
- reschedule 시간 검증(10분 경계)이 누락되면 데이터 정책이 흐트러질 수 있음

---

## Test strategy (TDD 없이 진행 시)
- 새 테스트를 추가하지 않더라도, 최소한 아래 기존 테스트/시나리오는 실행한다.
  - `ReservationServiceConfirmTimeBlockUnitTest` (블록 생성/할당 흐름)
  - `ReservationServiceConfirmOverlapRollbackIntegrationTest` (충돌 시 롤백)
- 수동 검증 시나리오(로컬):
  - 기존 시간에서 `startTime`을 바꿔 confirm 호출 → 블록 시작 시간이 변경된 시간 기준인지 확인

