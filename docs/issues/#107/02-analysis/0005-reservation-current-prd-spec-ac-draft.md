# `reservation` 도메인 현재 구현 기준 PRD / 기능 명세 / API 명세 / AC 초안

작성일: `2026-03-10`

목적:

- 현재 구현된 `reservation` 도메인을 기준으로 요구사항을 재구성한다.
- 테스트 시나리오 작성 전에 누락/모호한 요구사항을 식별한다.
- 이후 객체지향 관점 리팩터링에서 고정해야 할 도메인 계약을 분명히 한다.

---

## 1. 한 줄 요약

현재 구현은 "고객이 `PENDING` 예약을 생성하고, 점주가 `CONFIRMED` 또는 `REJECTED`로 처리하는 기본 흐름"은 갖고 있다. 다만 예약 생성 시 가용성/영업시간/휴무/예약 가능 기간을 강제하지 않고, `requirements`가 저장되지 않으며, 상태 전이 실패와 메뉴 검증 실패가 일관된 비즈니스 에러로 정리되어 있지 않다.

---

## 2. 현재 구현 사실

### 2.1 지원되는 핵심 시나리오

- 고객은 `shopId`, `staffId`, `date`, `time`으로 예약을 생성할 수 있다.
- 예약 생성 시 초기 상태는 `PENDING`이다.
- 예약 생성 시 메뉴를 여러 개 선택할 수 있고, 메뉴별 입력값을 저장할 수 있다.
- 점주는 `PENDING` 예약을 확정하거나 거절할 수 있다.
- 확정 시 `durationMinutes`를 결정하고, `staffId + 10분 블록` 기준으로 점유를 생성한다.
- 예약 상세/목록/채팅방 기준 조회 API가 있다.
- 예약 관련 이벤트는 커밋 이후 채팅 시스템 메시지 발행으로 이어진다.

### 2.2 현재 구현이 하지 않는 것

- 고객 예약 취소 API는 없다.
- 점주 예약 수정 API는 없다.
- 예약 생성 시 영업시간/휴무/예약 가능 기간/최소 리드타임 검증을 하지 않는다.
- 예약 생성 시 `PENDING` 중복 접수를 막지 않는다.
- 예약 생성 시 `requirements`를 영속 저장하지 않는다.
- 확정 후 시간 블록 해제 로직은 취소 API 부재로 실제 플로우에 연결되어 있지 않다.

---

## 3. PRD 초안

### 3.1 문제 정의

고객은 샵과 담당 직원을 선택해 예약 요청을 남길 수 있어야 하고, 점주는 이를 검토한 뒤 확정 또는 거절할 수 있어야 한다. 확정 시 실제 시술 시간 점유를 안전하게 관리해야 하며, 예약 시점의 메뉴 선택과 입력값은 이후 원본 메뉴가 바뀌어도 보존되어야 한다.

### 3.2 사용자

- 고객
- 점주(매장 소유자)

### 3.3 도메인 목표

- 고객 예약 접수
- 점주 승인/거절
- 직원 단위 시간 점유 관리
- 예약 시점 메뉴/입력값 스냅샷 보존
- 예약 조회 및 채팅 컨텍스트 연계

### 3.4 현재 구현 기준 비목표

- 예약 변경 이력 관리
- 취소 정책 관리
- 확정 이후 재배정/재스케줄 전용 API
- 가격 계산/결제
- 고객 요청사항의 영속 조회

---

## 4. 기능 명세 초안

### F1. 예약 생성

입력:

- `shopId`
- `staffId`
- `date`
- `time`
- `requirements`(현재는 응답에만 반영, 저장 안 됨)
- `imageUrls`
- `menuSelections`

현재 규칙:

- `date`, `time`, `staffId`는 필수다.
- `time`은 10분 단위여야 한다.
- `staffId`는 존재해야 하며 해당 `shopId` 소속이어야 한다.
- 예약 생성 시 `startAt = date + time`으로 계산된다.
- 상태는 `PENDING`으로 생성된다.
- `imageUrls`는 저장된다.
- `menuSelections`가 있으면 메뉴 스냅샷과 입력값 스냅샷을 저장한다.

현재 미적용 규칙:

