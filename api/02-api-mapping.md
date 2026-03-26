# API Mapping - User Flow 기반

> 작성일: 2026-02-15
> 범례: **존재** = 현재 코드에 구현됨 | **미구현** = 문서상 계획만 있고 현재 코드에 없음 | **수정** = 기존 API/엔티티 변경 필요

---

## A. 점주 온보딩 플로우

### 1. 회원가입

| 기능 | HTTP | Endpoint | 상태 |
|------|------|----------|------|
| 카카오 로그인 | `POST` | `/oauth/login/kakao` | 존재 |
| 카카오 로그인 (로컬) | `POST` | `/oauth/login/kakao/local` | 존재 |
| 토큰 갱신 | `POST` | `/auth/refresh` | 존재 |
| 점주 회원가입 | `POST` | `/user/owner/signup` | 존재 |

### 2. 매장 생성

| 기능 | HTTP | Endpoint | 상태 |
|------|------|----------|------|
| 매장 생성 | `POST` | `/shop` | 존재 |

### 3. 운영 시간 설정

| 기능 | HTTP | Endpoint | 상태 |
|------|------|----------|------|
| 운영시간 타입+시간 설정 | `PUT` | `/api/v1/shops/{shopId}/schedule/operating-times` | 존재 |
| 간격(interval) 설정 | `PUT` | `/api/v1/shops/{shopId}/schedule/interval` | 존재 |
| 현재 설정 조회 | `GET` | `/api/v1/shops/{shopId}/schedule/settings` | 존재 |
| 현재 운영시간 조회 | `GET` | `/api/v1/shops/{shopId}/schedule/operating-times` | 존재 |

> `ScheduleType` enum: `DAILY`, `WEEKDAY_WEEKEND`, `BY_DAY` -- User Flow의 3가지 타입과 정확히 대응

### 4. 휴무일 설정

| 기능 | HTTP | Endpoint | 상태 |
|------|------|----------|------|
| 정기 휴무일 생성 | `POST` | `/api/v1/shops/{shopId}/schedule/holidays` | 존재 |
| 공휴일 휴무 토글 | `PATCH` | `/api/v1/shops/{shopId}/schedule/booking-policy` | **미구현** (후속 분리 API) |
| 특정 날짜 휴무 추가 | `POST` | `/api/v1/shops/{shopId}/schedule/holidays` | 존재 (`CUSTOM` 타입) |
| 휴무일 목록 조회 | `GET` | `/api/v1/shops/{shopId}/schedule/holidays` | 존재 |
| 휴무일 삭제 | `DELETE` | `/api/v1/shops/{shopId}/schedule/holidays/{holidayId}` | 존재 |

> `HolidayType` enum: `WEEKLY`, `BIWEEKLY`, `MONTHLY`, `CUSTOM` -- User Flow와 정확히 대응

### 5. 직원 추가

| 기능 | HTTP | Endpoint | 상태 | 비고 |
|------|------|----------|------|------|
| 직원 생성 | `POST` | `/api/v1/shops/{shopId}/staff` | **미구현** | `Staff` 엔티티 있음, Controller/Service/DTO 없음 |
| 직원 목록 조회 | `GET` | `/api/v1/shops/{shopId}/staff` | **미구현** | |
| 직원 상세 조회 | `GET` | `/api/v1/shops/{shopId}/staff/{staffId}` | **미구현** | |
| 직원 수정 | `PATCH` | `/api/v1/shops/{shopId}/staff/{staffId}` | **미구현** | |
| 직원 삭제 | `DELETE` | `/api/v1/shops/{shopId}/staff/{staffId}` | **미구현** | |
| 직원 운영시간 설정 | `PUT` | `/api/v1/shops/{shopId}/staff/{staffId}/operating-times` | 존재 | "매장과 동일" 플래그는 프론트에서 처리 가능 |
| 직원 운영시간 조회 | `GET` | `/api/v1/shops/{shopId}/staff/{staffId}/operating-times` | 존재 | |
| 직원 정기 휴무일 생성 | `POST` | `/api/v1/shops/{shopId}/staff/{staffId}/holidays` | **미구현** | `StaffHoliday` 엔티티 자체가 없음 |
| 직원 공휴일 휴무 토글 | `PUT` | `/api/v1/shops/{shopId}/staff/{staffId}/settings` | **미구현** | `publicHolidayOff` 같은 직원별 설정 필요 |
| 직원 특정 날짜 휴무 | `POST` | `/api/v1/shops/{shopId}/staff/{staffId}/holidays` | **미구현** | CUSTOM 타입 |
| 직원 휴무일 조회 | `GET` | `/api/v1/shops/{shopId}/staff/{staffId}/holidays` | **미구현** | |
| 직원 휴무일 삭제 | `DELETE` | `/api/v1/shops/{shopId}/staff/{staffId}/holidays/{id}` | **미구현** | |

### 6. 태그 추가

| 기능 | HTTP | Endpoint | 상태 | 비고 |
|------|------|----------|------|------|
| 태그 생성 | `POST` | `/api/tags` | 존재 | 전역 태그 (shopId 없음) |
| 태그 목록 조회 | `GET` | `/api/tags` | 존재 | 전역 조회 |
| 매장별 태그 조회 | `GET` | `/api/shops/{shopId}/tags` | **미구현** | 매장에 연결된 태그만 필터 필요 |

### 7. 메뉴 추가

