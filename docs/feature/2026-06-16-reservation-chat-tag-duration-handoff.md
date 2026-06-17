# 예약/채팅 계약 변경 안내

작성일: `2026-06-16`
최종 갱신: `2026-06-17`

이 문서는 프론트가 예약 생성, 예약 상세, 메뉴 가격, 채팅 메시지, 채팅방 목록 연동을 바로 맞출 수 있도록 현재 백엔드 계약 변경분만 정리한 문서다.

## 적용 브랜치

- 브랜치: `feature/jiseob/reservation-menu-tag-id`
- 주요 커밋:
  - `feat: add menu price snapshots`

## 변경 요약

- 채팅방 목록 응답에 `lastMessageType`이 추가된다.
- 예약 확정 시스템 메시지에 `durationMinutes`가 포함된다.
- 예약 상세 응답에 `durationMinutes`가 포함된다.
- 예약 생성 시 `menuSelections[].tagId`가 필수다.
- 예약 상세 `menus[]`에 `tagNameSnapshot`이 포함된다.
- 매장 메뉴에 `price`가 추가된다.
- 예약 상세 `menus[]`에 예약 시점 가격인 `priceSnapshot`이 포함된다.

## 1. 예약 생성 요청

예약 생성 시 `menuSelections`는 `menuId`만 보내는 구조가 아니라, `tagId`도 함께 보내야 한다.

### Request

```json
{
  "shopId": 1,
  "staffId": 10,
  "date": "2026-06-16",
  "time": "14:00",
  "requirements": "짧게",
  "imageUrls": [],
  "menuSelections": [
    {
      "menuId": 11,
      "tagId": 101,
      "inputValues": []
    }
  ]
}
```

### 의미

- `menuId`: 선택한 메뉴 ID
- `tagId`: 사용자가 실제로 눌러서 들어온 태그 ID
- `inputValues`: 메뉴별 추가 입력값

### 중요

- `tagId`는 필수다.
- `tagId`가 없으면 `400 Bad Request`로 처리된다.
- 메뉴에 태그가 여러 개 붙어 있어도, 프론트는 **실제로 선택한 태그 1개**만 보내면 된다.
- 가격은 예약 생성 요청에서 보내지 않는다. 백엔드가 `menuId` 기준으로 원본 메뉴 가격을 읽어 예약 스냅샷에 저장한다.

## 2. 메뉴 생성/수정/조회

매장 메뉴에 `price`가 추가됐다.

### Create Request

```json
{
  "name": "젤네일",
  "description": "기본 젤네일",
  "price": 50000,
  "sortOrder": 0
}
```

### Update Request

```json
{
  "name": "젤아트",
  "description": "새 설명",
  "price": 60000,
  "sortOrder": 1
}
```

### Menu Response

```json
{
  "id": 11,
  "shopId": 1,
  "name": "젤네일",
  "description": "기본 젤네일",
  "price": 50000,
  "isActive": true,
  "sortOrder": 0,
  "tags": [
    {
      "id": 101,
      "name": "손관리"
    }
  ]
}
```

### 의미

- `price`: 현재 매장 메뉴에 설정된 가격
- 단위는 원화 정수값이다.
- `price`는 nullable이다. 가격 미설정 메뉴는 `null`로 내려갈 수 있다.
- 예약 생성 시 프론트가 가격을 보내면 안 된다. 예약 가격은 백엔드가 서버에 저장된 메뉴 가격으로 결정한다.

## 3. 예약 상세 응답

예약 상세 `menus[]`에는 태그 스냅샷과 가격 스냅샷이 포함된다.

### Response

```json
{
  "id": 1,
  "status": "CONFIRMED",
  "date": "2026-06-16",
  "time": "14:00:00",
  "durationMinutes": 60,
  "shopId": 1,
  "shopName": "테스트샵",
  "customerName": "고객",
  "customerPhone": "01012345678",
  "confirmationMessage": "확정합니다",
  "requirements": "짧게",
  "menus": [
    {
      "shopMenuId": 11,
      "menuNameSnapshot": "젤네일",
      "tagNameSnapshot": "손관리",
      "priceSnapshot": 50000,
      "sortOrder": 0,
      "inputValues": []
    }
  ]
}
```

### 의미

