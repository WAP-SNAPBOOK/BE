# `reservation` 도메인 확정 명세

작성일: `2026-03-10`

목적:

- 테스트 시나리오 작성과 객체지향 리팩터링의 기준이 되는 `reservation` 도메인 확정 명세를 정리한다.

---

## 1. 범위

현재 범위:

- 예약 생성
- 예약 상세/목록 조회
- 점주 확정
- 점주 거절
- 고객/점주 취소
- 점주 예약 수정
- 예약 가능 여부 연계
- 에러 계약 정의

후속 확장:

- `COMPLETED`
- `NO_SHOW`
- 예약금 결제/환불 자체 처리

---

## 2. 핵심 정책

### 2.1 예약 생성

- 고객은 `shopId`, `staffId`, `date`, `time`으로 예약을 생성한다.
- 예약 생성 시 상태는 `PENDING`이다.
- `PENDING` 중복 접수는 허용한다.
- 단, 아래 조건을 만족하지 않으면 `PENDING`도 생성하지 않는다.
    - 과거 날짜/시간 아님
    - 예약 가능 기간 내부
    - 최소 리드타임 충족
    - 휴무일 아님
    - 직원 운영시간 내부
    - availability 기준 예약 가능한 슬롯

### 2.2 시간 정책

- `time`과 `startAt`은 10분 단위다.
- `durationMinutes`도 10분 단위다.
- 점유는 `ReservationTimeBlock` 10분 단위로 관리한다.

### 2.3 확정 정책

- 점주만 확정할 수 있다.
- 자기 샵 예약만 확정할 수 있다.
- 확정 시 `durationMinutes`를 결정한다.
- 확정 시 시간 블록을 생성한다.
- 시간 블록 충돌 시 확정은 실패하고 기존 상태를 유지한다.

### 2.4 거절 정책

- 점주만 거절할 수 있다.
- 자기 샵 예약만 거절할 수 있다.

### 2.5 취소 정책

- 고객과 점주 모두 취소할 수 있다.
- 취소 상태는 `CANCELED` 하나로 통일한다.
- 고객은 예약일 전날 `23:59:59`까지 취소 가능하다.
- 예약일 `00:00`부터 고객 셀프 취소는 불가다.
- 점주는 운영상 언제든 취소 처리할 수 있다.
- `CONFIRMED` 예약 취소 시 점유 블록은 즉시 해제한다.

### 2.6 예약금 정책

- 예약금은 우리 서비스가 직접 관리하지 않는다.
- 예약 취소 후 환불 판단과 실제 환불은 점주가 별도로 처리한다.
- 전날까지 취소는 환불 가능 대상이다.
- 당일 취소는 환불 불가 대상이다.

### 2.7 점주 수정 정책

- 점주는 아래 전체 항목을 수정할 수 있다.
    - `date`
    - `time`
    - `staffId`
    - `menuSelections`
    - 메뉴 입력값
    - `requirements`
    - 첨부 이미지
- `PENDING`, `CONFIRMED` 모두 수정 가능하다.
- `CONFIRMED` 상태에서 `date/time/staffId` 변경 시 기존 점유를 해제하고 재생성한다.
- 재생성 충돌 시 전체 변경은 실패하고 기존 상태/점유를 유지한다.
- `reschedule`은 상태가 아니라 수정 행위다.

---

## 3. 상태 모델

현재 범위 상태:

- `PENDING`
- `CONFIRMED`
- `REJECTED`
- `CANCELED`

후속 확장 상태:

- `COMPLETED`
- `NO_SHOW`

운영 원칙:

- `NO_SHOW`는 "연락 없이 오지 않음"에만 사용한다.
- 고객이 연락했고 점주가 슬롯을 비우는 경우는 `CANCELED`다.
- 상태와 환불 정책은 분리한다.
- 상태와 연락 여부는 분리한다.

---

## 4. 취소 메타데이터

`CANCELED` 상태에는 아래 메타데이터를 함께 관리한다.

- `canceledByType`: `CUSTOMER` | `OWNER`
- `canceledByUserId`
- `canceledAt`
- `cancelReason`
- `cancelTiming`: `BEFORE_CUTOFF` | `AFTER_CUTOFF`
- `refundEligible`: `true` | `false`

해석 예시:

- 고객 전날 취소:
    - `canceledByType=CUSTOMER`
    - `cancelTiming=BEFORE_CUTOFF`
    - `refundEligible=true`