- 선택한 시간이 실제 예약 가능 슬롯인지 검증하지 않는다.
- 해당 날짜가 휴무일인지 검증하지 않는다.
- 예약 가능 기간(`bookingWindowDays`) 초과인지 검증하지 않는다.
- 당일 최소 리드타임(`minBookingLeadMinutes`)을 검증하지 않는다.
- 같은 고객/같은 직원/같은 시각의 중복 접수 제한이 없다.

### F2. 예약 확정

입력:

- `message`
- `durationMinutes`
- `startAt`(선택)

현재 규칙:

- 점주만 확정할 수 있다.
- 자기 샵 예약만 확정할 수 있다.
- `durationMinutes`는 10분 단위여야 한다.
- `startAt`이 오면 기존 예약 시간을 같은 날짜 내 새 시간으로 변경한다.
- 예약은 `PENDING` 상태에서만 확정 가능하다.
- 확정 시 `ReservationTimeBlock`이 10분 단위로 생성된다.
- 동일 `staffId + block_start_at` 충돌 시 전체 트랜잭션이 롤백된다.

제약:

- 현재 API는 날짜 변경을 지원하지 않는다.
- 현재 API는 직원 변경을 지원하지 않는다.
- 상태 전이 실패는 `IllegalStateException`이라 현재 API 기준 500으로 떨어질 가능성이 있다.

### F3. 예약 거절

입력:

- `reason`

현재 규칙:

- 점주만 거절할 수 있다.
- 자기 샵 예약만 거절할 수 있다.
- 예약은 `PENDING` 상태에서만 거절 가능하다.

제약:

- 상태 전이 실패는 `IllegalStateException`이라 비즈니스 4xx로 정리되어 있지 않다.

### F4. 예약 조회

현재 지원:

- 예약 상세 조회
- 고객 내 예약 목록 조회
- 점주 샵 예약 목록 조회
- 고객 채팅방 내 예약 조회
- 점주가 특정 고객의 샵 내 예약 조회

현재 상세 응답 포함 정보:

- 상태, 날짜, 시간, 생성일시
- 샵명, 고객명, 고객 전화번호
- 거절 사유, 확정 메시지
- 사진 목록
- 메뉴 스냅샷과 입력값 스냅샷

현재 상세 응답 미포함 정보:

- `staffId`
- `startAt`
- `durationMinutes`
- `requirements`

### F5. 가용성 조회

현재 시스템에는 두 종류가 공존한다.

- `availability` 도메인: 직원/영업시간/휴무/예약 가능 기간 기준의 "진짜 예약 가능 슬롯" 조회
- `reservation` 도메인: 샵 기준 예약된 시간(`bookedTimes`)만 조회

즉, `reservation` 도메인만 보면 "예약 가능한지"가 아니라 "이미 예약된 time 값 목록"만 반환한다.

---

## 5. API 명세 초안

### A1. 예약 생성

- Method/Path: `POST /api/reservations`
- Actor: 고객
- Request:

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

- Success:
  - `201 Created`
  - `status = PENDING`
- Error:
  - `REQUIRED_DATE_MISSING`
  - `REQUIRED_TIME_MISSING`
  - `REQUIRED_STAFF_ID_MISSING`
  - `INVALID_TIME_INTERVAL`
  - `STAFF_NOT_FOUND`
  - `STAFF_NOT_IN_SHOP`
  - 메뉴/입력값 검증 실패는 현재 `IllegalArgumentException` 기반이라 표준 에러 코드가 없다.

### A2. 예약 상세 조회

- Method/Path: `GET /api/reservations/{id}`
- Actor: 예약 고객 또는 예약 점주
- Success:
  - `200 OK`
  - 메뉴/입력값 스냅샷 포함
- Error:
  - `RESERVATION_NOT_FOUND`
  - 권한 없음

### A3. 예약 확정

- Method/Path: `PUT /api/reservations/{id}/confirm`
- Actor: 점주
- Request:

```json
{
  "message": "내일 10시 30분에 방문해주세요",
  "durationMinutes": 60,
  "startAt": "10:30"
}
```

- Success:
  - `200 OK`
  - `status = CONFIRMED`
- Error:
  - 권한 없음
  - `TIME_BLOCK_ALREADY_BOOKED`
  - 입력 validation 실패
  - 비정상 상태 전이는 현재 표준 에러 코드 없음

### A4. 예약 거절

- Method/Path: `PUT /api/reservations/{id}/reject`
- Actor: 점주
- Request:

