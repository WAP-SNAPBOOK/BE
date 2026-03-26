## Summary
- **목표/문제**: 예약 확정(Confirm) 과정에서 “소요시간(durationMinutes)”을 기반으로 **시간 점유(occupancy)를 데이터로 강제**하고, 중복 확정을 **409(CONFLICT)** 로 명확히 반환하며, 입력 검증 실패도 **일관된 ErrorResponse**로 응답한다.
- **선택한 방식**:
  - 확정 시 `ReservationTimeBlock`(10분 단위 블록)을 생성/저장하고, `staff_id + block_start_at` 유니크 제약(또는 동등한 충돌 감지)을 통해 **동시성/중복 확정**을 차단
  - DB 제약 충돌을 애플리케이션 예외로 매핑해 **도메인 에러코드/HTTP 409**로 노출
  - `durationMinutes` 및 시간 정책(10분/30분 등)을 DTO/서비스 레벨에서 검증하고, 검증 오류는 `ErrorResponse`로 통합

---

## Changes
- **확정 시 점유 블록 할당(예약 충돌 방지)**: `confirmReservation()`에서 duration 기반으로 10분 블록을 생성하고, 충돌 시 예외/롤백으로 예약이 확정되지 않도록 보장
- **검증/에러 응답 체계 정리**: `durationMinutes` 등 입력 검증 실패를 `ErrorResponse`로 일관되게 반환하고, 점유 충돌을 409로 매핑
- **도메인/매핑 안정화 + 테스트 보강**: `ReservationTimeBlock` 매핑/제약 중복 제거 및 단위/통합 테스트로 회귀 방지

---

## Test plan
- **TDD(plan.md)**: `docs/issues/#97/04-tdd/plan.md`
- **확인한 테스트(체크리스트)**:
  - [ ] `./gradlew test --tests com.example.easybooking.reservation.service.ReservationServiceConfirmTimeBlockUnitTest`
  - [ ] `./gradlew test --tests com.example.easybooking.reservation.service.ReservationServiceConfirmOverlapRollbackIntegrationTest`
  - [ ] `./gradlew test` (전체 회귀)
- **엣지 케이스/회귀 포인트**:
  - [ ] 동일 staff + 동일 시간대 확정 충돌 시 409 + 트랜잭션 롤백(PENDING 유지)
  - [ ] durationMinutes 정책(10분 단위) 위반 시 validation error 응답
  - [ ] (선택) reschedule(확정 시 시간 변경) 도입 시, 변경된 시간 기준으로 블록 생성/충돌 검증

---

## Risks & Rollback
- **위험 요소**
  - 점유 블록(유니크 제약) 추가로 인해, 기존 데이터/동시성 경로에서 예외가 더 자주 발생할 수 있음(기대 동작이지만 운영 관측 필요)
  - 에러 매핑 변경으로 클라이언트가 특정 에러코드/HTTP status에 의존 중이면 영향 가능
- **롤백/완화 전략**
  - 점유 블록 생성/할당 로직을 feature flag로 감싸거나(가능 시), 예외 매핑을 단계적으로 적용
  - DB 제약/인덱스 적용은 마이그레이션 단위로 분리해 롤백 가능성 확보
  - 장애 시: 점유 블록 생성 비활성화(또는 예외를 더 보수적으로 처리) 후 원복

---

## Docs
- 이슈/분석: `docs/issues/#97/02-analysis/0001-root-cause-and-options.md`
- ADR: `docs/issues/#97/03-adr/0001-adr-occupancy-granularity-10min.md`
- TDD 계획(SSOT): `docs/issues/#97/04-tdd/plan.md`
- 관련 테스트 설계 메모: `docs/issues/#97/04-tdd/0003-tests-confirm-overlap-and-race.md`
- Green 설계: `docs/issues/#97/05-green-design/`

---

## Commit history (현재 브랜치 주요 커밋)
- `feat(reservation): allocate time blocks on confirm and map overlap to 409`
- `feat(reservation): persist durationMinutes on confirm`
- `feat(reservation): validate confirm durationMinutes in 10-minute steps`
- `feat(errors): return ErrorResponse for validation failures`
- `refactor(reservation): remove duplicate unique constraint from ReservationTimeBlock mapping`
- `test(reservation): align integration/unit tests with 10-minute time policy`

