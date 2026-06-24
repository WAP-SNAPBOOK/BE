# `#135 예약 자동 메시지 본문 저장` 구현 계획

작성일: `2026-06-18`
기준 이슈: `#135`
관련 브랜치: `feature/#135`

## 목적

예약 생성/확정/거절/취소/수정 시 고객 채팅방 시스템 메시지에 실제 본문을 저장한다.

이번 이슈는 자동 메시지 본문 생성만 다룬다. 메시지 문구의 세부 카피 조정과 알림 이력은 후속 이슈로 분리한다.

## 구현 원칙

- 기존 이벤트 기반 채팅 저장 흐름을 재사용한다.
- `ReservationEvent`에 메시지 본문을 포함한다.
- 이벤트 리스너와 시스템 메시지 writer가 본문을 저장한다.
- 테스트는 이번 이슈 범위에서 추가하지 않는다.

## 현재 문제

- 예약 이벤트는 타입만 남고 실제 본문이 비어 있다.
- 채팅방 목록과 메시지 히스토리에서 변경 내용이 읽히지 않는다.
- 수정 이벤트는 아직 고객이 이해할 만한 문장을 남기지 못한다.

## 목표 범위

### 포함

- `ReservationEvent`에 본문 필드 추가
- `RESERVATION_UPDATED` 메시지 타입 추가
- 시스템 메시지 writer가 본문을 저장하도록 수정
- 예약 수정 시 변경 요약 본문 생성
- 예약 생성/확정/거절/취소 시 기본 본문 저장

### 제외

- 수정 메타데이터
- 변경 이력 조회 API
- 메시지 카피 세부 조정
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 예약 이벤트와 시스템 메시지에 본문 필드를 추가한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/event/ReservationEvent.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationChatEventListener.java`
  - `src/main/java/com/example/easybooking/chat/SystemMessageWriter.java`
  - `src/main/java/com/example/easybooking/chat/domain/Message.java`
  - `src/main/java/com/example/easybooking/chat/domain/MessageType.java`
- 검증: 컴파일 시 새 필드와 타입이 참조 가능해야 한다.
- 완료 조건: 예약 이벤트가 본문을 실어 보낼 수 있다.

### 변경 단위 2

- 목표: 예약 수정에서 변경 요약 본문을 생성해 발송한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: 컴파일 및 흐름 확인
- 완료 조건: 수정 시 고객 채팅방에 변경 요약이 남는다.

## 완료 기준

- [x] 예약 자동 메시지 본문이 저장된다.
- [x] 예약 수정 본문과 구조화된 변경 스냅샷이 저장된다.
- [x] 컴파일과 전체 빌드가 통과한다.

## 후속 변경: 구조화된 예약 수정 메시지

### 배경

- 문자열 변경 요약만으로는 프론트엔드가 변경 전·후 비교 UI를 안정적으로 구성할 수 없다.
- 동일 예약이 여러 번 수정될 수 있으므로 메시지 조회 시 최신 변경 이력을 다시 조회해 결합하면 과거 메시지와 잘못 연결될 수 있다.
- 구조화된 변경 스냅샷을 메시지에 함께 저장해 조회 API와 WebSocket이 동일한 `MessageResponse`를 사용하게 한다.

### 변경 단위 3

- 목표: 예약 수정 메시지가 구조화된 변경 스냅샷을 저장하고 응답할 수 있는 기반을 추가한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/java/com/example/easybooking/chat/domain/Message.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/MessageResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/ReservationChangeResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/ValueChangeResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/StaffChangeResponse.java`
  - `src/main/java/com/example/easybooking/chat/dto/response/StaffSnapshotResponse.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationEvent.java`
  - `src/main/java/com/example/easybooking/chat/SystemMessageWriter.java`
  - `src/main/resources/db/migration/V18__add_message_reservation_change_snapshot.sql`
- 검증: `./gradlew compileJava`
- 완료 조건: 기존 메시지 응답은 유지되고 예약 변경 필드가 nullable 구조로 추가된다.

### 변경 단위 4