- `tagNameSnapshot`: 예약 생성 시점에 저장된 태그명
- `priceSnapshot`: 예약 생성 시점에 저장된 메뉴 가격
- 태그명이 이후 바뀌어도 과거 예약 상세는 이 스냅샷 값을 유지한다
- 메뉴 가격이 이후 바뀌어도 과거 예약 상세는 이 스냅샷 값을 유지한다
- 메뉴에 태그가 여러 개 붙어 있어도, 저장은 `tagId` 기준으로 선택된 태그 1개만 반영된다
- 가격 미설정 메뉴로 예약한 경우 `priceSnapshot`은 `null`일 수 있다

## 4. 예약 확정 시스템 메시지

예약 확정 시 생성되는 시스템 메시지에는 `durationMinutes`가 포함된다.

### Message Response

```json
{
  "messageType": "RESERVATION_CONFIRMED",
  "messageId": 1001,
  "senderId": 0,
  "senderName": "SYSTEM",
  "message": null,
  "imageUrl": null,
  "sentAt": "2026-06-16T14:00:00",
  "roomId": 4,
  "reservationId": 1,
  "durationMinutes": 60
}
```

### 의미

- `durationMinutes`: 점주가 예약 확정 시 입력한 소요시간
- 예약 확정 시스템 메시지와 websocket payload에 함께 내려간다
- 예약 생성/거절 시스템 메시지는 소요시간이 없으므로 `durationMinutes`가 `null`일 수 있다

## 5. 채팅방 목록 응답

채팅방 목록에는 마지막 메시지 타입이 추가됐다.

### Response

```json
{
  "chatRoomId": 4,
  "shopId": 1,
  "shopBusinessName": "테스트",
  "otherUserId": 1,
  "otherUserName": "테스트 매장",
  "lastMessageSenderId": 0,
  "lastMessageType": "RESERVATION_CREATED",
  "lastMessageContent": null,
  "lastMessageAt": "2026-06-13T20:13:28.482283",
  "unreadCount": 1
}
```

### 의미

- `lastMessageType` 기준으로 프리뷰 문구를 결정할 수 있다
- 시스템 메시지일 때도 `RESERVATION_CREATED`, `RESERVATION_CONFIRMED`, `RESERVATION_REJECTED`를 구분할 수 있다
- `lastMessageContent`가 `null`이어도 `lastMessageType`을 우선 사용하면 된다

## 6. 프론트 구현 기준

- 예약 생성 화면은 `menuSelections[].menuId`와 `menuSelections[].tagId`를 함께 보내야 한다.
- 태그가 여러 개인 메뉴는 사용자가 실제로 선택한 태그 ID를 보내면 된다.
- 메뉴 목록/관리 화면은 `price`를 사용하면 된다.
- 예약 상세 화면은 `menus[].tagNameSnapshot`과 `menus[].priceSnapshot`을 사용하면 된다.
- 예약 생성 요청에는 가격을 보내지 않는다.
- 채팅 프리뷰는 `lastMessageType`으로 분기하면 된다.
- 예약 확정 메시지 화면은 `durationMinutes`를 함께 표시할 수 있다.

## 7. 에러 기준

- `menuSelections[].tagId` 누락: `400 Bad Request`
- `menuSelections[]` 자체가 비어 있음: `400 Bad Request`
- 선택한 `tagId`가 해당 메뉴/매장과 맞지 않음: `400 Bad Request`

## 8. 표시 권장

- 메뉴 목록: `name`, `tag`, `price`를 표시한다.
- 예약 상세: `tagNameSnapshot > menuNameSnapshot` 순서로 보여주고, 가격은 `priceSnapshot`을 표시한다.
- 채팅방 목록: `lastMessageType`으로 시스템 메시지 프리뷰를 만든다.
- 예약 확정 시스템 메시지: `durationMinutes`가 있으면 예상 소요시간을 함께 보여준다.

## 한 줄 정리

프론트는 예약 생성 때 `menuId + tagId`만 보내고 가격은 보내지 않는다. 메뉴 화면은 `price`, 예약 상세는 `tagNameSnapshot + priceSnapshot`, 채팅방 목록은 `lastMessageType`, 확정 메시지는 `durationMinutes`를 기준으로 쓰면 된다.