| 기능 | HTTP | Endpoint | 상태 | 비고 |
|------|------|----------|------|------|
| 메뉴 생성 | `POST` | `/api/shops/{shopId}/menus` | 존재 | 가격 필드 없음, 카테고리는 태그로 동일 처리 |
| 메뉴 조회 | `GET` | `/api/shops/{shopId}/menus` | 존재 | tagIds로 필터 가능 |
| 메뉴 수정 | `PATCH` | `/api/shops/{shopId}/menus/{menuId}` | 존재 | |
| 메뉴 비활성화 | `DELETE` | `/api/shops/{shopId}/menus/{menuId}` | 존재 | soft delete |
| 입력 필드 추가 | `POST` | `/api/shops/{shopId}/menus/{menuId}/input-fields` | 존재 | TEXT/NUMBER 타입 |
| 메뉴에 태그 연결 | `POST` | `/api/shops/{shopId}/menus/{menuId}/tags` | 존재 | |
| 카테고리(=태그) 생성 | `POST` | `/api/tags` | 존재 | 카테고리와 태그를 동일 개념으로 사용 |
| 카테고리(=태그) 목록 조회 | `GET` | `/api/tags` | 존재 | 필요 시 매장 연결 태그만 별도 조회 |
| 가격 필드 | - | `ShopMenu.price` | **수정** | `ShopMenu` 엔티티에 `price` 필드 추가 필요 |

---

## B. 고객 예약 플로우

### 1. 채팅방 진입

| 기능 | HTTP | Endpoint | 상태 |
|------|------|----------|------|
| 단축 링크 리다이렉트 | `GET` | `/s/{slugOrCode}` | 존재 |
| 채팅방 생성/조회 | `GET` | `/chat/rooms/shop/{shopId}` | 존재 |
| 고객 회원가입 | `POST` | `/user/customer/signup` | 존재 |

### 2. 날짜 선택 (캘린더)

| 기능 | HTTP | Endpoint | 상태 | 비고 |
|------|------|----------|------|------|
| 월별 예약 가능 날짜 조회 | `GET` | `/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly?yearMonth=` | 존재 | `availableDates`, `holidayDates`, `closedDates` 반환 |
| 직원 목록 조회 (선택용) | `GET` | `/api/v1/shops/{shopId}/staff` | **미구현** | 고객이 직원을 선택하려면 목록 필요 |

> `AvailabilityMonthlyResponse`: `availableDates`(예약 가능), `holidayDates`(공휴일=빨간), `closedDates`(휴무=회색) -- User Flow 요구사항과 대응

### 3. 시간 선택

| 기능 | HTTP | Endpoint | 상태 | 비고 |
|------|------|----------|------|------|
| 일별 예약 가능 슬롯 조회 | `GET` | `/api/v1/shops/{shopId}/staff/{staffId}/availability?date=` | 존재 | 시간 문자열 리스트 반환 |

> 현재 `AvailabilitySlotsResponse`는 available 슬롯만 반환. 점유된 슬롯을 "회색"으로 표시하려면 전체 슬롯 + 점유 여부 플래그가 필요할 수 있음 (**수정 검토**)

### 4. 메뉴 선택

| 기능 | HTTP | Endpoint | 상태 | 비고 |
|------|------|----------|------|------|
| 매장 태그 목록 조회 | `GET` | `/api/shops/{shopId}/tags` | **미구현** | 고객에게 매장의 태그 보여주기 |
| 태그별 메뉴 조회 | `GET` | `/api/shops/{shopId}/menus?tagIds=` | 존재 | tagIds 파라미터 지원 |
| 메뉴 입력 필드 조회 | `GET` | `/api/shops/{shopId}/menus/{menuId}/input-fields` | 존재 | |

### 5. 예약 생성

| 기능 | HTTP | Endpoint | 상태 | 비고 |
|------|------|----------|------|------|
| 예약 생성 | `POST` | `/api/reservations` | 존재 | `date`, `time`, `requirements`, `imageUrls`, `menuSelections` 사용 |
| 사진 업로드 | `POST` | `/api/files/upload-multiple` | 존재 | 예약 전 업로드 후 URL 전달 |

---

## 신규 개발 필요 항목 요약

| 우선순위 | 항목 | 범위 |
|---------|------|------|
| **P0** | Staff CRUD Controller | 엔티티 있음, Controller/Service/DTO 신규 |
| **P0** | StaffHoliday 엔티티 + API | 엔티티/Repository/Service/Controller 전체 신규 |
| **P0** | Staff publicHolidayOff 설정 | StaffSettings 엔티티 or Staff 필드 추가 |
| **P1** | ShopMenu에 `price` 필드 추가 | 엔티티/DTO 수정 + DB migration |
| **P1** | 카테고리/태그 용어 통일 | 카테고리는 태그로 동일 처리 (별도 Category 엔티티 불필요) |
| **P2** | 매장별 태그 조회 API | `GET /api/shops/{shopId}/tags` 미구현 |
| **P2** | 가용 슬롯 응답 확장 검토 | 전체 슬롯 + 점유 상태 반환 (회색 표시용) |

---

## C. 기타 구현 API (현재 코드 기준)

| 기능 | HTTP | Endpoint | 상태 | 비고 |
|------|------|----------|------|------|
| 예약 상세(보조) 가용 시간 조회 | `GET` | `/api/reservations/shop/{shopId}/availability?date=` | 존재 | `bookedTimes` 반환 |
| 채팅 메시지 히스토리 조회 | `GET` | `/chat/rooms/{chatRoomId}/messages?cursor=&size=` | 존재 | `size` 기본 50 |
