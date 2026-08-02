# `[REFACTOR]: 채팅방 중복 생성 방지와 메시지 조회 인덱스를 추가`

Issue: `#132`  
Created: `2026-07-03`  
Repository: `WAP-SNAPBOOK/BE`  
Issue URL: `https://github.com/WAP-SNAPBOOK/BE/issues/132`  
Branch: `refactor/jiseob/#132`

---

## Background

채팅 도메인은 `chat_room`이 매장-고객 대화방을 나타내고, `message`가 대화 이력을 저장한다. 코드에서는 `findByShopIdAndCustomerId`로 방이 하나라고 전제하고, 메시지는 `chat_room_id`와 `id` 커서를 기준으로 조회한다.

관련 코드:

- `src/main/java/com/example/easybooking/chat/domain/ChatRoom.java`
- `src/main/java/com/example/easybooking/chat/domain/Message.java`
- `src/main/java/com/example/easybooking/chat/repository/ChatRoomRepository.java`
- `src/main/java/com/example/easybooking/chat/repository/MessageRepository.java`

## Problem

현재 DB 스키마에는 `chat_room(shop_id, customer_id)` 유니크 제약이 없어 동시 요청 시 중복 채팅방이 생길 수 있다. 또한 메시지 이력 조회와 unread count 쿼리의 핵심 조건에 맞는 인덱스가 부족해 데이터가 쌓이면 채팅 목록/이력 조회 성능이 급격히 떨어질 수 있다.

## Goal

채팅방 단일성은 DB 제약으로 보장하고, 메시지 목록/커서 조회/unread count에 필요한 인덱스를 추가한다.

## Scope

- `chat_room(shop_id, customer_id)` 유니크 제약을 추가한다.
- 중복 채팅방이 이미 존재하는지 점검하는 사전 검증 쿼리를 작성한다.
- 채팅방 목록 조회용 인덱스 `chat_room(owner_id, last_message_at)`와 `chat_room(customer_id, last_message_at)`를 검토한다.
- 메시지 커서 조회용 인덱스 `message(chat_room_id, id)`를 추가한다.
- unread count 최적화를 위해 `message(chat_room_id, sender_id, id)` 또는 대안 인덱스를 검토한다.
- `message.message_type`의 `NOT NULL` 전환 가능성을 검토한다.
- `last_message_id`, `owner_last_read_message_id`, `customer_last_read_message_id`의 정합성 보장 방식을 정한다.

## Out Of Scope

- WebSocket 프로토콜 변경
- 메시지 읽음 모델을 별도 테이블로 전면 재설계
- 채팅 알림 기능 추가

## Acceptance Criteria

- [ ] 같은 `shop_id`, `customer_id` 조합으로 채팅방이 중복 생성되지 않는다.
- [ ] 메시지 최신 조회와 커서 조회가 `message(chat_room_id, id)` 인덱스를 사용할 수 있다.
- [ ] unread count 쿼리의 인덱스 전략이 명확히 문서화되거나 적용된다.
- [ ] 기존 중복 데이터가 있는 경우 마이그레이션 전에 감지할 수 있다.
- [ ] 채팅 관련 Flyway 마이그레이션과 JPA 매핑이 일치한다.

## Risks

- 기존 중복 채팅방이 있으면 유니크 제약 추가가 실패한다.
- 인덱스를 과도하게 추가하면 메시지 쓰기 비용이 증가한다.
- 읽음 포인터를 FK로 강하게 묶을 경우 메시지 삭제 정책과 충돌할 수 있다.

## References

- `src/main/java/com/example/easybooking/chat/repository/ChatRoomRepository.java`
- `src/main/java/com/example/easybooking/chat/repository/MessageRepository.java`
- `src/main/java/com/example/easybooking/chat/domain/ChatRoom.java`
- `src/main/java/com/example/easybooking/chat/domain/Message.java`