```json
{
  "reason": "해당 시간에는 예약이 어렵습니다"
}
```

- Success:
  - `200 OK`
  - `status = REJECTED`
- Error:
  - 권한 없음
  - 비정상 상태 전이는 현재 표준 에러 코드 없음

### A5. 고객 내 예약 목록

- Method/Path: `GET /api/reservations/my`
- Actor: 고객

### A6. 점주 샵 예약 목록

- Method/Path: `GET /api/reservations/shop`
- Actor: 점주

### A7. 채팅방 컨텍스트 조회

- 고객: `GET /api/reservations/chat/customer?shopId={shopId}`
- 점주: `GET /api/reservations/chat/owner?shopId={shopId}&customerId={customerId}`

### A8. 샵 기준 예약 시간 조회

- Method/Path: `GET /api/reservations/shop/{shopId}/availability?date={yyyy-MM-dd}`
- Actor: 현재 구현상 별도 도메인 권한 검증 없음
- Response:
- `bookedTimes`
- 주의:
  - staff 기준이 아니다.
  - 영업시간 기반 availability API와 역할이 다르다.

---

## 6. AC 초안

### AC1. 예약 생성

- 고객이 `shopId`, `staffId`, `date`, `time`을 주면 예약이 `PENDING`으로 생성된다.
- `time`이 10분 단위가 아니면 생성이 거부된다.
- 존재하지 않는 `staffId`이거나 다른 샵 직원이면 생성이 거부된다.
- 메뉴가 선택되면 예약 시점 메뉴 스냅샷이 저장된다.
- 메뉴 입력값이 있으면 예약 시점 입력값 스냅샷이 저장된다.

### AC2. 예약 확정

- 점주만 확정할 수 있다.
- 자기 샵 예약만 확정할 수 있다.
- `durationMinutes`는 10분 단위여야 한다.
- 충돌 없는 경우 `CONFIRMED`가 되고 시간 블록이 생성된다.
- `startAt`이 오면 예약 시간은 해당 시각으로 변경된 후 확정된다.
- 동일 직원 시간 블록 충돌 시 확정은 실패하고 예약은 `PENDING`을 유지한다.

### AC3. 예약 거절

- 점주만 거절할 수 있다.
- 자기 샵 예약만 거절할 수 있다.
- 성공 시 상태는 `REJECTED`가 된다.

### AC4. 상세 조회

- 예약 고객 또는 점주만 상세를 조회할 수 있다.
- 상세 조회에는 메뉴/입력값 스냅샷이 포함된다.
- 메뉴가 없는 예약은 `menus=[]`로 반환된다.

### AC5. 목록 조회

- 고객은 자신의 예약 목록을 최신순으로 조회할 수 있다.
- 점주는 자신의 샵 예약 목록을 최신순으로 조회할 수 있다.
- 채팅 컨텍스트에서도 동일한 예약 목록을 조회할 수 있다.

---

## 7. 에러 계약 초안

### 7.1 원칙

- 요청 파싱 실패, 타입 불일치, Bean Validation 실패는 공통 `INVALID_PARAMETER`로 처리한다.
- 도메인 규칙 위반은 `ReservationErrorCode` 기반 비즈니스 에러로 처리한다.
- `IllegalStateException`, `IllegalArgumentException`이 API 응답으로 직접 노출되지 않도록 정리한다.

### 7.2 상태 전이 에러

| 에러 코드 | HTTP 상태 | 의미 |
|---|---|---|
| `INVALID_RESERVATION_STATUS_FOR_CONFIRM` | `409 Conflict` | 현재 상태에서는 확정할 수 없음 |
| `INVALID_RESERVATION_STATUS_FOR_REJECT` | `409 Conflict` | 현재 상태에서는 거절할 수 없음 |
| `INVALID_RESERVATION_STATUS_FOR_CANCEL` | `409 Conflict` | 현재 상태에서는 취소할 수 없음 |

메시지 원칙:

- 기본 메시지는 액션 중심으로 간결하게 유지한다.
- 필요 시 detail message에 현재 상태(`currentStatus`)를 포함한다.

### 7.3 취소 정책 에러

| 에러 코드 | HTTP 상태 | 의미 |
|---|---|---|
| `CUSTOMER_CANCELLATION_DEADLINE_PASSED` | `409 Conflict` | 고객 취소 가능 기한이 지남 |

