# `#134 예약 변경 이력 저장` 작업 로그

작성일: `2026-06-18`  
기준 이슈: `#134`  
관련 브랜치: `feature/#134`

## Entries

### Entry 001

- Date: `2026-06-18 01:00`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `docs/issues/#134/02-analysis/0001-implementation-plan.md`
- What:
  - 예약 변경 이력 저장 이슈의 최소 범위를 정리했다.
- Why:
  - 변경 이력은 수정 API와 별도 저장 책임으로 분리해야 한다.
- Verification:
  - 계획 문서를 작성하고 범위를 확인했다.
- Next:
  - 변경 이력 테이블과 저장 writer를 추가한다.

### Entry 002

- Date: `2026-06-18 01:00`
- Unit: `pre-commit`
- Type: `Behavioral`
- Scope:
  - `src/main/resources/db/migration/V16__add_reservation_change_histories.sql`
  - `src/main/java/com/example/easybooking/reservation/domain/ReservationChangeHistory.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationChangeHistoryRepository.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationChangeHistoryWriter.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `docs/issues/#134/frontend-api-handoff.md`
- What:
  - 예약 수정 시 before/after JSON을 `reservation_change_histories`에 저장하도록 연결했다.
- Why:
  - 변경 이력은 추후 조회/감사 화면의 기반이 된다.
- Verification:
  - 컴파일 검증을 이어서 수행할 예정이다.
- Next:
  - 변경 이력 조회와 수정 자동 메시지는 다음 이슈로 분리한다.

### Entry 003

- Date: `2026-06-18 08:45`
- Unit: `migration-fix`
- Type: `Structural`
- Scope:
  - `src/main/resources/db/migration/V16__add_reservation_change_histories.sql`
- What:
  - `V1`에서 이미 생성하는 `reservation_change_histories`를 `V16`에서 다시 생성하지 않도록 no-op 마이그레이션으로 정리했다.
- Why:
  - `V15`와 같은 중복 테이블 생성 문제가 `V16`에서도 발생할 수 있었다.
- Verification:
  - 로컬 DB `flyway_schema_history`에서 `V16` 성공 기록 확인
- Next:
  - 없음.
