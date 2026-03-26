# 04-api-spec-by-flow

APP: 시작 전
BE: 시작 전

# User Flow별 API 명세 - #97 / #99 / #101 / #105 / #115 기준

> 작성일: 2026-02-15
이 문서는 User Flow 각 단계에서 호출하는 API를 명세한다.
기준은 현재 코드이며, 미구현 항목은 명시적으로 `[미구현]`으로 표기한다.
> 

---

## 인증 기준 (현재 구현)

- 기본 정책: `SecurityConfig`의 `anyRequest().authenticated()` 적용
- 공개는 `allowUrls`에 포함된 경로만 허용
- 공개 경로 예시: `/s/**`, `/api/public/shops/*/booking-entry`, `/oauth/login/kakao*`, `/dev/auth/**`
- 따라서 본 문서의 `/api/**`, `/api/v1/**` 호출은 별도 예외가 없는 한 JWT 인증이 필요

---

## 0. 로컬 개발용 인증 플로우 (#116)

> 이 플로우는 **로컬 UI 확인용**이다.
> 실제 카카오 로그인 대신 고정 persona를 사용한다.
> `local` / `test` 프로필에서만 사용할 수 있다.

### 0-1. persona 목록 조회

```
GET /dev/auth/personas
```

- 인증: 없음
- 목적: 프론트에서 테스트 로그인 버튼/목록을 렌더링할 때 사용

**Response (200):**

```json
[
  {
    "personaKey": "owner-1",
    "providerId": "dev-owner-1001",
    "userType": "OWNER",
    "description": "기존 원장 계정 확인용 persona",
    "signedUp": true
  },
  {
    "personaKey": "new-customer-1",
    "providerId": "dev-customer-new-4001",
    "userType": "CUSTOMER",
    "description": "회원가입 필요 고객 persona",
    "signedUp": false
  }
]
```

### 0-2. persona 로그인

```
POST /dev/auth/login
```

- 인증: 없음
- 목적: 실제 카카오 OAuth 없이 기존 로그인 후처리를 재사용

**Request:**

```json
{
  "personaKey": "owner-1"
}
```

**Response (200):**

```json
{
  "accessToken": "string",
  "refreshToken": "string | null",
  "userId": 1,
  "role": "USER | ADMIN",
  "message": "로그인 성공 | 회원가입 필요",
  "authStatus": "LOGIN_SUCCESS | SIGNUP_REQUIRED",
  "userType": "OWNER | CUSTOMER | null"
}
```

**프론트 처리 규칙:**

- `LOGIN_SUCCESS`면 기존 로그인 성공 후처리와 동일하게 토큰 저장 및 라우팅
- `SIGNUP_REQUIRED`면 `accessToken`에 담긴 임시 토큰으로 기존 회원가입 API 호출

### 0-3. 회원가입 필요 persona 처리

```
POST /user/owner/signup
POST /user/customer/signup
```

- 인증: `@RequireTempUser`
- `SIGNUP_REQUIRED` 응답에서 받은 임시 토큰을 그대로 사용
- 회원가입 API 계약 자체는 기존과 동일

### 0-4. persona reset

```
POST /dev/auth/reset/persona/{personaKey}
```

- 인증: 없음
- 목적: 반복 QA 전 상태 초기화

**Response (200):**

```json
{
  "personaKey": "owner-1",
  "deleted": true,
  "message": "persona user deleted"
}
```

### 0-5. 주의사항

- `/oauth/login/kakao/local`은 여전히 **실제 카카오 OAuth** 경로다.
- 예전 `loadtest_code_*` 우회는 더 이상 사용하지 않는다.
- `owner-1` 같은 기존 persona도 DB에 해당 `providerId` 사용자가 없으면 `SIGNUP_REQUIRED`로 떨어진다.
- 즉 `dev auth`는 로그인 계약 재사용용 경로이며, 기존 사용자/샵/예약 데이터를 자동 생성하지 않는다.
- 로그인 성공 상태나 샵/예약이 있는 화면을 바로 확인하려면 별도 seed/bootstrap 후속 작업이 필요하다.
- 부하테스트용 인증은 별도 후속 이슈로 분리한다.

