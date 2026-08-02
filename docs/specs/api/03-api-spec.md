# 03-api-spec

APP: 시작 전
BE: 시작 전

# 03-api-spec

APP: 시작 전
BE: 시작 전

# API 명세서 - Snapbook (기능별)

> 작성일: 2026-02-15
범례: [존재] 기존 구현 | [미구현] 문서상 계획만 있고 현재 코드에 없음 | [수정] 기존 변경 필요
> 

---

## 목차

1. [인증 (Auth)](about:blank#1-%EC%9D%B8%EC%A6%9D-auth)
2. [회원가입 (User)](about:blank#2-%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85-user)
3. [매장 관리 (Shop)](about:blank#3-%EB%A7%A4%EC%9E%A5-%EA%B4%80%EB%A6%AC-shop)
4. [운영시간 설정 (Schedule)](about:blank#4-%EC%9A%B4%EC%98%81%EC%8B%9C%EA%B0%84-%EC%84%A4%EC%A0%95-schedule)
5. [휴무일 설정 (Holiday)](about:blank#5-%ED%9C%B4%EB%AC%B4%EC%9D%BC-%EC%84%A4%EC%A0%95-holiday)
6. [직원 관리 (Staff)](about:blank#6-%EC%A7%81%EC%9B%90-%EA%B4%80%EB%A6%AC-staff)
7. [직원 운영시간 (Staff Operating Times)](about:blank#7-%EC%A7%81%EC%9B%90-%EC%9A%B4%EC%98%81%EC%8B%9C%EA%B0%84-staff-operating-times)
8. [직원 휴무일 (Staff Holiday)](about:blank#8-%EC%A7%81%EC%9B%90-%ED%9C%B4%EB%AC%B4%EC%9D%BC-staff-holiday)
9. [태그 관리 (Tag)](about:blank#9-%ED%83%9C%EA%B7%B8-%EA%B4%80%EB%A6%AC-tag)
10. [메뉴 관리 (Menu)](about:blank#10-%EB%A9%94%EB%89%B4-%EA%B4%80%EB%A6%AC-menu)
11. [메뉴 입력 필드 (Menu Input Field)](about:blank#11-%EB%A9%94%EB%89%B4-%EC%9E%85%EB%A0%A5-%ED%95%84%EB%93%9C-menu-input-field)
12. [카테고리/태그 용어 매핑](about:blank#12-%EC%B9%B4%ED%85%8C%EA%B3%A0%EB%A6%AC%ED%83%9C%EA%B7%B8-%EC%9A%A9%EC%96%B4-%EB%A7%A4%ED%95%91)
13. [예약 가용성 조회 (Availability)](about:blank#13-%EC%98%88%EC%95%BD-%EA%B0%80%EC%9A%A9%EC%84%B1-%EC%A1%B0%ED%9A%8C-availability)
14. [예약 (Reservation)](about:blank#14-%EC%98%88%EC%95%BD-reservation)
15. [채팅 (Chat)](about:blank#15-%EC%B1%84%ED%8C%85-chat)
16. [파일 업로드 (File)](about:blank#16-%ED%8C%8C%EC%9D%BC-%EC%97%85%EB%A1%9C%EB%93%9C-file)
17. [공유 링크 (Link)](about:blank#17-%EA%B3%B5%EC%9C%A0-%EB%A7%81%ED%81%AC-link)
18. [예약 진입 (Booking Entry)](about:blank#18-%EC%98%88%EC%95%BD-%EC%A7%84%EC%9E%85-booking-entry)

---

## 인증 기준 (현재 구현)

- 기본 정책: `SecurityConfig`에서 `anyRequest().authenticated()` 적용
- 공개 경로(`allowUrls`) 예시:
    - `/auth/refresh`
    - `/auth/token-validation`
    - `/oauth/login/kakao`, `/oauth/login/kakao/local`, `/oauth/login/kakao/loadtest`
    - `/s/**`
    - `/api/public/shops/*/booking-entry`
    - `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
    - `/actuator/**`, `/h2-console/**`, `/ws-connect/**`
- 따라서 `/api/**`, `/api/v1/**` 계열은 별도 예외 등록이 없으면 JWT 인증이 필요

---

## 1. 인증 (Auth)

### 1-1. 카카오 로그인 [존재]

```
POST /oauth/login/kakao
```

- 인증: 없음 (공개, `allowUrls`)

**Request Body:**

```json
{
  "accessCode": "string"
  // 카카오 인가 코드
}
```

**Response Body (200):**

```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "userId": 1,
  "role": "OWNER",
  "message": "로그인 성공",
  "authStatus": "LOGIN_SUCCESS | SIGNUP_REQUIRED",
  "userType": "OWNER | CUSTOMER"
}
```

> `authStatus`가 `SIGNUP_REQUIRED`이면 `accessToken`에 임시 토큰이 발급됨. 이 토큰으로 회원가입 API 호출.
> 

---

### 1-2. 카카오 로그인 (로컬) [존재]

```
POST /oauth/login/kakao/local
```

- 스펙 동일. redirect URL만 로컬 환경용.

---

### 1-3. 토큰 갱신 [존재]

```
POST /auth/refresh
```

- 인증: 없음 (공개, `allowUrls`)

**Request Body:**

```json
{
  "token": "string"
  // refreshToken
}
```

**Response Body (200):**

```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "userId": 1,
  "role": "OWNER",
  "message": "...",
  "authStatus": "LOGIN_SUCCESS",
  "userType": "OWNER"
}
```

---

### 1-4. 토큰 유효성 검증 [존재]

```
POST /auth/token-validation
```

- 인증: 없음 (공개, `allowUrls`)

**Request Body:**

```json
{
  "token": "string"
}
```

**Response:** `200 OK` (유효) / `4xx` (무효)

---

## 2. 회원가입 (User)

### 2-1. 점주 회원가입 [존재]

```
POST /user/owner/signup
```

- 인증: `@RequireTempUser` (임시 토큰)

**Request Body:**

```json
{
  "name": "string",
  "phoneNumber": "string"
}
```

**Response Body (200):**

```json
{
  "userType": "OWNER",
  "userId": 1,
  "name": "string",
  "phoneNumber": "string",
  "tokens": {
    "accessToken": "string",
    "refreshToken": "string"
  }
}
```

---

### 2-2. 고객 회원가입 [존재]

```
POST /user/customer/signup
```

- 인증: `@RequireTempUser` (임시 토큰)

**Request Body:**

```json
{
  "name": "string",
  "phoneNumber": "string"
}
```

**Response Body (200):**

```json
{
  "userType": "CUSTOMER",
  "userId": 1,
  "name": "string",
  "phoneNumber": "string",
  "tokens": {
    "accessToken": "string",
    "refreshToken": "string"
  }
}
```

---

### 2-3. 내 정보 조회 [존재]

```
GET /user/me
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):**

```json
{
  "userId": 1,
  "name": "string",
  "phoneNumber": "string",
  "userType": "OWNER | CUSTOMER",
  "role": "ROLE_USER | ROLE_ADMIN"
}
```

---

## 3. 매장 관리 (Shop)

### 3-1. 매장 생성 [존재]

```
POST /shop
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:**

```json
{
  "businessName": "string",
  // 필수 (NotBlank)
  "address": "string",
  // 선택
  "businessNumber": "string"
  // 선택
}
```

**Response Body (200):**

```json
{
  "ownerId": 1,
  "shopId": 1,
  "businessName": "string",
  "address": "string",
  "businessNumber": "string"
}
```

---

### 3-2. 매장 정보 조회 (slug/code) [존재]

```
GET /shop/{slugOrCode}
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):**

```json
{
  "shopId": 1,
  "shopName": "string"
}
```

---

### 3-3. 매장 정보 조회 (shopId) [존재]

```
GET /shop/info/{shopId}
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):** 3-2와 동일

---

### 3-4. 공유 링크 정보 조회 [존재]

```
GET /shop/link
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):**

```json
{
  "shopId": 1,
  "fullUrl": "https://domain.com/s/my-shop",
  "canonicalUrl": "/s/my-shop",
  "slug": "my-shop",
  "publicCode": "ABC12345"
}
```

---

### 3-5. 슬러그 변경 [존재]

```
PUT /shop/link/slug
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:**

```json
{
  "slug": "my-shop"
  // 3~20자, [a-z0-9-] 패턴
}
```

**Response Body (200):** `LinkInfoResponse`와 동일

---

## 4. 운영시간 설정 (Schedule)

### 4-1. 스케줄 설정 조회 [존재](고객%20입장도%20가능)

```
GET /api/v1/shops/{shopId}/schedule/settings
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):**

```json
{
  "shopId": 1,
  "intervalMinutes": 30,
  // 슬롯 간격 (분)
  "scheduleType": "DAILY | WEEKDAY_WEEKEND | BY_DAY",
  "bookingWindowDays": 30,
  // 예약 가능 기간 (일)
  "minBookingLeadMinutes": 60,
  // 최소 예약 리드타임 (분)
  "publicHolidayOff": false
  // 공휴일 자동 휴무
}
```

---

### 4-2. 간격(interval) 설정 수정 [존재]

```
PUT /api/v1/shops/{shopId}/schedule/interval
```

- 인증: `@RequireAuthenticatedUser`
- 검증: `intervalMinutes`는 `30` 또는 `60`만 허용

**Request Body:**

```json
{
  "intervalMinutes": 30
  // 필수, 30 또는 60
}
```

**Response Body (200):** `ShopScheduleSettingsResponse`와 동일

> `bookingWindowDays`, `minBookingLeadMinutes`, `publicHolidayOff` 수정은 후속 `booking-policy` API로 분리 예정.
> 

---

### 4-3. 운영시간 조회 [존재]

```
GET /api/v1/shops/{shopId}/schedule/operating-times
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):**

```json
{
  "scheduleType": "BY_DAY",
  "dayTimes": {
    "MONDAY": [
      {
        "start": "09:00",
        "end": "18:00"
      }
    ],
    "TUESDAY": [
      {
        "start": "09:00",
        "end": "18:00"
      }
    ]
  }
}
```

> `dayTimes`는 모든 타입에서 요일별로 정규화되어 반환됨.
> 

---

### 4-4. 운영시간 설정 [존재]

```
PUT /api/v1/shops/{shopId}/schedule/operating-times
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:**

`scheduleType`에 따라 사용하는 필드가 다름:

**DAILY:**

```json
{
  "scheduleType": "DAILY",
  "times": [
    {
      "start": "09:00",
      "end": "18:00"
    }
  ]
}
```

**WEEKDAY_WEEKEND:**

```json
{
  "scheduleType": "WEEKDAY_WEEKEND",
  "weekdayTimes": [
    {
      "start": "09:00",
      "end": "18:00"
    }
  ],
  "weekendTimes": [
    {
      "start": "10:00",
      "end": "16:00"
    }
  ]
}
```

**BY_DAY:**

```json
{
  "scheduleType": "BY_DAY",
  "dayTimes": {
    "MONDAY": [
      {
        "start": "09:00",
        "end": "18:00"
      }
    ],
    "TUESDAY": [
      {
        "start": "10:00",
        "end": "20:00"
      }
    ]
  }
}
```

**Response:** `200 OK` (body 없음)

---

## 5. 휴무일 설정 (Holiday)

### 5-1. 휴무일 목록 조회 [존재]

```
GET /api/v1/shops/{shopId}/schedule/holidays
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):**

```json
{
  "holidays": [
    {
      "holidayId": 1,
      "holidayType": "WEEKLY | BIWEEKLY | MONTHLY | CUSTOM",
      "dayOfWeek": "MONDAY",
      // WEEKLY, BIWEEKLY, MONTHLY
      "weekOfMonth": 1,
      // MONTHLY 전용 (1~5)
      "referenceDate": "2026-02-10",
      // BIWEEKLY 전용 (기준일)
      "specificDate": "2026-03-01"
      // CUSTOM 전용
    }
  ]
}
```

---

### 5-2. 휴무일 생성 [존재]

```
POST /api/v1/shops/{shopId}/schedule/holidays
```

- 인증: `@RequireAuthenticatedUser`

**Request Body (타입별 예시):**

**매주 휴무 (WEEKLY):**

```json
{
  "holidayType": "WEEKLY",
  "dayOfWeek": "SUNDAY"
}
```

**격주 휴무 (BIWEEKLY):**

```json
{
  "holidayType": "BIWEEKLY",
  "dayOfWeek": "MONDAY",
  "referenceDate": "2026-02-10"
  // 격주 기준 날짜
}
```

**매달 n째주 휴무 (MONTHLY):**

```json
{
  "holidayType": "MONTHLY",
  "dayOfWeek": "WEDNESDAY",
  "weekOfMonth": 2
  // 둘째 주
}
```

**특정 날짜 휴무 (CUSTOM):**

```json
{
  "holidayType": "CUSTOM",
  "specificDate": "2026-03-01"
}
```

**Response Body (201):** `ShopHolidayResponse`와 동일

---

### 5-3. 휴무일 삭제 [존재]

```
DELETE /api/v1/shops/{shopId}/schedule/holidays/{holidayId}
```

- 인증: `@RequireAuthenticatedUser`

**Response:** `204 No Content`

---

## 6. 직원 관리 (Staff)

> 현재 코드 기준 미구현. `Staff` 엔티티는 존재하지만 CRUD Controller/Service/DTO는 없음.
> 

### 6-1. 직원 생성 [미구현]

```
POST /api/v1/shops/{shopId}/staff
```

- 인증: `@RequireAuthenticatedUser`

**Request Body (안):**

```json
{
  "name": "string"
  // 직원명
}
```

**Response Body (201, 안):**

```json
{
  "staffId": 1,
  "shopId": 1,
  "name": "string"
}
```

---

### 6-2. 직원 목록 조회 [미구현]

```
GET /api/v1/shops/{shopId}/staff
```

- 인증: `@RequireAuthenticatedUser` 또는 공개(고객용)
- 고객 예약 플로우에서도 직원 선택에 사용

**Response Body (200, 안):**

```json
[
  {
    "staffId": 1,
    "shopId": 1,
    "name": "string"
  }
]
```

---

### 6-3. 직원 상세 조회 [미구현]

```
GET /api/v1/shops/{shopId}/staff/{staffId}
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200, 안):** 6-2 단건과 동일

---

### 6-4. 직원 수정 [미구현]

```
PATCH /api/v1/shops/{shopId}/staff/{staffId}
```

- 인증: `@RequireAuthenticatedUser`

**Request Body (안):**

```json
{
  "name": "string"
}
```

**Response:** `200 OK`

---

### 6-5. 직원 삭제 [미구현]

```
DELETE /api/v1/shops/{shopId}/staff/{staffId}
```

- 인증: `@RequireAuthenticatedUser`

**Response:** `204 No Content`

---

## 7. 직원 운영시간 (Staff Operating Times)

### 7-1. 직원 운영시간 조회 [존재]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/operating-times
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):**

```json
{
  "overrides": [
    {
      "dayOfWeek": "MONDAY",
      "isOff": false,
      "start": "09:00",
      "end": "18:00"
    },
    {
      "dayOfWeek": "SUNDAY",
      "isOff": true,
      "start": null,
      "end": null
    }
  ]
}
```

---

### 7-2. 직원 운영시간 설정 [존재]

```
PUT /api/v1/shops/{shopId}/staff/{staffId}/operating-times
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:**

```json
{
  "overrides": [
    {
      "dayOfWeek": "MONDAY",
      "isOff": false,
      "start": "09:00",
      "end": "18:00"
    },
    {
      "dayOfWeek": "SUNDAY",
      "isOff": true,
      "start": null,
      "end": null
    }
  ]
}
```

> “매장 기본 운영시간과 동일” 체크 시, 프론트에서 매장 운영시간을 그대로 overrides에 넣어 호출하는 방식.
> 

**Response:** `200 OK` (body 없음)

---

## 8. 직원 휴무일 (Staff Holiday)

> 현재 코드 기준 미구현. `StaffHoliday` 관련 엔티티/Repository/Controller가 없음.
> 

### 8-1. 직원 휴무일 목록 조회 [미구현]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/holidays
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200, 안):**

```json
{
  "holidays": [
    {
      "holidayId": 1,
      "holidayType": "WEEKLY | BIWEEKLY | MONTHLY | CUSTOM",
      "dayOfWeek": "SUNDAY",
      "weekOfMonth": null,
      "referenceDate": null,
      "specificDate": null
    }
  ]
}
```

> `ShopHolidayResponse`와 동일한 구조 재활용 가능.
> 

---

### 8-2. 직원 휴무일 생성 [미구현]

```
POST /api/v1/shops/{shopId}/staff/{staffId}/holidays
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:** `CreateShopHolidayRequest`와 동일한 구조

```json
{
  "holidayType": "WEEKLY",
  "dayOfWeek": "SUNDAY"
}
```

**Response Body (201):** `StaffHolidayResponse`

---

### 8-3. 직원 휴무일 삭제 [미구현]

```
DELETE /api/v1/shops/{shopId}/staff/{staffId}/holidays/{holidayId}
```

- 인증: `@RequireAuthenticatedUser`

**Response:** `204 No Content`

---

### 8-4. 직원 공휴일 휴무 설정 [미구현]

```
PUT /api/v1/shops/{shopId}/staff/{staffId}/settings
```

- 인증: `@RequireAuthenticatedUser`

**Request Body (안):**

```json
{
  "publicHolidayOff": true
}
```

**Response (안):**

```json
{
  "staffId": 1,
  "publicHolidayOff": true
}
```

---

## 9. 태그 관리 (Tag)

### 9-1. 전역 태그 생성 [존재, Deprecated]

```
POST /api/tags
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- Deprecated. 새 연동에서는 사용 비권장
- 동일 이름 존재 시 기존 태그 반환 (createOrGet)

**Request Body:**

```json
{
  "name": "string"
  // 필수 (NotBlank)
}
```

**Response Body (201):**

```json
{
  "id": 1,
  "name": "네일"
}
```

---

### 9-2. 전역 태그 전체 조회 [존재, Deprecated]

```
GET /api/tags
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- Deprecated. 새 연동에서는 사용 비권장

**Response Body (200):**

```json
[
  {
    "id": 1,
    "name": "네일"
  },
  {
    "id": 2,
    "name": "속눈썹"
  }
]
```

---

### 9-3. 매장 로컬 태그 생성 [존재]

```
POST /api/shops/{shopId}/tags
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 점주만 호출 가능
- 같은 매장에 같은 이름이 이미 있으면 `409 Conflict`

**Request Body:**

```json
{
  "name": "string"
}
```

**Response Body (201):**

```json
{
  "id": 101,
  "name": "손관리"
}
```

**주요 에러:**

- `403 Forbidden` / `SHOP_OWNER_MISMATCH`
- `409 Conflict` / `SHOP_TAG_ALREADY_EXISTS`

---

### 9-4. 매장별 태그 조회 [존재]

```
GET /api/shops/{shopId}/tags
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 해당 매장의 활성 메뉴에 연결된 visible 태그만 반환
- 중복 제거 후 `shop_tags.sort_order` 순으로 반환

**Response Body (200):**

```json
[
  {
    "id": 101,
    "name": "손관리"
  },
  {
    "id": 102,
    "name": "발관리"
  }
]
```

---

### 9-5. 매장별 태그 관리 목록 조회 [존재]

```
GET /api/shops/{shopId}/tags/manage
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 점주만 호출 가능
- 활성 메뉴 연결 여부와 관계없이 해당 매장의 모든 로컬 태그를 반환
- `shop_tags.sort_order` 순으로 반환
- 점주 메뉴/카테고리 관리 화면에서는 이 API 사용 권장

**Response Body (200):**

```json
[
  {
    "id": 101,
    "name": "손관리"
  },
  {
    "id": 102,
    "name": "발관리"
  }
]
```

**주요 에러:**

- `403 Forbidden` / `SHOP_OWNER_MISMATCH`

---

### 9-6. 매장 로컬 태그 이름 수정 [존재]

```
PATCH /api/shops/{shopId}/tags/{tagId}
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 점주만 호출 가능
- 매장 로컬 태그(`shop_tags`) 이름을 수정
- 같은 매장에 같은 이름이 이미 있으면 `409 Conflict`

**Request Body:**

```json
{
  "name": "손관리"
}
```

**Response Body (200):**

```json
{
  "id": 101,
  "name": "손관리"
}
```

**주요 에러:**

- `400 Bad Request` / `SHOP_TAG_MISMATCH`
- `403 Forbidden` / `SHOP_OWNER_MISMATCH`
- `409 Conflict` / `SHOP_TAG_ALREADY_EXISTS`

---

### 9-7. 매장 로컬 태그 삭제 [존재]

```
DELETE /api/shops/{shopId}/tags/{tagId}
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 점주만 호출 가능
- 해당 태그의 메뉴 연결(`shop_menu_tags.shop_tag_id`)을 먼저 제거한 뒤 `shop_tags` 행을 삭제
- 현재 스키마에는 태그용 `deleted_at`/`is_visible` 컬럼이 없어 물리 삭제로 처리

**Response:** `200 OK`

**주요 에러:**

- `400 Bad Request` / `SHOP_TAG_MISMATCH`
- `403 Forbidden` / `SHOP_OWNER_MISMATCH`

---

### 9-8. 매장 태그 정렬 변경 [존재]

```
PUT /api/shops/{shopId}/tags/order
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 점주만 호출 가능
- visible 태그 전체를 순서대로 전달해야 함

**Request Body:**

```json
{
  "tagIds": [102, 101, 103]
}
```

**Response:** `200 OK`

**주요 에러:**

- `403 Forbidden` / `SHOP_OWNER_MISMATCH`
- `400 Bad Request` / `INVALID_SHOP_TAG_ORDER`

---

### 9-9. 메뉴에 태그 연결 [존재]

```
POST /api/shops/{shopId}/menus/{menuId}/tags
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 현재 `tagId`는 과도기적으로 아래 둘 다 허용
    - legacy 전역 `tags.id`
    - 매장 로컬 `shop_tags.id`
- 새 연동에서는 `GET /api/shops/{shopId}/tags` 응답의 `id` 사용 권장

**Request Body:**

```json
{
  "tagId": 101
  // 필수 (NotNull)
}
```

**Response:** `200 OK`

---

### 9-10. 메뉴에서 태그 제거 [존재]

```
DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 현재 `tagId` path variable도 과도기적으로 아래 둘 다 허용
    - legacy 전역 `tags.id`
    - 매장 로컬 `shop_tags.id`

**Response:** `200 OK`

---

## 10. 메뉴 관리 (Menu)

### 10-1. 메뉴 생성 [존재]

```
POST /api/shops/{shopId}/menus
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Request Body:**

```json
{
  "name": "string",
  // 필수 (NotBlank)
  "description": "string",
  // 선택
  "sortOrder": 1
  // 정렬 순서
}
```

> **[수정 필요]** User Flow 기준 `price`(가격) 필드 없음.
> 

**Response Body (201):**

```json
{
  "id": 1,
  "shopId": 1,
  "name": "젤네일",
  "description": "기본 젤네일 시술",
  "isActive": true,
  "sortOrder": 1,
  "tags": []
}
```

---

### 10-2. 메뉴 목록 조회 [존재]

```
GET /api/shops/{shopId}/menus
GET /api/shops/{shopId}/menus?tagIds=1,2
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- `tagIds` 파라미터로 태그 필터링 가능
- 새 연동에서는 `GET /api/shops/{shopId}/tags` 응답의 `id` 기준 사용 권장
- 과도기 동안 legacy `tags.id`도 일부 허용
- 응답의 `tags[].id`는 매장 로컬 태그 `shop_tags.id` 기준

**Response Body (200):**

```json
[
  {
    "id": 1,
    "shopId": 1,
    "name": "젤네일",
    "description": "기본 젤네일 시술",
    "isActive": true,
    "sortOrder": 1,
    "tags": [
      {
        "id": 101,
        "name": "손관리"
      }
    ]
  }
]
```

---

### 10-3. 메뉴 수정 [존재]

```
PATCH /api/shops/{shopId}/menus/{menuId}
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Request Body:** (null이면 미변경)

```json
{
  "name": "string",
  "description": "string",
  "sortOrder": 2
}
```

**Response Body (200):** `ShopMenuResponse`와 동일

---

### 10-4. 메뉴 비활성화 (soft delete) [존재]

```
DELETE /api/shops/{shopId}/menus/{menuId}
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Response:** `200 OK`

---

## 11. 메뉴 입력 필드 (Menu Input Field)

### 11-1. 입력 필드 생성 [존재]

```
POST /api/shops/{shopId}/menus/{menuId}/input-fields
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Request Body:**

```json
{
  "label": "string",
  // 필수 (NotBlank)
  "inputType": "TEXT | NUMBER",
  // 필수 (NotNull)
  "required": true,
  // 필수 (NotNull)
  "minValue": 0,
  // NUMBER 전용 (BigDecimal)
  "maxValue": 10,
  // NUMBER 전용
  "stepValue": 1,
  // NUMBER 전용
  "maxLength": 100,
  // TEXT 전용
  "placeholder": "입력해주세요",
  // 선택
  "sortOrder": 1
  // 정렬 순서
}
```

> `inputType`에 따라 서버가 관련 없는 필드를 자동 무시:
- `NUMBER`: `maxLength` 무시
- `TEXT`: `minValue`, `maxValue`, `stepValue` 무시
> 

**Response Body (201):**

```json
{
  "id": 1,
  "shopMenuId": 1,
  "label": "연장 개수",
  "inputType": "NUMBER",
  "required": true,
  "minValue": 0,
  "maxValue": 10,
  "stepValue": 1,
  "maxLength": null,
  "placeholder": "개수를 입력하세요",
  "sortOrder": 1,
  "isActive": true
}
```

---

### 11-2. 입력 필드 목록 조회 [존재]

```
GET /api/shops/{shopId}/menus/{menuId}/input-fields
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Response Body (200):** `InputFieldResponse` 배열

---

### 11-3. 입력 필드 수정 [존재]

```
PATCH /api/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Request Body:** (null이면 미변경, `inputType`에 따라 적용)

```json
{
  "label": "string",
  "minValue": 0,
  "maxValue": 20,
  "stepValue": 1,
  "maxLength": null,
  "placeholder": "string",
  "sortOrder": 2
}
```

**Response Body (200):** `InputFieldResponse`와 동일

---

### 11-4. 입력 필드 비활성화 [존재]

```
DELETE /api/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Response:** `200 OK`

---

## 12. 카테고리/태그 용어 매핑

- 카테고리는 태그와 동일 개념으로 사용한다.
- 별도 Category 엔티티/API(`.../categories`)는 도입하지 않는다.
- 카테고리 관련 동작은 기존 태그 API를 사용한다.
    - 생성: `POST /api/shops/{shopId}/tags`
    - 목록: `GET /api/shops/{shopId}/tags`
    - 관리 목록: `GET /api/shops/{shopId}/tags/manage`
    - 이름 수정: `PATCH /api/shops/{shopId}/tags/{tagId}`
    - 삭제: `DELETE /api/shops/{shopId}/tags/{tagId}`
    - 정렬: `PUT /api/shops/{shopId}/tags/order`
    - 메뉴 연결: `POST /api/shops/{shopId}/menus/{menuId}/tags`
    - 메뉴 해제: `DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}`

---

## 13. 예약 가용성 조회 (Availability)

> 권장 진입 순서:
> `GET /api/public/shops/{slugOrCode}/booking-entry` 또는
> `GET /api/v1/shops/{shopId}/booking-entry`로 기본 직원 목록과 `defaultStaffId`를 먼저 조회한 뒤
> `GET /api/v1/shops/{shopId}/staff/{staffId}/availability...` 계열을 호출한다.
> 

### 13-1. 월별 예약 가능 날짜 조회 [존재]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly?yearMonth=2026-03
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `yearMonth` | `yyyy-MM` | Y | 조회 대상 연월 |

**Response Body (200):**

```json
{
  "yearMonth": "2026-03",
  "availableDates": [
    1,
    2,
    3,
    5,
    6
  ],
  // 예약 가능한 일자
  "holidayDates": [
    7,
    14,
    21,
    28
  ],
  // 공휴일 (빨간색 표시)
  "closedDates": [
    8,
    15,
    22,
    29
  ]
  // 휴무일 (회색 표시)
}
```

> 프론트 매핑:
- `availableDates` -> 기본 색상 (선택 가능)
- `holidayDates` -> 빨간색 (선택 불가)
- `closedDates` -> 회색 (선택 불가)
> 

---

### 13-2. 일별 예약 가능 슬롯 조회 [존재]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability?date=2026-03-05
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Query Parameters:**

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `yyyy-MM-dd` | Y | 조회 대상 날짜 |

**Response Body (200):**

```json
{
  "date": "2026-03-05",
  "slots": [
    "09:00",
    "09:30",
    "10:00",
    "10:30",
    "11:00"
  ],
  "holiday": false
}
```

> **참고**: 현재 available 슬롯만 반환. 점유된 슬롯을 “회색”으로 표시하려면 전체 슬롯 + 점유 여부 플래그 응답 확장 검토 필요.
> 

---

## 14. 예약 (Reservation)

### 14-1. 예약 생성 [존재]

```
POST /api/reservations
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:**

```json
{
  "shopId": 1,
  "staffId": 1,
  "date": "2026-03-05",
  "time": "10:00",
  "requirements": "길이 짧게 해주세요",
  "imageUrls": [
    "https://..."
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

> `staffId`, `date`, `time`은 필수이며, `time`은 10분 단위만 허용.
요청 본문은 `HH:mm` 형식의 `time`을 받지만, 응답 직렬화 시 `time`은 `HH:mm:ss`로 내려간다.
레거시 `formData`는 현재 계약에 포함되지 않으며, `formData`만 보내는 요청은 4xx로 실패한다.
생성 응답은 표준 필드 `requirements`, `imageUrls`, `imageCount`와 레거시 필드 `requests`, `photoUrls`, `photoCount`를 함께 반환한다.
> 

**Response Body (201):**

```json
{
  "id": 1,
  "date": "2026-03-05",
  "time": "10:00:00",
  "status": "PENDING",
  "customerName": "string",
  "imageCount": 2,
  "imageUrls": [
    "https://..."
  ],
  "requirements": "길이 짧게 해주세요",
  "photoCount": 2,
  "photoUrls": [
    "https://..."
  ],
  "requests": "길이 짧게 해주세요"
}
```

**주요 에러 코드:**

- `400 BAD_REQUEST`
  - `REQUIRED_DATE_MISSING`
  - `REQUIRED_TIME_MISSING`
  - `REQUIRED_STAFF_ID_MISSING`
  - `INVALID_TIME_INTERVAL`
  - `STAFF_NOT_IN_SHOP`
- `404 NOT_FOUND`
  - `STAFF_NOT_FOUND`
- `400 BAD_REQUEST`
  - 잘못된 `time` 형식은 `INVALID_PARAMETER`로 매핑될 수 있다.

---

### 14-2. 예약 상세 조회 [존재]

```
GET /api/reservations/{id}
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):**

```json
{
  "id": 1,
  "status": "PENDING | CONFIRMED | CANCELED | REJECTED",
  "date": "2026-03-05",
  "time": "10:00:00",
  "createdAt": "2026-03-04T15:30:00",
  "shopId": 1,
  "shopName": "네일샵",
  "customerName": "홍길동",
  "customerPhone": "010-1234-5678",
  "rejectionReason": null,
  "confirmationMessage": null,
  "requirements": "길이 짧게 해주세요",
  "imageUrls": [
    "https://..."
  ],
  "imageCount": 2,
  "photoUrls": [
    "https://..."
  ],
  "photoCount": 2,
  "menus": [
    {
      "shopMenuId": 1,
      "menuNameSnapshot": "젤네일",
      "priceSnapshot": null,
      "sortOrder": 0,
      "inputValues": [
        {
          "fieldLabelSnapshot": "연장 개수",
          "inputTypeSnapshot": "NUMBER",
          "valueNumber": 2,
          "valueText": null
        }
      ]
    }
  ]
}
```

> 예약 상세/목록 응답에서는 `formData` 파생 필드(`part`, `removal`, `extend`, `wrapping`)를 더 이상 내려주지 않는다.
상세/목록 응답은 `requirements`, `imageUrls`, `imageCount`를 포함하고, 레거시 `photoUrls`, `photoCount`도 함께 유지한다.
> 

---

### 14-3. 예약 확정 (점주) [존재]

```
PUT /api/reservations/{id}/confirm
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:**

```json
{
  "message": "string",
  // 필수 (NotBlank) - 전달 메시지
  "durationMinutes": 60,
  // 필수 (NotNull, 10분 단위)
  "startAt": "10:30"
  // 선택 - 시간 변경 시 (HH:mm)
}
```

**Response Body (200):**

```json
{
  "status": "CONFIRMED",
  "customerName": "홍길동",
  "date": "2026-03-05",
  "time": "10:00",
  "message": "내일 10시에 방문해주세요",
  "rejectReason": null
}
```

---

### 14-4. 예약 거절 (점주) [존재]

```
PUT /api/reservations/{id}/reject
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:**

```json
{
  "reason": "string"
  // 필수 (NotBlank) - 거절 사유
}
```

**Response Body (200):**

```json
{
  "status": "REJECTED",
  "customerName": "홍길동",
  "date": "2026-03-05",
  "time": "10:00",
  "message": null,
  "rejectReason": "해당 시간에 예약이 어렵습니다"
}
```

---

### 14-5. 고객 내 예약 목록 [존재]

```
GET /api/reservations/my
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):** `ReservationCustomerResponse[]`

---

### 14-6. 점주 매장 예약 목록 [존재]

```
GET /api/reservations/shop
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):** `ReservationOwnerResponse[]`

---

### 14-7. 채팅방 내 고객 예약 내역 [존재]

```
GET /api/reservations/chat/customer?shopId={shopId}
```

- 인증: `@RequireAuthenticatedUser`

---

### 14-8. 채팅방 내 점주 예약 내역 [존재]

```
GET /api/reservations/chat/owner?shopId={shopId}&customerId={customerId}
```

- 인증: `@RequireAuthenticatedUser`

---

### 14-9. 샵 예약 가능 시간 조회 [존재, Deprecated]

```
GET /api/reservations/shop/{shopId}/availability?date=2026-03-05
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- Deprecated. 새 연동에서는 사용하지 않는다.
- 권장 대체 흐름:
  `booking-entry` 조회 -> `defaultStaffId` 또는 선택한 `staffId` 확보 ->
  `GET /api/v1/shops/{shopId}/staff/{staffId}/availability`
- `date` 미전달 시 서버의 오늘 날짜 기준 조회

**Response Body (200):**

```json
{
  "date": "2026-03-05",
  "bookedTimes": [
    "10:00",
    "11:30"
  ]
}
```

---

## 15. 채팅 (Chat)

### 15-1. 채팅방 조회/생성 [존재]

```
GET /chat/rooms/shop/{shopId}
```

- 인증: `@RequireAuthenticatedUser`
- 채팅방이 없으면 자동 생성

---

### 15-2. 내 채팅방 목록 [존재]

```
GET /chat/rooms/
```

- 인증: `@RequireAuthenticatedUser`

---

### 15-3. 마지막 읽은 메시지 업데이트 [존재]

```
PATCH /chat/rooms/{chatRoomId}/last-read-message
```

- 인증: `@RequireAuthenticatedUser`

**Request Body:**

```json
{
  "lastReadMessageId": 123
}
```

---

### 15-4. 메시지 히스토리 조회 [존재]

```
GET /chat/rooms/{chatRoomId}/messages?cursor={messageId}&size=50
```

- 인증: `@RequireAuthenticatedUser`
- `cursor`는 선택, `size` 기본값은 `50`

**Response Body (200):** `MessageResponse[]`

```json
[
  {
    "messageType": "TEXT",
    "messageId": 1001,
    "senderId": 7,
    "senderName": "홍길동",
    "message": "안녕하세요",
    "imageUrl": null,
    "sentAt": "2026-03-05T10:00:00",
    "roomId": 33,
    "reservationId": null
  }
]
```

---

## 16. 파일 업로드 (File)

### 16-1. 단일 이미지 업로드 [존재]

```
POST /api/files/upload
Content-Type: multipart/form-data
```

- 인증: `@RequireAuthenticatedUser`

**Request:** `file` (MultipartFile)

**Response Body (200):**

```json
{
  "fileUrl": "https://s3.amazonaws.com/...",
  "message": "업로드 성공"
}
```

---

### 16-2. 다중 이미지 업로드 [존재]

```
POST /api/files/upload-multiple
Content-Type: multipart/form-data
```

- 인증: `@RequireAuthenticatedUser`

**Request:** `files` (MultipartFile[])

**Response Body (200):**

```json
[
  {
    "fileUrl": "https://s3.amazonaws.com/...",
    "message": "업로드 성공"
  },
  {
    "fileUrl": "https://s3.amazonaws.com/...",
    "message": "업로드 성공"
  }
]
```

---

## 17. 공유 링크 (Link)

### 17-1. 단축 링크 리다이렉트 [존재]

```
GET /s/{slugOrCode}
```

- 인증: 없음 (공개, `allowUrls`)
- `302 Found` -> 프론트엔드 URL로 리다이렉트

---

### 17-2. 링크로 채팅방 열기 [존재]

```
GET /link/chat/{slugOrCode}
```

- 인증: `@RequireAuthenticatedUser`

**Response Body (200):** `ChatRoomResponse`

---

## 18. 예약 진입 (Booking Entry)

### 18-1. 공개 예약 진입 조회 [존재]

```
GET /api/public/shops/{slugOrCode}/booking-entry
```

- 인증: 없음 (공개, `allowUrls`)
- `slugOrCode`는 먼저 `slug`로 조회하고, 없으면 `publicCode`로 fallback 조회
- 링크 랜딩 페이지, 공개 예약 진입 화면에서 먼저 호출하는 엔드포인트

**Response Body (200):**

```json
{
  "shopId": 1,
  "shopName": "스냅북 네일",
  "defaultStaffId": 11,
  "staffs": [
    {
      "staffId": 11,
      "name": "민지"
    },
    {
      "staffId": 12,
      "name": "수연"
    }
  ]
}
```

> 참고:
> `defaultStaffId`는 현재 응답의 첫 번째 직원(`staffs[0].staffId`)이다.
> 프론트는 이 값을 기본 선택으로 사용하고, 이후 `staffId` 기반 가용성 조회 API를 호출한다.
> 

---

### 18-2. 인증 예약 진입 조회 [존재]

```
GET /api/v1/shops/{shopId}/booking-entry
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 로그인 이후 내부 진입 화면에서 이미 `shopId`를 알고 있을 때 사용하는 엔드포인트
- 현재 응답 구조는 공개 예약 진입 조회와 동일

**Response Body (200):**

```json
{
  "shopId": 1,
  "shopName": "스냅북 네일",
  "defaultStaffId": 11,
  "staffs": [
    {
      "staffId": 11,
      "name": "민지"
    },
    {
      "staffId": 12,
      "name": "수연"
    }
  ]
}
```

> 참고:
> 이 엔드포인트는 JWT가 있어야 호출되지만,
> 현재 응답 내용 자체는 호출 사용자별로 달라지지 않는다.
> 

## Enum 참조

### ScheduleType

| 값 | 설명 |
| --- | --- |
| `DAILY` | 매일 같은 운영시간 |
| `WEEKDAY_WEEKEND` | 평일/주말 구분 |
| `BY_DAY` | 요일별 개별 설정 |

### HolidayType

| 값 | 설명 |
| --- | --- |
| `WEEKLY` | 매주 (특정 요일) |
| `BIWEEKLY` | 격주 (기준일 + 요일) |
| `MONTHLY` | 매달 n째주 (weekOfMonth + 요일) |
| `CUSTOM` | 특정 날짜 |

### InputType

| 값 | 설명 |
| --- | --- |
| `TEXT` | 텍스트 입력 |
| `NUMBER` | 수량 입력 |

### Reservation.Status

| 값 | 설명 |
| --- | --- |
| `PENDING` | 대기 (고객 신청) |
| `CONFIRMED` | 확정 (점주 수락) |
| `REJECTED` | 거절 (점주 거절) |
| `CANCELED` | 취소 |

### AuthStatus

| 값 | 설명 |
| --- | --- |
| `LOGIN_SUCCESS` | 기존 회원 로그인 성공 |
| `SIGNUP_REQUIRED` | 회원가입 필요 (임시 토큰 발급) |
