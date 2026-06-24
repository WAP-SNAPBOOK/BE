# `#135 예약 자동 메시지 본문 저장` 작업 로그

작성일: `2026-06-18`
기준 이슈: `#135`
관련 브랜치: `feature/#135`

## Entries

### Entry 001

- Date: `2026-06-18 01:00`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `docs/issues/#135/02-analysis/0001-implementation-plan.md`
- What:
  - 자동 메시지 본문 저장 이슈의 최소 범위를 정리했다.
- Why:
  - 메시지 본문과 이벤트 타입만 남는 상태를 정리해야 채팅 UI가 실제 변경 내용을 보여줄 수 있다.
- Verification:
  - 계획 문서를 작성하고 범위를 확인했다.
- Next:
  - 예약 이벤트와 시스템 메시지 writer를 본문 저장 방식으로 확장한다.

### Entry 002

- Date: `2026-06-18 01:00`
- Unit: `pre-commit`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/chat/domain/MessageType.java`
  - `src/main/java/com/example/easybooking/chat/domain/Message.java`
  - `src/main/java/com/example/easybooking/chat/SystemMessageWriter.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationEvent.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationChatEventListener.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `docs/issues/#135/frontend-api-handoff.md`
- What:
  - 예약 이벤트에 본문을 실어 고객 채팅방 시스템 메시지로 저장하도록 바꿨다.
  - 예약 수정은 변경 요약 본문을 발행한다.
- Why:
  - 예약 상태 변화가 채팅방에서 문장으로 보여야 고객이 바로 이해할 수 있다.
- Verification:
  - `./gradlew compileJava`
- Next:
  - 메시지 카피 조정과 추가 알림 정책은 후속 이슈로 분리한다.

### Entry 003

- Date: `2026-06-22 22:00:47 +09:00`
- Unit: `structured-message-foundation`
- Type: `Structural`
- Scope:
  - `src/main/java/com/example/easybooking/chat/domain/ReservationChangeSnapshot.java`
  - `src/main/java/com/example/easybooking/chat/domain/Message.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/*ChangeResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/MessageResponse.java`
  - `src/main/java/com/example/easybooking/chat/SystemMessageWriter.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationEvent.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationChatEventListener.java`
  - `src/main/resources/db/migration/V18__add_message_reservation_change_snapshot.sql`
- What:
  - 예약 수정 메시지에 시간, 소요시간, 담당자의 전·후 스냅샷과 점주 전달사항을 저장할 nullable 필드를 추가했다.
  - 조회 API와 WebSocket이 공통 `MessageResponse` 매핑을 사용하도록 구조화 응답 DTO를 연결했다.
- Why:
  - 과거 메시지를 최신 예약 변경 이력과 잘못 결합하지 않고 메시지 생성 시점의 값을 보존하기 위해서다.
- Verification:
  - `./gradlew compileJava`
- Next:
  - 예약 수정 시 실제 변경된 값만 스냅샷에 채운다.

### Entry 004

- Date: `2026-06-22 22:00:47 +09:00`
- Unit: `publish-actual-reservation-changes`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `docs/issues/#135/frontend-api-handoff.md`
- What:
  - 수정 전·후 값을 비교해 실제로 달라진 시간, 소요시간, 담당자만 `reservationChange`에 포함한다.
  - 담당자 변경 시 `staffId`, `staffName` 전·후 스냅샷을 저장한다.
  - 시스템 본문은 `예약 정보가 변경되었습니다.`로 유지하고 점주 전달사항은 `ownerMessage`로 분리했다.
- Why:
  - 프론트엔드가 문자열을 파싱하지 않고 변경 전 → 변경 후 비교 UI를 구성할 수 있어야 한다.
- Verification:
  - `./gradlew clean build` 성공
  - 테스트 소스가 없어 Gradle 테스트 단계는 `NO-SOURCE`
  - 마이그레이션 버전 중복 없음
- Next:
  - 프론트엔드는 `reservationChange`의 존재하는 필드만 렌더링하고 `ownerMessage`를 별도 영역에 표시한다.

### Entry 005

- Date: `2026-06-22 22:25:24 +09:00`
- Unit: `recover-failed-v18-local-migration`
- Type: `Behavioral`
- Scope:
  - 로컬 DB `flyway_schema_history`
  - 로컬 DB `message` 테이블
- What:
  - V18 컬럼 9개는 적용됐지만 Flyway 이력만 실패로 남은 비트랜잭션 DDL 상태를 확인했다.
  - 새 컬럼의 데이터가 0건임을 확인하고 컬럼과 실패 이력을 제거한 뒤 V18을 재실행했다.
- Why:
  - 실패 이력만 repair하면 이미 존재하는 컬럼 때문에 V18이 다시 실패하므로 스키마와 이력을 함께 원복해야 했다.
- Verification:
  - `flyway_schema_history`: V18 `success=1`
  - `message` 테이블: V18 컬럼 9개 존재
  - Flyway 및 JPA 초기화 통과
- Next:
  - 전체 로컬 애플리케이션 기동에는 `AWS_ACCESS_KEY`, `AWS_SECRET_KEY` 등 로컬 환경변수를 별도로 설정한다.

### Entry 006

- Date: `2026-06-22 23:06:00 +09:00`
- Unit: `add-owner-message-change-snapshot`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/chat/domain/Message.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/MessageResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/ValueChangeResponse.java`
  - `src/main/java/com/example/easybooking/chat/SystemMessageWriter.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationEvent.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationChatEventListener.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `src/main/resources/db/migration/V19__add_message_owner_message_before.sql`