- 고객 당일 연락 후 점주 처리:
    - `canceledByType=OWNER`
    - `cancelTiming=AFTER_CUTOFF`
    - `refundEligible=false`

설계 권장:

- 위 데이터는 `CancellationInfo` 같은 값 객체 또는 동등한 구조로 묶는다.

---

## 5. 조회 정책

### 5.1 목록 조회

- 고객 목록과 점주 목록은 계속 제공한다.
- 목록 응답에는 `requirements`를 포함하지 않는다.

### 5.2 상세 조회

- 상세 조회에는 `requirements`를 포함한다.
- 메뉴 스냅샷과 입력값 스냅샷을 포함한다.

보류:

- 상세 조회에 `staffId`, `startAt`, `durationMinutes`를 포함할지 여부는 별도 확정 필요

### 5.3 availability 책임

- 예약 가능 여부 판단은 `availability` 도메인 API가 담당한다.
- `GET /api/reservations/shop/{shopId}/availability`는 삭제 대상이다.

---

## 6. 에러 계약

### 6.1 공통 원칙

- 요청 파싱/타입 오류/Bean Validation 실패는 `INVALID_PARAMETER`
- 도메인 규칙 위반은 비즈니스 에러 코드
- `IllegalStateException`, `IllegalArgumentException`은 API 바깥으로 직접 노출되지 않도록 정리

### 6.2 상태 전이 에러

| 에러 코드                                    | HTTP  |
|------------------------------------------|-------|
| `INVALID_RESERVATION_STATUS_FOR_CONFIRM` | `409` |
| `INVALID_RESERVATION_STATUS_FOR_REJECT`  | `409` |
| `INVALID_RESERVATION_STATUS_FOR_CANCEL`  | `409` |

### 6.3 취소 정책 에러

| 에러 코드                                   | HTTP  |
|-----------------------------------------|-------|
| `CUSTOMER_CANCELLATION_DEADLINE_PASSED` | `409` |

### 6.4 예약 생성 availability 에러

| 에러 코드                                 | HTTP  |
|---------------------------------------|-------|
| `RESERVATION_OUTSIDE_OPERATING_HOURS` | `400` |
| `RESERVATION_ON_HOLIDAY`              | `400` |
| `BOOKING_WINDOW_EXCEEDED`             | `400` |
| `MIN_BOOKING_LEAD_TIME_NOT_MET`       | `400` |
| `PAST_RESERVATION_NOT_ALLOWED`        | `400` |
| `TIME_BLOCK_ALREADY_BOOKED`           | `409` |

### 6.5 메뉴/입력값 에러

| 에러 코드                         | HTTP  |
|-------------------------------|-------|
| `MENU_NOT_IN_SHOP`            | `400` |
| `MENU_INACTIVE`               | `400` |
| `INVALID_MENU_INPUT_FIELD`    | `400` |
| `REQUIRED_MENU_INPUT_MISSING` | `400` |
| `INVALID_MENU_INPUT_VALUE`    | `400` |

정책:

- 메뉴 입력값 상세 위반 사유는 `INVALID_MENU_INPUT_VALUE`의 detail message로 전달한다.

---

## 7. AC 요약

- 유효한 슬롯에 대해서만 예약 생성이 가능하다.
- 동일 슬롯의 `PENDING` 중복 접수는 가능하다.
- `requirements`는 저장되며 상세 조회에서 확인할 수 있다.
- 점주만 확정/거절할 수 있다.
- 시간 블록 충돌 시 확정 또는 확정 상태 변경은 실패하고 기존 상태를 유지한다.
- 고객은 전날 `23:59:59`까지 취소할 수 있다.
- 당일 고객 셀프 취소는 불가하지만 점주는 운영상 취소 처리할 수 있다.
- 점주는 예약의 핵심 필드와 구성 정보를 모두 수정할 수 있다.

---

## 8. 구현 대비 갭

현재 코드와 비교했을 때 후속 구현/테스트 반영이 필요한 주요 갭:

- 예약 생성 시 availability 검증 추가
- `requirements` 영속 저장 및 상세 응답 반영
- 취소 API 및 취소 메타데이터 추가
- 점주 예약 수정 API 추가
- 상태 전이/메뉴 검증 에러 코드화
- `reservation` 도메인의 보조 availability API 제거