정책:

- 고객의 당일 취소 불가는 위 코드 하나로 통합한다.

### 7.4 예약 생성 availability 위반 에러

| 에러 코드 | HTTP 상태 | 의미 |
|---|---|---|
| `RESERVATION_OUTSIDE_OPERATING_HOURS` | `400 Bad Request` | 직원 운영시간 밖 예약 요청 |
| `RESERVATION_ON_HOLIDAY` | `400 Bad Request` | 휴무일 예약 요청 |
| `BOOKING_WINDOW_EXCEEDED` | `400 Bad Request` | 예약 가능 기간 초과 |
| `MIN_BOOKING_LEAD_TIME_NOT_MET` | `400 Bad Request` | 최소 리드타임 미충족 |
| `PAST_RESERVATION_NOT_ALLOWED` | `400 Bad Request` | 과거 날짜/시간 예약 요청 |
| `TIME_BLOCK_ALREADY_BOOKED` | `409 Conflict` | 확정 점유 또는 충돌로 예약 불가 |

### 7.5 메뉴/입력값 검증 에러

| 에러 코드 | HTTP 상태 | 의미 |
|---|---|---|
| `MENU_NOT_IN_SHOP` | `400 Bad Request` | 해당 샵 소속 메뉴가 아님 |
| `MENU_INACTIVE` | `400 Bad Request` | 비활성 메뉴 |
| `INVALID_MENU_INPUT_FIELD` | `400 Bad Request` | 메뉴에 정의되지 않은 입력 필드 |
| `REQUIRED_MENU_INPUT_MISSING` | `400 Bad Request` | 필수 입력값 누락 |
| `INVALID_MENU_INPUT_VALUE` | `400 Bad Request` | 타입/범위/step/길이 위반 |

정책:

- 메뉴 입력값 세부 위반은 `INVALID_MENU_INPUT_VALUE` 하나로 묶고, detail message로 구체 사유를 전달한다.

---

## 8. 권장 상태 전이표

### 7.1 현재 범위 상태

- `PENDING`
- `CONFIRMED`
- `REJECTED`
- `CANCELED`

### 7.2 후속 확장 상태

- `COMPLETED`
- `NO_SHOW`

### 7.3 상태 전이 규칙

| 현재 상태 | 액션 | 수행 주체 | 조건 | 다음 상태 | 비고 |
|---|---|---|---|---|---|
| 없음 | 예약 생성 | 고객 | availability 정책 통과 | `PENDING` | `PENDING` 중복 접수는 허용 |
| `PENDING` | 확정 | 점주 | 권한 있음, 슬롯 충돌 없음 | `CONFIRMED` | duration 결정, 점유 생성 |
| `PENDING` | 거절 | 점주 | 권한 있음 | `REJECTED` | 점유 없음 |
| `PENDING` | 취소 | 고객/점주 | 취소 가능 기간 또는 운영 처리 | `CANCELED` | 점유 없음 |
| `CONFIRMED` | 취소 | 고객 | 예약일 전날 `23:59:59` 이전 | `CANCELED` | 점유 즉시 해제, 환불 가능 대상 |
| `CONFIRMED` | 취소 | 점주 | 언제든 운영 처리 가능 | `CANCELED` | 점유 즉시 해제 |
| `CONFIRMED` | 당일 취소 처리 | 점주 | 예약일 `00:00` 이후 고객 연락/운영 판단 | `CANCELED` | 점유 즉시 해제, 환불 불가 대상 |
| `CONFIRMED` | 방문 완료 | 점주 | 후속 확장 | `COMPLETED` | 현재 구현 범위 아님 |
| `CONFIRMED` | 미방문 처리 | 점주 | 후속 확장 | `NO_SHOW` | 현재 구현 범위 아님 |

### 7.4 운영 해석

| 상황 | 추천 상태 | 슬롯 해제 | 환불 정책 의미 | 비고 |
|---|---|---|---|---|
| 전날까지 정상 취소 | `CANCELED` | 예 | 환불 가능 | 일반 취소 |
| 당일 연락 후 취소 | `CANCELED` | 예 | 환불 불가 | late cancellation |
| 연락 없이 안 옴 | `NO_SHOW` | 후속 확장 정책 | 환불 불가 | 진짜 노쇼 |
| 점주 사정 취소 | `CANCELED` | 예 | 점주 정책 | 고객 책임 아님 |

