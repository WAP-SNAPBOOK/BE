# `#105` 프론트 전달용 예약 계약 변경 안내

작성일: `2026-03-26`

## 목적

이 문서는 프론트가 `#105` 이후 예약 생성/조회 계약을 바로 연동할 수 있도록,
현재 백엔드가 실제로 받는 요청과 실제로 내려주는 응답만 정리한다.

## 핵심 요약

- 예약 생성 요청에서 레거시 `formData`는 더 이상 현재 계약이 아니다.
- 프론트는 `shopId`, `staffId`, `date`, `time` 기반으로 예약을 생성해야 한다.
- 생성 응답은 새 표준 필드 `requirements`, `imageUrls`, `imageCount`를 반환한다.
- 과도기 호환을 위해 생성 응답의 레거시 필드 `requests`, `photoUrls`, `photoCount`도 함께 유지된다.
- 상세/목록/채팅 조회 응답도 `requirements`, `imageUrls`, `imageCount`를 포함한다.
- `formData` 파생 필드(`part`, `removal`, `extend`, `wrapping`)는 더 이상 어떤 예약 응답에서도 기대하면 안 된다.

---

## 요청 계약

### `POST /api/reservations`

- 필수: `shopId`, `staffId`, `date`, `time`
- 선택: `requirements`, `imageUrls`, `menuSelections`
- 레거시 `formData`는 지원하지 않는다.

```json
{
  "shopId": 1,
  "staffId": 10,
  "date": "2026-03-26",
  "time": "10:00",
  "requirements": "길이 짧게 해주세요",
  "imageUrls": [
    "https://example.com/a.jpg",
    "https://example.com/b.jpg"
  ],
  "menuSelections": [
    {
      "menuId": 1,
      "inputValues": [
        {
          "fieldId": 1,
          "valueNumber": 2,
          "valueText": null
        }
      ]
    }
  ]
}
```

---

## 생성 응답 계약

`201 Created`

```json
{
  "id": 1,
  "date": "2026-03-26",
  "time": "10:00:00",
  "status": "PENDING",
  "customerName": "홍길동",
  "requirements": "길이 짧게 해주세요",
  "imageCount": 2,
  "imageUrls": [
    "https://example.com/a.jpg",
    "https://example.com/b.jpg"
  ],
  "requests": "길이 짧게 해주세요",
  "photoCount": 2,
  "photoUrls": [
    "https://example.com/a.jpg",
    "https://example.com/b.jpg"
  ]
}
```

### 프론트 기준 사용 권장

- 우선 사용:
  - `requirements`
  - `imageUrls`
  - `imageCount`
- 레거시 호환:
  - `requests`
  - `photoUrls`
  - `photoCount`

### 에러 코드

- `400 Bad Request`
  - `REQUIRED_DATE_MISSING`
  - `REQUIRED_TIME_MISSING`
  - `REQUIRED_STAFF_ID_MISSING`
  - `INVALID_TIME_INTERVAL`
  - `STAFF_NOT_IN_SHOP`
- `404 Not Found`
  - `STAFF_NOT_FOUND`
- `400 Bad Request`
  - 잘못된 `time` 문자열 형식은 `INVALID_PARAMETER`로 매핑될 수 있다.

---

## 조회 응답 영향

아래 응답은 이제 공통으로 `requirements`, `imageUrls`, `imageCount`를 포함한다.

- `GET /api/reservations/{id}`
- `GET /api/reservations/my`
- `GET /api/reservations/shop`
- `GET /api/reservations/chat/customer`
- `GET /api/reservations/chat/owner`

과도기 호환을 위해 이미지 레거시 필드도 유지된다.

- `photoUrls`
- `photoCount`

제거된 필드:

- `part`
- `removal`
- `extend`
- `wrapping`

---

## 프론트 체크리스트

- 예약 생성 요청에서 `formData`를 제거한다.
- 예약 생성 요청은 `shopId`, `staffId`, `date`, `time` 기준으로 보낸다.
- 생성 응답과 조회 응답 모두 `requirements`, `imageUrls`, `imageCount` 기준으로 읽는다.
- 레거시 `requests`, `photoUrls`, `photoCount` 의존은 제거 대상으로 본다.
- `part`, `removal`, `extend`, `wrapping`을 화면 모델에서 제거한다.

---

## 한 줄 정리

프론트는 이제 **예약 요청은 `date/time/requirements/imageUrls`로 보내고,
생성/조회 응답은 `requirements/imageUrls/imageCount`를 표준으로 읽으며,
레거시 `requests/photoUrls/photoCount`는 과도기 호환으로만 본다**고 이해하면 된다.