- What:
  - 기존 `ownerMessage` 문자열을 유지하면서 `ownerMessageChange.before/after`를 추가했다.
  - 점주 전달사항이 실제로 달라진 경우에만 변경 스냅샷을 저장한다.
- Why:
  - 점주 전달사항도 수정 대상이지만 예약 필드 변경 정보와 분리된 전·후 비교 구조가 필요하다.
- Verification:
  - `./gradlew clean build` 성공
  - 로컬 DB V19 `success=1`
  - `owner_message_before` 컬럼 생성 확인
- Next:
  - 프론트엔드는 `ownerMessageChange`를 예약 필드와 별도 영역에서 비교 표시한다.

### Entry 007

- Date: `2026-06-23 08:43:51 +09:00`
- Unit: `merge-owner-message-into-reservation-change`
- Type: `Structural`
- Scope:
  - `src/main/java/com/example/easybooking/chat/domain/ReservationChangeSnapshot.java`
  - `src/main/java/com/example/easybooking/chat/domain/Message.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/ReservationChangeResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/MessageResponse.java`
  - `src/main/java/com/example/easybooking/chat/SystemMessageWriter.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationEvent.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationChatEventListener.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- What:
  - 최상위 `ownerMessageChange`를 제거하고 전·후 값을 `reservationChange.ownerMessage`로 이동했다.
  - 최상위 `ownerMessage` 문자열은 기존 클라이언트 호환을 위해 유지했다.
- Why:
  - 프론트엔드가 모든 수정 항목을 하나의 변경 전 → 변경 후 비교 구조로 처리할 수 있어야 한다.
- Verification:
  - `./gradlew compileJava` 성공
  - `./gradlew clean build` 성공
  - 테스트 소스가 없어 Gradle 테스트 단계는 `NO-SOURCE`
- Next:
  - 프론트엔드는 `reservationChange.ownerMessage`를 다른 변경 필드와 동일하게 렌더링한다.

### Entry 008

- Date: `2026-06-23 10:09:21 +09:00`
- Unit: `support-reservation-update-date-change`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationUpdateRequest.java`
  - `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `src/main/java/com/example/easybooking/chat/domain/ReservationChangeSnapshot.java`
  - `src/main/java/com/example/easybooking/chat/domain/Message.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/ReservationChangeResponse.java`
  - `src/main/resources/db/migration/V20__add_message_reservation_date_change_snapshot.sql`
- What:
  - 예약 수정 요청에 `date`를 추가했다.
  - 수정 목표 날짜와 시간을 결합한 `targetStartDateTime`으로 점유 블록을 재생성한다.
  - `Reservation.updateConfirmed`가 `date`, `time`, `startAt`을 함께 갱신하도록 변경했다.
  - 날짜 변경 전·후 값을 `reservationChange.date`로 저장·응답한다.
- Why:
  - 날짜만 변경하는 경우에도 기존 시간과 결합해 예약 시작 시점과 점유 블록이 같은 기준을 사용해야 한다.
- Verification:
  - `./gradlew compileJava` 성공
  - `./gradlew clean build` 성공
  - 로컬 DB V20 `success=1`
  - `reservation_date_before`, `reservation_date_after` 컬럼 생성 확인
- Next:
  - 프론트엔드는 날짜 변경 시 `reservationChange.date`를 비교 UI에 표시한다.

### Entry 009

- Date: `2026-06-23 10:31:24 +09:00`
- Unit: `support-reservation-update-menu-change`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationUpdateRequest.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationMenuItemWriter.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationMenuInputValueWriter.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationMenuItemRepository.java`
  - `src/main/java/com/example/easybooking/chat/domain/ReservationChangeSnapshot.java`
  - `src/main/java/com/example/easybooking/chat/domain/Message.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/ReservationChangeResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/MenuSnapshotResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/MenuInputValueSnapshotResponse.java`
  - `src/main/resources/db/migration/V21__add_message_reservation_menu_change_snapshot.sql`
- What:
  - 예약 수정 요청에 `menuSelections`를 추가했다.
  - `menuSelections`가 포함되면 기존 예약 메뉴와 입력값을 삭제하고 새 메뉴 선택값으로 저장한다.
  - 메뉴 변경 전·후 스냅샷을 `reservationChange.menus.before/after`로 저장·응답한다.
- Why:
  - 실제 운영에서 날짜/시간뿐 아니라 선택 메뉴도 변경될 수 있고, 프론트엔드가 동일한 변경 전 → 변경 후 UI로 표시해야 한다.
- Verification:
  - `./gradlew compileJava` 성공
  - `./gradlew clean build` 성공
  - 테스트 소스가 없어 Gradle 테스트 단계는 `NO-SOURCE`
- Next:
  - 로컬 실행 시 V21 마이그레이션 적용을 확인한다.

### Entry 010

- Date: `2026-06-23 11:19:25 +09:00`
- Unit: `support-reservation-confirm-date-change`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationConfirmRequest.java`
  - `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `docs/issues/#135/frontend-api-handoff.md`
- What:
  - 예약 확정 요청에 `date`를 추가했다.
  - 확정 시 날짜와 시간을 최종 목표 시작 시점으로 결합해 `date`, `time`, `startAt`을 함께 갱신한다.
  - 확정 점유 블록도 최종 목표 시작 시점 기준으로 생성한다.
- Why:
  - 점주가 예약 확정 단계에서 고객 요청 가능 일자에 맞춰 날짜와 시간을 조정할 수 있어야 한다.
- Verification:
  - `./gradlew clean build` 성공
  - 테스트 소스가 없어 Gradle 테스트 단계는 `NO-SOURCE`
- Next:
  - 빌드 검증 후 프론트엔드에 확정 요청 계약을 전달한다.