### 7.5 핵심 원칙

- 상태와 환불 정책은 분리한다.
- 상태와 연락 여부는 분리한다.
- `NO_SHOW`는 "연락 없이 오지 않음"에만 사용한다.
- 고객이 연락했고 점주가 운영상 슬롯을 비우는 경우는 `CANCELED`로 본다.

---

## 9. 테스트 전에 반드시 닫아야 할 질문

### Q1. 예약 생성은 "가용한 슬롯"만 허용해야 하는가?

결정:

- 예약 생성 시 아래 조건은 모두 서버가 검사하고, 만족하지 않으면 `PENDING` 접수도 생성하지 않는다.

적용 대상:

- 영업시간 내부인지
- 휴무일인지
- 예약 가능 기간 내부인지
- 최소 리드타임을 만족하는지

정리:

- `PENDING`을 열어둔다는 것은 "확정 전 상태를 둔다"는 뜻이지, 유효하지 않은 슬롯 접수를 허용한다는 뜻은 아니다.
- 따라서 생성 단계부터 availability 정책을 통과한 요청만 접수한다.
- 단, "같은 슬롯에 대해 다른 `PENDING` 예약을 중복 접수할 수 있는가"는 별도 결정이 필요하다.

### Q2. `PENDING` 중복 접수는 어디까지 허용할 것인가?

결정:

- 같은 `staffId + date + time`에 대해 `PENDING` 예약은 중복 접수를 허용한다.

정리:

- 생성 단계에서는 `PENDING` 중복 자체를 이유로 거절하지 않는다.
- 다만 availability 정책 위반 슬롯은 생성 자체를 거절한다.
- 최종 확정은 점유 블록 기준으로 충돌 검증하며, 동일 시간대에 여러 `PENDING`이 있더라도 실제 `CONFIRMED`는 하나만 성립할 수 있다.

### Q3. 고객 요청사항 `requirements`는 저장 대상인가?

결정:

- `requirements`는 영속 저장 대상이다.
- 예약 목록 응답에는 포함하지 않는다.
- 예약 상세 조회 응답에는 포함한다.

현재 구현과의 차이:

- 현재는 생성 응답에만 실리고 엔티티에 저장되지 않는다.
- 따라서 이 요구사항을 만족하려면 저장 모델과 상세 조회 응답 계약이 보강되어야 한다.

### Q4. 상세 응답에 `staffId`, `startAt`, `durationMinutes`, `requirements`가 포함되어야 하는가?

테스트 시나리오와 운영 화면 요구를 생각하면 빠져 있는 정보다.

### Q5. 상태 전이 실패는 어떤 에러 계약을 가져야 하는가?

예:

- 이미 `CONFIRMED`된 예약을 다시 확정
- 이미 `REJECTED`된 예약을 다시 거절
- `CONFIRMED` 예약을 거절

현재는 `IllegalStateException`으로 떨어질 가능성이 높다. 비즈니스 4xx와 에러 코드가 필요하다.

### Q6. 메뉴/입력값 검증 실패는 어떤 표준 에러로 내려야 하는가?

현재는 `IllegalArgumentException`이라 클라이언트 계약이 불안정하다.

### Q7. 취소 정책은 어떻게 정의할 것인가?

결정:

- 고객과 점주 모두 취소할 수 있다.
- 취소 상태는 `CANCELED` 하나로 통일한다.
- `CONFIRMED` 예약 취소 시 점유 블록은 즉시 해제한다.
- 예약금은 우리 서비스에서 직접 관리하지 않는다.
- 예약 취소가 발생하면, 예약금 환불 판단과 실제 환불 처리는 점주가 오프라인/외부 수단으로 별도 수행한다.

예약금/환불 정책:

- 예약 시작일 1일 전까지는 취소 가능이며, 예약금 환불 대상이다.
- 예약 당일은 취소 및 예약금 환불 불가 정책으로 운영한다.

- "1일 전"의 기준 시점은 예약일 전날 `23:59:59`까지로 한다.
- 즉 예약일 `00:00`부터는 취소 및 예약금 환불 불가다.

취소 메타데이터 요구사항:

- `CANCELED` 상태에는 취소 메타데이터를 함께 관리한다.
- 상태만으로는 정상 취소 / 당일 연락 취소 / 점주 사정 취소를 구분할 수 없으므로, 아래 필드는 명세 범위에 포함한다.