---

## A. 점주 온보딩 플로우

---

### A-1. 매장 생성 -> 기본 Staff 자동 생성 (#97)

매장을 생성하면 기본 Staff 1명(점주)이 자동으로 생성된다. 별도 API 호출 불필요.

```
POST /shop
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#97** Phase 1-B

**Request:**

```json
{
  "businessName": "네일샵",
  // 필수
  "address": "서울시 강남구",
  "businessNumber": "123-45-67890"
}
```

**Response (200):**

```json
{
  "ownerId": 1,
  "shopId": 1,
  "businessName": "네일샵",
  "address": "서울시 강남구",
  "businessNumber": "123-45-67890"
}
```

**부수 효과:**

- `Staff(shopId=1, name=점주이름)` 자동 생성됨 (#97 1-B-1)
- `StaffReader.getDefaultStaffByShopId(shopId)`로 조회 가능 (#97 1-B-2)

---

### A-2. 운영 시간 설정 (#101)

### A-2-1. 운영시간 타입 + 시간 설정

```
PUT /api/v1/shops/{shopId}/schedule/operating-times
```

- 인증: `@RequireAuthenticatedUser` (매장 점주만)
- 이슈: **#101** Phase 14-3 ~ 14-5

**`scheduleType`에 따른 Request 구분:**

**매일 같음 (DAILY):**

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

> 서버가 월~일 7요일에 동일하게 복제 저장.
> 

**평일/주말 (WEEKDAY_WEEKEND):**

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

> 월~금 = weekdayTimes, 토~일 = weekendTimes로 저장.
> 

**요일별 다름 (BY_DAY):**

```json
{
  "scheduleType": "BY_DAY",
  "dayTimes": {
    "MONDAY": [
      {
        "start": "09:00",
        "end": "13:00"
      },
      {
        "start": "14:00",
        "end": "19:00"
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

> `start`/`end`는 `LocalTime` (HH:mm).
`end`는 “마지막 예약 가능 슬롯 시간” (inclusive).
복수 블록 = 휴게시간 표현 가능.
> 

**Response:** `200 OK` (body 없음)

---

### A-2-2. 운영시간 조회

```
GET /api/v1/shops/{shopId}/schedule/operating-times
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#101** Phase 14-6

**Response (200):**

```json
{
  "scheduleType": "BY_DAY",
  "dayTimes": {
    "MONDAY": [
      {
        "start": "09:00",
        "end": "13:00"
      },
      {
        "start": "14:00",
        "end": "19:00"
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

> 모든 타입에서 요일별로 정규화되어 반환.
> 

---

### A-2-3. 간격(interval) 설정

```
PUT /api/v1/shops/{shopId}/schedule/interval
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#101** Phase 14-2

**Request:**

```json
{
  "intervalMinutes": 30
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `intervalMinutes` | int | Y | 예약 슬롯 간격 (30 또는 60) |

**Response (200):**

```json
{
  "shopId": 1,
  "intervalMinutes": 30,
  "scheduleType": "BY_DAY",
  "bookingWindowDays": 30,
  "minBookingLeadMinutes": 60,
  "publicHolidayOff": false
}
```

> `bookingWindowDays`, `minBookingLeadMinutes`, `publicHolidayOff` 수정은 후속 `booking-policy` API로 분리 예정.
> 
> 
> ---
> 

### A-2-4. 스케줄 설정 조회

```
GET /api/v1/shops/{shopId}/schedule/settings
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#101** Phase 14-1

**Response (200):** A-2-3 Response와 동일

---

### A-3. 휴무일 설정 (#101)

### A-3-1. 정기 휴무일 생성

```
POST /api/v1/shops/{shopId}/schedule/holidays
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#101** Phase 16-2 ~ 16-5

**매주 (WEEKLY):**

```json
{
  "holidayType": "WEEKLY",
  "dayOfWeek": "SUNDAY"
}
```

**격주 (BIWEEKLY):**

```json
{
  "holidayType": "BIWEEKLY",
  "dayOfWeek": "SATURDAY",
  "referenceDate": "2026-02-14"
}
```

> `referenceDate`: 격주 기준 날짜. 이 날짜 기준 2주마다 해당 요일 휴무.
> 

**매달 n째주 (MONTHLY):**

```json
{
  "holidayType": "MONTHLY",
  "dayOfWeek": "MONDAY",
  "weekOfMonth": 2
}
```

> `weekOfMonth`: 1(첫째주) ~ 5(다섯째주)
> 

**특정 날짜 (CUSTOM):**

```json
{
  "holidayType": "CUSTOM",
  "specificDate": "2026-03-15"
}
```

**Response (201):**

```json
{
  "holidayId": 1,
  "holidayType": "WEEKLY",
  "dayOfWeek": "SUNDAY",
  "weekOfMonth": null,
  "referenceDate": null,
  "specificDate": null
}
```

---

### A-3-2. 공휴일 휴무 토글 [미구현]

- 현재 코드 기준 별도 `booking-policy` API는 없음.
- `publicHolidayOff` 값 확인은 `GET /api/v1/shops/{shopId}/schedule/settings` 응답에서 가능.

---

### A-3-3. 휴무일 목록 조회

```
GET /api/v1/shops/{shopId}/schedule/holidays
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#101** Phase 16-1

**Response (200):**

```json
{
  "holidays": [
    {
      "holidayId": 1,
      "holidayType": "WEEKLY",
      "dayOfWeek": "SUNDAY",
      "weekOfMonth": null,
      "referenceDate": null,
      "specificDate": null
    },
    {
      "holidayId": 2,
      "holidayType": "CUSTOM",
      "dayOfWeek": null,
      "weekOfMonth": null,
      "referenceDate": null,
      "specificDate": "2026-03-15"
    }
  ]
}
```

---

### A-3-4. 휴무일 삭제

```
DELETE /api/v1/shops/{shopId}/schedule/holidays/{holidayId}
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#101** Phase 16-6

**Response:** `204 No Content`

---

### A-4. 직원 운영 시간 설정 (#101)

### A-4-1. 직원 운영시간 설정 (오버라이드)

```
PUT /api/v1/shops/{shopId}/staff/{staffId}/operating-times
```

- 인증: `@RequireAuthenticatedUser` (매장 점주만)
- 이슈: **#101** Phase 15-1

**Request:**

```json
{
  "overrides": [
    {
      "dayOfWeek": "MONDAY",
      "isOff": false,
      "start": "10:00",
      "end": "17:00"
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

> `isOff: true` = 해당 요일 직원 휴무 (start/end는 null).
오버라이드 없는 요일은 매장 기본 운영시간을 따름.
**검증**: 매장 운영시간 범위를 초과하면 `400 Bad Request` (#101 15-3)
> 

**Response:** `200 OK` (body 없음)

> **“매장과 동일” 체크**: 프론트에서 매장 운영시간을 그대로 overrides에 넣어 호출.
> 

---

### A-4-2. 직원 운영시간 조회

```
GET /api/v1/shops/{shopId}/staff/{staffId}/operating-times
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#101** Phase 15-2

**Response (200):**

```json
{
  "overrides": [
    {
      "dayOfWeek": "MONDAY",
      "isOff": false,
      "start": "10:00",
      "end": "17:00"
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

### A-5. 태그 추가 (#99)

### A-5-1. 태그 생성

```
POST /api/tags
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 이슈: **#99** Phase 2-D-1

**Request:**

```json
{
  "name": "네일"
}
```

**Response (201):**

```json
{
  "id": 1,
  "name": "네일"
}
```

> 동일 이름 태그가 이미 존재하면 기존 태그 반환 (idempotent).
> 

---

### A-5-2. 전체 태그 조회

```
GET /api/tags
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 이슈: **#99** Phase 2-D-4

**Response (200):**

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

### A-6. 메뉴 추가 (#99)

### A-6-1. 메뉴 생성

```
POST /api/shops/{shopId}/menus
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 이슈: **#99** Phase 1-C-1

**Request:**

```json
{
  "name": "젤네일",
  "description": "기본 젤네일 시술",
  "sortOrder": 1
}
```

> 동일 shop에서 같은 이름 메뉴 중복 시 `409 Conflict`.
> 

**Response (201):**

```json
{
  "id": 1,
  "shopId": 1,
  "name": "젤네일",
  "description": "기본 젤네일 시술",
  "isActive": true,
  "sortOrder": 1
}
```

---

### A-6-2. 메뉴 목록 조회

```
GET /api/shops/{shopId}/menus
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 이슈: **#99** Phase 1-C-2

**Response (200):** `ShopMenuResponse[]` (활성 메뉴만, sort_order 순)

---

### A-6-3. 메뉴 수정

```
PATCH /api/shops/{shopId}/menus/{menuId}
```

- 이슈: **#99** Phase 1-C-3

**Request:** (null이면 미변경)

```json
{
  "name": "젤네일(수정)",
  "description": "업데이트",
  "sortOrder": 2
}
```

**Response (200):** `ShopMenuResponse`

---

### A-6-4. 메뉴 비활성화

```
DELETE /api/shops/{shopId}/menus/{menuId}
```

- 이슈: **#99** Phase 1-C-4
- soft delete (`is_active = false`)

**Response:** `200 OK`

---

### A-6-5. 메뉴에 태그 연결

```
POST /api/shops/{shopId}/menus/{menuId}/tags
```

- 이슈: **#99** Phase 2-D-2

**Request:**

```json
{
  "tagId": 1
}
```

**Response:** `200 OK`

---

### A-6-6. 메뉴에서 태그 제거

```
DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}
```

- 이슈: **#99** Phase 2-D-3

**Response:** `200 OK`

---

### A-6-7. 메뉴 입력 필드 생성

```
POST /api/shops/{shopId}/menus/{menuId}/input-fields
```

- 이슈: **#99** Phase 3-C-1

**Request:**

```json
{
  "label": "연장 개수",
  "inputType": "NUMBER",
  "required": true,
  "minValue": 1,
  "maxValue": 10,
  "stepValue": 1,
  "maxLength": null,
  "placeholder": "개수 입력",
  "sortOrder": 0
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `label` | String | Y | 필드 라벨 (메뉴 내 UNIQUE) |
| `inputType` | String | Y | `"NUMBER"` 또는 `"TEXT"` |
| `required` | Boolean | Y | 필수 여부 |
| `minValue` | BigDecimal | N | NUMBER 전용 - 최솟값 |
| `maxValue` | BigDecimal | N | NUMBER 전용 - 최댓값 |
| `stepValue` | BigDecimal | N | NUMBER 전용 - 증가 단위 (>0) |
| `maxLength` | Integer | N | TEXT 전용 - 최대 글자수 (>0) |
| `placeholder` | String | N | 힌트 텍스트 |
| `sortOrder` | int | Y | 정렬 순서 |

> 서버 자동 정합성: NUMBER 타입에 maxLength 전달 시 무시, TEXT 타입에 min/max/step 전달 시 무시 (#99 3-B-4, 3-B-5)
> 

**Response (201):**

```json
{
  "id": 1,
  "shopMenuId": 1,
  "label": "연장 개수",
  "inputType": "NUMBER",
  "required": true,
  "minValue": 1,
  "maxValue": 10,
  "stepValue": 1,
  "maxLength": null,
  "placeholder": "개수 입력",
  "sortOrder": 0,
  "isActive": true
}
```

---

### A-6-8. 메뉴 입력 필드 목록 조회

```
GET /api/shops/{shopId}/menus/{menuId}/input-fields
```

- 이슈: **#99** Phase 3-C-2

**Response (200):** `InputFieldResponse[]` (활성 필드만, sort_order 순)

---

### A-6-9. 메뉴 입력 필드 수정

```
PATCH /api/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}
```

- 이슈: **#99** Phase 3-C-3

**Request:** (null이면 미변경)

```json
{
  "label": "수정된 라벨",
  "minValue": 0,
  "maxValue": 20,
  "stepValue": 1,
  "maxLength": null,
  "placeholder": "수정된 힌트",
  "sortOrder": 1
}
```

**Response (200):** `InputFieldResponse`

---

### A-6-10. 메뉴 입력 필드 비활성화

```
DELETE /api/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}
```

- 이슈: **#99** Phase 3-C-4
- soft delete

**Response:** `200 OK`

---

## B. 고객 예약 플로우

---

### B-0. 예약 진입 정보 조회 (#115)

예약 시작 전에 `shopId`, `staffs`, `defaultStaffId`를 먼저 확보한다.
공유 링크 진입과 인증 사용자 진입은 경로만 다르고 응답 계약은 같다.

**공개 링크 진입:**

```
GET /api/public/shops/{slugOrCode}/booking-entry
```

- 인증: 없음 (공개, `allowUrls`)
- `slugOrCode`는 `slug` 우선, 미매칭 시 `publicCode` fallback

**인증 사용자 진입:**

```
GET /api/v1/shops/{shopId}/booking-entry
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)

**Response (200):**

```json
{
  "shopId": 1,
  "shopName": "네일샵",
  "defaultStaffId": 10,
  "staffs": [
    {
      "staffId": 10,
      "name": "원장"
    },
    {
      "staffId": 11,
      "name": "직원A"
    }
  ]
}
```

**프론트 사용 규칙:**

- 공유 링크 화면은 `GET /api/public/shops/{slugOrCode}/booking-entry`를 먼저 호출
- 인증 사용자 화면은 `GET /api/v1/shops/{shopId}/booking-entry`를 먼저 호출
- 이후 `defaultStaffId` 또는 사용자가 선택한 `staffId`로 B-1, B-2를 호출

---

### B-1. 날짜 선택 - 월별 캘린더 조회 (#101)

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly?yearMonth=2026-03
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 이슈: **#101** Phase 17-5 ~ 17-8
- 선행 조건: B-0 응답에서 확보한 `shopId`와 `defaultStaffId` 또는 선택한 `staffId` 사용

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `yearMonth` | `yyyy-MM` | Y | 조회 연월 |

**Response (200):**

```json
{
  "yearMonth": "2026-03",
  "availableDates": [
    1,
    2,
    3,
    5,
    6,
    7
  ],
  "holidayDates": [
    8,
    15,
    22,
    29
  ],
  "closedDates": [
    4,
    11,
    18,
    25
  ]
}
```

| 필드 | UI 표시 | 설명 |
| --- | --- | --- |
| `availableDates` | 기본 색상 (선택 가능) | 예약 가능한 날짜 |
| `holidayDates` | **빨간색** (선택 불가) | 공휴일 + 정기휴무 |
| `closedDates` | **회색** (선택 불가) | 운영시간 없음 / 직원 off |

**내부 로직 (#101):**

1. `HolidayChecker` - WEEKLY/BIWEEKLY/MONTHLY/CUSTOM 휴무 판정 + publicHolidayOff 판정
2. `OperatingTimeResolver` - 매장 기본 시간 + 직원 오버라이드 결합
3. `SlotGenerator` - 운영시간 블록을 interval 단위로 슬롯 생성
4. `AvailabilityService` - 점유 블록(ReservationTimeBlock) 제외 + bookingWindow/minBookingLead 적용

**에러:**

- `staffId` 존재하지 않음 -> `404 Not Found` (#101 17-6)

---

### B-2. 시간 선택 - 일별 슬롯 조회 (#101)

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability?date=2026-03-05
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 이슈: **#101** Phase 17-1 ~ 17-4
- 선행 조건: B-0에서 선택된 `staffId` 사용

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `yyyy-MM-dd` | Y | 조회 날짜 |

**Response (200):**

```json
{
  "date": "2026-03-05",
  "slots": [
    "09:00",
    "09:30",
    "10:00",
    "11:00",
    "11:30",
    "12:00",
    "12:30",
    "13:00"
  ],
  "holiday": false
}
```

> `slots`에는 예약 **가능한** 시간만 포함.
이미 확정된 예약의 점유 블록(ReservationTimeBlock)에 겹치는 슬롯은 제외됨 (#101 13-2).
당일 예약 시 `minBookingLeadMinutes` 미만 슬롯도 제외 (#101 13-5).
> 

**휴무일인 경우:**

```json
{
  "date": "2026-03-08",
  "slots": [],
  "holiday": true
}
```

**에러:**

- 예약 가능 기간(`bookingWindowDays`) 초과 -> `400 Bad Request` (#101 17-4)
- `staffId` 존재하지 않음 -> `404 Not Found` (#101 17-2)

---

### B-3. 메뉴 선택 (#99)

### B-3-1. 태그별 메뉴 조회(점주도 사용가능)

```
GET /api/shops/{shopId}/menus?tagIds=1
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 이슈: **#99** Phase 2-C-2

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `tagIds` | Long[] | N | 태그 ID (OR 필터). 미지정 시 전체 활성 메뉴 |

**Response (200):**

```json
[
  {
    "id": 1,
    "shopId": 1,
    "name": "젤네일",
    "description": "기본 젤네일 시술",
    "isActive": true,
    "sortOrder": 1
  },
  {
    "id": 3,
    "shopId": 1,
    "name": "젤아트",
    "description": "아트 시술",
    "isActive": true,
    "sortOrder": 3
  }
]
```

> 비활성 메뉴는 무조건 제외 (#99 2-C-1).
첫 진입 시 매장의 첫 번째 태그가 기본 선택 -> 프론트에서 처리.
> 

---

### B-3-2. 메뉴 입력 필드 조회

```
GET /api/shops/{shopId}/menus/{menuId}/input-fields
```

- 인증: JWT 필요 (`SecurityConfig` 전역 정책, `allowUrls` 제외)
- 이슈: **#99** Phase 3-C-2

**Response (200):**

```json
[
  {
    "id": 1,
    "shopMenuId": 1,
    "label": "연장 개수",
    "inputType": "NUMBER",
    "required": true,
    "minValue": 1,
    "maxValue": 10,
    "stepValue": 1,
    "maxLength": null,
    "placeholder": "개수 입력",
    "sortOrder": 0,
    "isActive": true
  },
  {
    "id": 2,
    "shopMenuId": 1,
    "label": "요청사항",
    "inputType": "TEXT",
    "required": false,
    "minValue": null,
    "maxValue": null,
    "stepValue": null,
    "maxLength": 200,
    "placeholder": "요청사항을 입력하세요",
    "sortOrder": 1,
    "isActive": true
  }
]
```

---

### B-4. 예약 생성 (#97 + #99 + #105)

```
POST /api/reservations
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#97** Phase 1-D (staffId, 시간 검증) + **#99** Phase 4-C, 4-D (메뉴 선택 + 입력값) + **#105** (예약 생성 요청 스키마 전환)

**Request:**

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
    },
    {
      "menuId": 3,
      "inputValues": []
    }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 | 이슈 |
| --- | --- | --- | --- | --- |
| `shopId` | Long | Y | 매장 ID | 기존 |
| `staffId` | Long | Y | 담당 직원 ID (고객 선택) | **#97** |
| `date` | String | Y | 예약 날짜 (`yyyy-MM-dd`) | **#105** |
| `time` | String | Y | 예약 시간 (`HH:mm`) | **#105** |
| `requirements` | String | N | 고객 요청사항 | **#105** |
| `imageUrls` | List | N | 참고 이미지 URL 목록 | **#105** |
| `menuSelections` | List | N | 메뉴 선택 목록 | **#99** |
| `menuSelections[].menuId` | Long | Y | 선택한 메뉴 ID | **#99** |
| `menuSelections[].inputValues` | List | N | 해당 메뉴의 입력값 | **#99** |
| `inputValues[].fieldId` | Long | Y | 입력 필드 ID | **#99** |
| `inputValues[].valueNumber` | BigDecimal | N | NUMBER 타입 값 | **#99** |
| `inputValues[].valueText` | String | N | TEXT 타입 값 | **#99** |

**서버 검증 (#97):**

- `staffId`가 해당 `shopId` 소속인지 검증 (#97 1-D-2)
- `date`, `time` 누락 시 4xx 검증 에러
- `time`이 10분 단위인지 검증 (#97 1-D-3)
- `startAt = LocalDateTime.of(date, time)` 자동 계산 저장 (#97 1-D-1)

**서버 검증 (#105):**

- 레거시 payload 기반 요청은 지원하지 않음 (4xx)

**서버 검증 (#99):**

- 메뉴가 해당 shop 소속인지 검증 (#99 4-C-3)
- 비활성 메뉴 선택 불가 (#99 4-C-4)
- required 입력 필드 값 누락 시 에러 (#99 4-D-2)
- NUMBER 타입: min/max 범위, step 단위 검증 (#99 4-D-3, 4-D-4)
- TEXT 타입: maxLength 검증 (#99 4-D-5)
- NUMBER 필드에 valueText만 / TEXT 필드에 valueNumber만 -> 에러 (#99 4-D-6)
- 해당 메뉴에 정의되지 않은 fieldId -> 에러 (#99 4-D-7)

**부수 효과:**

- `reservation_menu_items` 저장 (메뉴명/설명 스냅샷 포함) (#99 4-C-5)
- `reservation_menu_input_values` 저장 (label/type 스냅샷 포함) (#99 4-D-1)
- 저장 실패 시 전체 트랜잭션 롤백 (#99 4-E-2)

**Response (201):**

```json
{
  "id": 1,
  "date": "2026-03-05",
  "time": "10:00",
  "status": "PENDING",
  "customerName": "홍길동",
  "photoCount": 2,
  "photoUrls": [
    "https://..."
  ],
  "requests": "길이 짧게 해주세요"
}
```

---

### B-5. 예약 상세 조회 (#99)

```
GET /api/reservations/{id}
```

- 인증: `@RequireAuthenticatedUser`
- 이슈: **#99** Phase 5-A-1 ~ 5-A-3

**Response (200):**

```json
{
  "id": 1,
  "status": "PENDING",
  "date": "2026-03-05",
  "time": "10:00",
  "createdAt": "2026-03-04T15:30:00",
  "shopId": 1,
  "shopName": "네일샵",
  "customerName": "홍길동",
  "customerPhone": "010-1234-5678",
  "rejectionReason": null,
  "confirmationMessage": null,
  "photoUrls": [
    "https://..."
  ],
  "photoCount": 1,
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
    },
    {
      "shopMenuId": 3,
      "menuNameSnapshot": "젤아트",
      "priceSnapshot": null,
      "sortOrder": 1,
      "inputValues": []
    }
  ]
}
```

> `menus`가 비어있으면 빈 배열로 반환된다.
스냅샷 데이터이므로 원본 메뉴 변경과 무관하게 예약 시점 정보 보존 (#99 4-C-5).
> 

---

### B-6. 예약 확정 - 점주 (#97)

```
PUT /api/reservations/{id}/confirm
```

- 인증: `@RequireAuthenticatedUser` (점주)
- 이슈: **#97** Phase 1-F

**Request:**

```json
{
  "message": "내일 10시에 방문해주세요",
  "durationMinutes": 60,
  "startAt": "10:30"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `message` | String | Y (NotBlank) | 고객에게 전달할 메시지 |
| `durationMinutes` | Integer | Y (NotNull) | 시술 시간 (10분 단위) |
| `startAt` | LocalTime (HH:mm) | N | 시간 변경 시 (reschedule). null이면 기존 시간 유지 |

**서버 검증 (#97):**

- `durationMinutes`가 10분 단위인지 검증 (#97 1-F-1a)
- `startAt` 제공 시 reschedule: 예약의 `time`, `startAt` 모두 갱신 (#97 1-F-6)

**부수 효과:**

- `ReservationTimeBlock` 10분 단위로 생성 (#97 1-F-4)
    - 예: startAt=10:00, duration=60분 -> 6개 블록 (10:00, 10:10, 10:20, 10:30, 10:40, 10:50)
- 동일 Staff + 동일 시간대 점유 존재 시 UNIQUE 충돌 -> 트랜잭션 롤백 (#97 1-F-5)
    - 에러 메시지: “해당 시간대는 이미 예약되어 있습니다. 다른 시간을 선택해주세요.”
- 상태: `PENDING` -> `CONFIRMED`

**Response (200):**

```json
{
  "status": "CONFIRMED",
  "customerName": "홍길동",
  "date": "2026-03-05",
  "time": "10:30",
  "message": "내일 10시에 방문해주세요",
  "rejectReason": null
}
```

---

### B-7. 예약 거절 - 점주 (#97)

```
PUT /api/reservations/{id}/reject
```

- 인증: `@RequireAuthenticatedUser` (점주)

**Request:**

```json
{
  "reason": "해당 시간에 예약이 어렵습니다"
}
```

**Response (200):**

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

## 이슈별 구현 범위 요약

### #97 - ERD v2 코드 전환

| User Flow 단계 | API | 핵심 기능 |
| --- | --- | --- |
| A-1. 매장 생성 | `POST /shop` | 기본 Staff 자동 생성 |
| B-4. 예약 생성 | `POST /api/reservations` | staffId 선택, startAt 자동 계산, 10분 단위 검증 |
| B-6. 예약 확정 | `PUT /api/reservations/{id}/confirm` | durationMinutes, ReservationTimeBlock 생성, 겹침 방지, reschedule |

### #99 - 메뉴 / 태그 / 입력 필드

| User Flow 단계 | API | 핵심 기능 |
| --- | --- | --- |
| A-5. 태그 추가 | `POST /api/tags`, `GET /api/tags` | 태그 CRUD |
| A-6. 메뉴 추가 | `POST/GET/PATCH/DELETE /api/shops/{shopId}/menus` | 메뉴 CRUD |
| A-6. 메뉴-태그 연결 | `POST/DELETE /api/shops/{shopId}/menus/{menuId}/tags` | 태그 연결/해제 |
| A-6. 입력 필드 | `POST/GET/PATCH/DELETE /api/shops/{shopId}/menus/{menuId}/input-fields` | 입력 필드 CRUD |
| B-3. 메뉴 선택 | `GET /api/shops/{shopId}/menus?tagIds=` | 태그 필터링 조회 |
| B-4. 예약 생성 | `POST /api/reservations` (menuSelections) | 메뉴 다중 선택 + 입력값 저장 |
| B-5. 예약 조회 | `GET /api/reservations/{id}` | menus + inputValues 포함 응답 |

### #101 - 예약 가능 시간 관리

| User Flow 단계 | API | 핵심 기능 |
| --- | --- | --- |
| A-2. 운영 시간 | `PUT/GET /api/v1/shops/{shopId}/schedule/operating-times` | DAILY/WEEKDAY_WEEKEND/BY_DAY |
| A-2. 간격 설정 | `PUT /api/v1/shops/{shopId}/schedule/interval`, `GET /api/v1/shops/{shopId}/schedule/settings` | interval 변경 + 현재 설정 조회 |
| A-3. 휴무일 | `POST/GET/DELETE /api/v1/shops/{shopId}/schedule/holidays` | WEEKLY/BIWEEKLY/MONTHLY/CUSTOM |
| A-4. 직원 시간 | `PUT/GET /api/v1/shops/{shopId}/staff/{staffId}/operating-times` | 직원별 오버라이드, isOff |
| B-1. 캘린더 | `GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly` | 월별 가용일/휴무/폐쇄일 |
| B-2. 시간 선택 | `GET /api/v1/shops/{shopId}/staff/{staffId}/availability` | 일별 가용 슬롯 |

### #115 - 예약 진입 조회

| User Flow 단계 | API | 핵심 기능 |
| --- | --- | --- |
| B-0. 예약 진입 | `GET /api/public/shops/{slugOrCode}/booking-entry` | 공개 링크 진입용 예약 시작 컨텍스트 조회 |
| B-0. 예약 진입 | `GET /api/v1/shops/{shopId}/booking-entry` | 인증 사용자 진입용 예약 시작 컨텍스트 조회 |

---

## Flow 외 구현 API (참고)

| 영역 | API | 설명 |
| --- | --- | --- |
| Reservation | `GET /api/reservations/shop/{shopId}/availability` | Deprecated. 과거 샵 기준 예약된 시간(`bookedTimes`) 조회. 새 연동은 `booking-entry -> staffId -> availability` 사용 |
| Chat | `GET /chat/rooms/{chatRoomId}/messages` | 메시지 히스토리 조회 (cursor/size) |
