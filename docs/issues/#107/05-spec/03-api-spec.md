# `reservation` API 명세

작성일: `2026-03-10`

## 1. 예약 생성

- Method/Path: `POST /api/reservations`
- Actor: 고객

Request:

```json
{
  "shopId": 1,
  "staffId": 10,
  "date": "2026-03-20",
  "time": "10:00",
  "requirements": "짧게 부탁드립니다",
  "imageUrls": ["https://example.com/a.jpg"],
  "menuSelections": [
    {
      "menuId": 1,
      "inputValues": [
        {
          "fieldId": 101,
          "valueNumber": 2,
          "valueText": null
        }
      ]
    }
  ]
}
```

Success:

- `201 Created`

Core Errors:

- `RESERVATION_OUTSIDE_OPERATING_HOURS`
- `RESERVATION_ON_HOLIDAY`
- `BOOKING_WINDOW_EXCEEDED`
- `MIN_BOOKING_LEAD_TIME_NOT_MET`
- `PAST_RESERVATION_NOT_ALLOWED`
- `TIME_BLOCK_ALREADY_BOOKED`
- `MENU_NOT_IN_SHOP`
- `MENU_INACTIVE`
- `INVALID_MENU_INPUT_FIELD`
- `REQUIRED_MENU_INPUT_MISSING`
- `INVALID_MENU_INPUT_VALUE`

## 2. 예약 상세 조회

- Method/Path: `GET /api/reservations/{id}`
- Actor: 예약 고객 또는 점주

Response 핵심 필드:

- `id`
- `status`
- `date`
- `time`
- `shopId`
- `shopName`
- `customerName`
- `customerPhone`
- `requirements`
- `photoUrls`
- `menus`

보류:

- `staffId`
- `startAt`
- `durationMinutes`

## 3. 고객 목록 조회

- Method/Path: `GET /api/reservations/my`
- Actor: 고객

## 4. 점주 목록 조회

- Method/Path: `GET /api/reservations/shop`
- Actor: 점주

## 5. 채팅방 컨텍스트 조회

- 고객: `GET /api/reservations/chat/customer?shopId={shopId}`
- 점주: `GET /api/reservations/chat/owner?shopId={shopId}&customerId={customerId}`

## 6. 예약 확정

- Method/Path: `PUT /api/reservations/{id}/confirm`
- Actor: 점주

Request:

```json
{
  "message": "내일 10시 30분에 방문해주세요",
  "durationMinutes": 60
}
```

Success:

- `200 OK`

Core Errors:

- `INVALID_RESERVATION_STATUS_FOR_CONFIRM`
- `TIME_BLOCK_ALREADY_BOOKED`

## 7. 예약 거절

- Method/Path: `PUT /api/reservations/{id}/reject`
- Actor: 점주

Request:

```json
{
  "reason": "해당 시간에는 예약이 어렵습니다"
}
```

Success:

- `200 OK`

Core Errors:

- `INVALID_RESERVATION_STATUS_FOR_REJECT`

## 8. 예약 취소

- Method/Path: `PUT /api/reservations/{id}/cancel`
- Actor: 고객 또는 점주

Request:

```json
{
  "reason": "개인 사정으로 방문이 어렵습니다"
}
```

처리 규칙:

- 고객은 예약일 전날 `23:59:59`까지만 성공
- 점주는 운영상 언제든 처리 가능

Success:

- `200 OK`

Response 핵심 필드:

- `status`
- `cancellationInfo`

Core Errors:

- `INVALID_RESERVATION_STATUS_FOR_CANCEL`
- `CUSTOMER_CANCELLATION_DEADLINE_PASSED`

## 9. 예약 수정

- Method/Path: `PATCH /api/reservations/{id}`
- Actor: 점주

Request 수정 가능 필드:

- `date`
- `time`
- `staffId`
- `requirements`
- `imageUrls`
- `menuSelections`

처리 규칙:

- `CONFIRMED`에서 `date/time/staffId` 변경 시 점유 재계산
- 충돌 시 전체 수정 실패

Success:

- `200 OK`

Core Errors:

- `TIME_BLOCK_ALREADY_BOOKED`
- 상태 전이 관련 에러가 아니라 수정 정책 위반/권한/validation 오류

## 10. 제거 대상 API

- `GET /api/reservations/shop/{shopId}/availability`

이 책임은 `availability` 도메인 API로 이관한다.