- 목표: 예약 수정 전·후를 비교해 실제로 변경된 예약 필드와 점주 전달사항을 하나의 변경 구조로 발행한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `src/main/java/com/example/easybooking/reservation/event/ReservationChatEventListener.java`
- 검증: `./gradlew clean build`
- 완료 조건:
  - 시간은 `HH:mm`으로 응답한다.
  - 시간·소요시간·담당자 중 실제 변경된 항목만 포함한다.
  - 담당자 변경은 `staffId`, `staffName`의 전·후 값을 제공한다.
  - 점주 전달사항의 전·후 값은 `reservationChange.ownerMessage`에 포함한다.
  - 최상위 `ownerMessage` 문자열은 하위 호환을 위해 유지한다.
  - 조회 API와 WebSocket이 같은 `MessageResponse` 구조를 사용한다.
  - 기존 `message` 문자열을 유지한다.

## 후속 변경: 예약 수정 날짜 변경 지원

### 변경 단위 5

- 목표: 예약 수정 요청에서 날짜 변경을 받을 수 있게 한다.
- 분류: `Structural`
- 수정 대상:
  - `ReservationUpdateRequest`
  - `ReservationChangeSnapshot`
  - `ReservationChangeResponse`
  - `Message`
  - `V20__add_message_reservation_date_change_snapshot.sql`
- 검증: `./gradlew compileJava`
- 완료 조건: `reservationChange.date`를 저장·응답할 수 있다.

### 변경 단위 6

- 목표: 예약 수정 시 날짜, 시간, `startAt`, 점유 블록을 같은 목표 시점으로 갱신한다.
- 분류: `Behavioral`
- 수정 대상:
  - `Reservation`
  - `ReservationService`
- 검증: `./gradlew clean build`
- 완료 조건:
  - 날짜만 바꿔도 기존 시간과 결합해 `startAt`이 갱신된다.
  - 날짜와 시간이 같이 바뀌어도 한 번의 목표 `LocalDateTime`으로 점유 블록을 재생성한다.
  - 날짜가 실제로 변경된 경우 `reservationChange.date.before/after`를 응답한다.

## 후속 변경: 예약 수정 메뉴 변경 지원

### 변경 단위 7

- 목표: 예약 수정 요청에서 메뉴 선택 목록을 전체 교체 방식으로 받을 수 있게 한다.
- 분류: `Behavioral`
- 수정 대상:
  - `ReservationUpdateRequest`
  - `ReservationService`
  - `ReservationMenuItemWriter`
  - `ReservationMenuInputValueWriter`
  - `ReservationMenuItemRepository`
- 검증: `./gradlew compileJava`
- 완료 조건:
  - `menuSelections`가 생략되면 기존 메뉴를 유지한다.
  - `menuSelections`가 포함되면 기존 예약 메뉴와 입력값을 삭제하고 새 선택값으로 저장한다.
  - `menuSelections: []`는 예약 메뉴 전체 제거로 처리한다.
  - 실제 메뉴 구성이 같으면 변경 메시지에는 메뉴 변경 필드를 포함하지 않는다.

### 변경 단위 8

- 목표: 예약 수정 메시지에 메뉴 변경 전·후 스냅샷을 포함한다.
- 분류: `Behavioral`
- 수정 대상:
  - `ReservationChangeSnapshot`
  - `ReservationChangeResponse`
  - `Message`
  - `MenuSnapshotResponse`
  - `MenuInputValueSnapshotResponse`
  - `V21__add_message_reservation_menu_change_snapshot.sql`
- 검증: `./gradlew clean build`
- 완료 조건:
  - 메뉴가 실제로 변경된 경우 `reservationChange.menus.before/after`를 응답한다.
  - 메뉴 스냅샷은 메뉴명, 태그명, 가격, 정렬순서, 입력값 스냅샷을 포함한다.
  - 메시지 조회 API와 WebSocket 응답이 동일한 구조를 사용한다.

## 후속 변경: 예약 확정 날짜 변경 지원

### 변경 단위 9

- 목표: 예약 확정 요청에서 날짜 변경을 받을 수 있게 한다.
- 분류: `Behavioral`
- 수정 대상:
  - `ReservationConfirmRequest`
  - `Reservation`
  - `ReservationService`
- 검증: `./gradlew clean build`
- 완료 조건:
  - 확정 시 `date`만 보내면 기존 시간을 유지한 채 날짜만 변경한다.
  - 확정 시 `startAt`만 보내면 기존 날짜를 유지한 채 시간만 변경한다.
  - 확정 시 `date`, `startAt`을 함께 보내면 둘을 결합한 새 시작 시점으로 확정한다.
  - 점유 블록은 확정된 최종 시작 시점 기준으로 생성한다.
