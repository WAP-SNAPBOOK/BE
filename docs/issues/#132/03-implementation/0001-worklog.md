# `#132 예약 상태 이력 저장` 작업 로그

작성일: `2026-06-18`  
기준 이슈: `#132`  
관련 브랜치: `feature/#132`

## Entries

### Entry 001

- Date: `2026-06-18 01:00`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `docs/issues/#132/02-analysis/0001-implementation-plan.md`
- What:
  - 상태 이력 저장 이슈의 최소 범위를 정리했다.
- Why:
  - 상태 이력과 변경 이력은 분리해야 스키마/서비스 변경 범위가 과도해지지 않는다.
- Verification:
  - 계획 문서를 작성하고 범위를 확인했다.
- Next:
  - 상태 이력 테이블과 엔티티를 추가한다.

### Entry 002

- Date: `2026-06-18 00:53`
- Unit: `pre-commit`
- Type: `Behavioral`
- Scope:
  - `src/main/resources/db/migration/V15__add_reservation_status_histories.sql`
  - `src/main/java/com/example/easybooking/reservation/domain/ReservationStatusHistory.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationStatusHistoryRepository.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationStatusHistoryWriter.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- What:
  - 예약 확정/거절/취소 시 상태 이력을 기록하도록 연결했다.
  - `reservation_status_histories` 엔티티와 저장 writer를 추가했다.
- Why:
  - 상태 전이를 감사 로그로 남겨야 사후 추적이 가능하다.
- Verification:
  - `./gradlew compileJava`
- Next:
  - 변경 이력(`reservation_change_histories`)과 취소 메타데이터를 별도 이슈로 분리한다.

### Entry 003

- Date: `2026-06-18 08:45`
- Unit: `migration-fix`
- Type: `Structural`
- Scope:
  - `src/main/resources/db/migration/V15__add_reservation_status_histories.sql`
- What:
  - `V1`에서 이미 생성하는 `reservation_status_histories`를 `V15`에서 다시 생성하지 않도록 no-op 마이그레이션으로 정리했다.
- Why:
  - 중복 `CREATE TABLE` 때문에 Flyway가 `V15`에서 실패했다.
- Verification:
  - 로컬 DB `flyway_schema_history`에서 `V15` 성공 기록 확인
- Next:
  - 없음.
