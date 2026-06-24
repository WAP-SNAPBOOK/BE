# `#131 예약 취소 자동 메시지` 구현 계획

작성일: `2026-06-18`  
기준 이슈: `#131`  
관련 브랜치: `feature/#131`

## 목적

예약 취소가 발생했을 때 고객 채팅방에 시스템 메시지를 자동 발송한다.

이번 이슈는 취소 이벤트의 자동 메시지 연결만 다룬다. 메시지 문구 고도화, 사유 포함, 변경 이력 연계는 후속 이슈로 분리한다.

## 구현 원칙

- 예약 취소 API는 `#130`에서 이미 추가했다.
- 기존 `ReservationEvent` / `ReservationChatEventListener` / `SystemMessageWriter` 흐름을 재사용한다.
- 새로운 메시지 타입만 추가해 최소 변경으로 연결한다.
- 테스트는 이번 이슈 범위에서 추가하지 않는다.

## 현재 문제

- 예약 확정/거절은 시스템 메시지로 흐르지만, 취소는 메시지 타입이 없어 자동 발송되지 않는다.
- 취소 후 고객 채팅방에 상태 변화가 남지 않는다.

## 목표 범위

### 포함

- `MessageType.RESERVATION_CANCELED` 추가
- `ReservationService.cancelReservation()`에서 취소 이벤트 발행
- 취소 이벤트가 채팅방 시스템 메시지로 저장되고 발행되도록 연결

### 제외

- 취소 사유 메시지
- 예약 변경 이력
- 취소 메타데이터
- 캘린더 추가 기능
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 취소 메시지 타입을 추가한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/java/com/example/easybooking/chat/domain/MessageType.java`
- 검증: 컴파일 시 메시지 타입 참조가 가능해야 한다.
- 완료 조건: 취소 메시지 타입이 존재한다.

### 변경 단위 2

- 목표: 취소 시 예약 이벤트를 발행한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: 컴파일 및 이벤트 연결 확인
- 완료 조건: 취소 시 고객 채팅방에 시스템 메시지가 저장/발행된다.

## 완료 기준

- [ ] 취소 메시지 타입이 추가된다.
- [ ] 취소 시 예약 이벤트가 발행된다.
- [ ] 컴파일이 통과한다.
