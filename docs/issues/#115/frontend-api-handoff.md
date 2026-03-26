# `#115` 프론트 전달용 API 변경 안내

작성일: `2026-03-26`

## 목적

이 문서는 프론트에서 `#115` 예약 진입 연동 시 알아야 할
현재 백엔드 API 계약만 빠르게 확인할 수 있도록 정리한 문서다.

핵심 방향은 아래와 같다.

- 예약 시작 전에 `staffId`를 확보하기 위한 `booking-entry` 응답이 추가됐다.
- 공개 링크 진입과 인증 사용자 진입이 같은 응답 계약을 사용한다.
- 이후 예약 가능 시간 조회는 `shopId + staffId` 기준 API를 사용한다.
- 기존 `shopId` 단독 availability API는 deprecated 상태다.

---

## 가장 먼저 알아야 할 점

### 1. 새 예약 진입 API가 추가됐다

- `GET /api/public/shops/{slugOrCode}/booking-entry`
- `GET /api/v1/shops/{shopId}/booking-entry`

두 API는 응답 형태가 같다.

```json
{
  "shopId": 1,
  "shopName": "네일샵",
  "defaultStaffId": 10,
  "staffs": [
    { "staffId": 10, "name": "원장" },
    { "staffId": 11, "name": "직원A" }
  ]
}
```

### 2. 공개/인증 진입만 다르고 응답은 같다

- 공유 링크 화면에서는 `slugOrCode` 기반 공개 API를 사용한다.
- 채팅 사용자 화면처럼 이미 `shopId`를 알고 있는 경우에는 인증 API를 사용한다.

### 3. 예약 가능 시간 조회는 `staffId` 기준으로 간다

권장 흐름은 아래다.

1. `booking-entry` 호출
2. `defaultStaffId` 또는 사용자가 선택한 `staffId` 확보
3. `GET /api/v1/shops/{shopId}/staff/{staffId}/availability`
4. 월 달력 필요 시 `GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly`

### 4. 기존 `shopId` 단독 availability 경로는 deprecated다

- 기존: `GET /api/reservations/shop/{shopId}/availability`
- 상태: deprecated
- 새 연동에서는 사용하지 않는 것을 권장한다.

---

## 권장 연동 흐름

### 1. 공유 링크 진입

1. 프론트가 `/s/{slugOrCode}` 화면에 진입
2. `GET /api/public/shops/{slugOrCode}/booking-entry` 호출
3. `shopId`, `staffs`, `defaultStaffId` 확보
4. 초기 선택 직원은 `defaultStaffId`로 맞추거나, 사용자 선택을 받는다
5. 선택된 `staffId`로 availability API 호출

### 2. 채팅/인증 사용자 진입

1. 프론트가 이미 `shopId`를 알고 있음
2. `GET /api/v1/shops/{shopId}/booking-entry` 호출
3. `staffs`, `defaultStaffId` 확보
4. 선택된 `staffId`로 availability API 호출

---

## API 상세

## 1. `GET /api/public/shops/{slugOrCode}/booking-entry`

### 용도

공유 링크 진입 시 로그인 없이 예약 시작 컨텍스트를 조회한다.

### 인증

- 불필요

### 경로 의미

- `slugOrCode`는 아래 순서로 해석된다.
    - 매장 `slug`
    - `slug`가 없거나 매칭 실패 시 `publicCode`

### 성공 응답

`200 OK`

```json
{
  "shopId": 1,
  "shopName": "네일샵",
  "defaultStaffId": 10,
  "staffs": [
    { "staffId": 10, "name": "원장" },
    { "staffId": 11, "name": "직원A" }
  ]
}
```

### 에러

- `404 Not Found`
    - 의미: `slugOrCode`에 해당하는 매장을 찾지 못함

### 프론트 주의사항

- 응답의 `shopId`를 이후 예약 생성/채팅 진입 공통 식별자로 사용한다.
- `defaultStaffId`는 현재 `staffs` 첫 번째 직원 기준이다.

---

## 2. `GET /api/v1/shops/{shopId}/booking-entry`

### 용도

이미 `shopId`를 알고 있는 인증 사용자 진입에서 같은 예약 시작 컨텍스트를 조회한다.

### 인증

- 필요
- `Authorization: Bearer <access-token>` 헤더 필요

### 성공 응답

`200 OK`

공개 API와 동일

### 에러

- `401 Unauthorized`
    - 의미: 토큰 누락 또는 유효하지 않은 토큰

- `404 Not Found`
    - 의미: `shopId`에 해당하는 매장을 찾지 못함

### 프론트 주의사항

- 현재 응답 내용 자체는 사용자별로 달라지지 않는다.
- 다만 인증이 필요한 경로이므로 토큰이 없으면 컨트롤러까지 진입하지 못한다.

---

## 3. `GET /api/v1/shops/{shopId}/staff/{staffId}/availability`

### 용도

선택한 직원 기준 하루 예약 가능 시간 슬롯을 조회한다.

### 요청 예시

```http
GET /api/v1/shops/1/staff/10/availability?date=2026-03-26
```

### 프론트 주의사항

- `shopId`와 `staffId` 둘 다 필요하다.
- `booking-entry` 응답에서 받은 `defaultStaffId` 또는 사용자가 선택한 `staffId`를 사용한다.

---

## 4. `GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly`

### 용도

선택한 직원 기준 월별 예약 가능 날짜를 조회한다.

### 요청 예시

```http
GET /api/v1/shops/1/staff/10/availability/monthly?yearMonth=2026-03
```

---

## deprecated API

## 5. `GET /api/reservations/shop/{shopId}/availability`

### 상태

- deprecated

### 의미

- 과거 `shopId` 단독 기준 availability 경로
- 새 프론트 연동에서는 `booking-entry -> staffId -> availability` 흐름으로 전환 권장

---

## 응답 필드 해석

- `shopId`
    - 이후 예약/채팅/availability 호출에서 공통으로 쓰는 매장 식별자

- `shopName`
    - 예약 시작 화면 상단 표시용

- `defaultStaffId`
    - 초기 선택 직원용 식별자
    - 현재는 `staffs` 첫 번째 직원의 `staffId`

- `staffs[]`
    - 예약 가능한 직원 선택 목록
    - 현재는 `id ASC` 순서

---

## 프론트 체크리스트

- 공유 링크 진입은 `GET /api/public/shops/{slugOrCode}/booking-entry`를 사용한다.
- 인증 사용자 진입은 `GET /api/v1/shops/{shopId}/booking-entry`를 사용한다.
- 예약 가능 시간 조회 전 반드시 `staffId`를 확보한다.
- 새 연동에서는 `GET /api/reservations/shop/{shopId}/availability`를 사용하지 않는다.
- 초기 선택 직원은 `defaultStaffId`를 사용하거나, 사용자 선택으로 덮어쓴다.

---

## 한 줄 정리

프론트는 이제 **먼저 `booking-entry`로 `shopId`와 `staffId`를 확보한 뒤,
그 `staffId`로 availability를 조회한다**고 이해하면 된다.