최소 필수 필드:

- `canceledByType`: `CUSTOMER` | `OWNER`
- `canceledByUserId`: 취소를 실제 처리한 사용자 ID
- `canceledAt`: 취소 처리 시각
- `cancelReason`: 취소 사유
- `cancelTiming`: `BEFORE_CUTOFF` | `AFTER_CUTOFF`
- `refundEligible`: `true` | `false`

해석 규칙:

- 고객이 전날까지 직접 취소: `canceledByType=CUSTOMER`, `cancelTiming=BEFORE_CUTOFF`, `refundEligible=true`
- 고객이 당일 연락하고 점주가 운영상 취소 처리: `canceledByType=OWNER`, `cancelTiming=AFTER_CUTOFF`, `refundEligible=false`
- 점주 사정 취소: `canceledByType=OWNER`, 환불 가능 여부는 운영 정책으로 결정

설계 권장:

- 취소 메타데이터는 엔티티 필드 난립보다 `CancellationInfo` 같은 값 객체 또는 동등한 구조로 묶는 편이 좋다.

후속 상태:

- `COMPLETED`, `NO_SHOW`는 현재 구현 범위에는 넣지 않되, 명세에 후속 확장 상태로 언급한다.

### Q8. 점주의 예약 변경 범위는 어디까지인가?

결정:

- 점주는 예약의 시간만이 아니라 아래 전체 항목을 변경할 수 있다.
  - `date`
  - `time`
  - `staffId`
  - `menuSelections`
  - 메뉴 입력값
  - `requirements`
  - 첨부 이미지

운영 규칙:

- `PENDING` 예약은 점주가 전체 항목을 수정할 수 있다.
- `CONFIRMED` 예약도 점주가 전체 항목을 수정할 수 있다.
- `CONFIRMED` 상태에서 `date/time/staffId`가 변경되면 기존 점유 블록을 해제하고, 변경된 값 기준으로 점유를 다시 생성해야 한다.
- 재생성 시 충돌이 발생하면 전체 변경은 실패하고 기존 확정 상태/점유를 유지해야 한다.
- 메뉴/입력값/요청사항/이미지 변경은 시간 점유에는 영향을 주지 않는다.

추가 해석:

- `reschedule`은 별도 상태가 아니라 점주의 수정 행위다.
- 따라서 점주 수정은 상태 전이가 아니라 변경 이력 대상이다.

### Q9. `GET /api/reservations/shop/{shopId}/availability`는 계속 유지할 것인가?

결정:

- `GET /api/reservations/shop/{shopId}/availability`는 삭제 대상이다.

정리:

- 예약 가능 여부 계산 책임은 `availability` 도메인 API로 단일화한다.
- `reservation` 도메인은 예약 생성/조회/상태 변경/수정에 집중한다.
- 테스트 기준에서도 예약 가능 시간 판단은 `availability` 도메인 응답을 기준으로 본다.

---

## 10. 지금 시점 추천 테스트 기준

테스트를 바로 쓰기 전에 아래 둘 중 무엇을 고정할지 먼저 정해야 한다.

### 옵션 A. 현재 구현 사실을 먼저 보호

- 장점: 리팩터링 안전망을 빠르게 만든다.
- 단점: 나중에 바꿀 정책까지 현재 동작으로 굳힐 수 있다.

### 옵션 B. 먼저 요구사항을 닫고 그 요구사항으로 테스트 작성

- 장점: 정책과 테스트가 맞는다.
- 단점: 지금 구현과 어긋나는 테스트가 많아질 수 있다.

현재 상황에서는 아래 항목만 먼저 의사결정하고 테스트에 들어가는 것을 권장한다.

- 예약 생성 시 availability 검증 여부
- `requirements` 저장 여부
- 취소 정책
- 상태 전이 실패 에러 계약
- 점주 변경 가능 범위

---

## 11. 결론

지금 `reservation` 도메인은 "기본 예약 접수/확정/거절"은 동작하지만, 테스트 기준 문서로 쓰기에는 아직 정책 구멍이 있다. 특히 `requirements` 미저장, 생성 시 availability 미검증, 취소 정책 부재, 상태 전이 예외 계약 부재는 테스트 시나리오 전에 반드시 정리해야 한다.
