# `reservation` 기능 명세

작성일: `2026-03-10`

## 1. 도메인 개요

`reservation` 도메인은 고객의 예약 요청을 접수하고, 점주가 이를 확정/거절/취소/수정하는 기능을 담당한다. 예약 가능 여부 계산은 `availability` 도메인이 담당하며, `reservation`은 그 결과를 기준으로 예약 상태를 관리한다.

## 2. 상태

현재 범위:

- `PENDING`
- `CONFIRMED`
- `REJECTED`
- `CANCELED`

후속 확장:

- `COMPLETED`
- `NO_SHOW`

## 3. 예약 생성

### 3.1 규칙

- 고객은 `shopId`, `staffId`, `date`, `time`으로 예약을 생성한다.
- 상태는 `PENDING`으로 시작한다.
- 동일 `staffId + date + time`에 대해 `PENDING` 중복 접수는 허용한다.

### 3.2 생성 차단 조건

- 과거 날짜/시간
- 예약 가능 기간 초과
- 최소 리드타임 미충족
- 휴무일
- 직원 운영시간 밖
- `availability` 기준 예약 불가 슬롯

### 3.3 부가 정보

- `requirements`는 저장 대상이다.
- 목록 응답에는 포함하지 않는다.
- 상세 응답에는 포함한다.
- 메뉴 선택과 입력값은 스냅샷으로 저장한다.
- 첨부 이미지는 저장한다.

## 4. 예약 확정

- 점주만 가능
- 자기 샵 예약만 가능
- `durationMinutes`는 10분 단위
- 확정 시 점유 블록 생성
- 충돌 시 확정 실패, 기존 상태 유지

## 5. 예약 거절

- 점주만 가능
- 자기 샵 예약만 가능
- 상태는 `REJECTED`

## 6. 예약 취소

### 6.1 취소 주체

- 고객
- 점주

### 6.2 취소 정책

- 상태는 `CANCELED` 하나로 통일
- 고객은 예약일 전날 `23:59:59`까지 취소 가능
- 예약일 `00:00`부터 고객 셀프 취소 불가
- 점주는 운영상 언제든 취소 가능
- `CONFIRMED` 취소 시 점유 블록 즉시 해제

### 6.3 운영 해석

- 당일 연락 후 점주가 슬롯을 비우는 경우는 `CANCELED`
- `NO_SHOW`는 연락 없이 안 온 경우의 후속 확장 상태

## 7. 취소 메타데이터

`CANCELED`에는 아래 메타데이터를 포함한다.

- `canceledByType`
- `canceledByUserId`
- `canceledAt`
- `cancelReason`
- `cancelTiming`
- `refundEligible`

## 8. 점주 수정

### 8.1 수정 가능 범위

- `date`
- `time`
- `staffId`
- `menuSelections`
- 메뉴 입력값
- `requirements`
- 첨부 이미지

### 8.2 수정 가능 상태

- `PENDING`
- `CONFIRMED`

### 8.3 수정 규칙

- `CONFIRMED`에서 `date/time/staffId`를 변경하면 기존 점유를 해제하고 재생성한다.
- 재생성 충돌 시 전체 변경은 실패하고 기존 상태/점유를 유지한다.
- `reschedule`은 별도 상태가 아니라 수정 행위다.

## 9. 조회

### 9.1 목록 조회

- 고객 내 예약 목록
- 점주 샵 예약 목록
- 채팅방 컨텍스트 목록

목록에는 `requirements`를 포함하지 않는다.

### 9.2 상세 조회

- `requirements` 포함
- 메뉴 스냅샷 및 입력값 스냅샷 포함

보류:

- `staffId`, `startAt`, `durationMinutes` 포함 여부

## 10. availability 책임

- 예약 가능 여부 판단은 `availability` 도메인이 담당한다.
- `GET /api/reservations/shop/{shopId}/availability`는 제거한다.

## 11. 에러 정책

- 요청 형식 오류는 `INVALID_PARAMETER`
- 상태 전이 실패는 액션별 `409`
- availability 위반은 `400`
- 시간 블록 충돌은 `409`
- 메뉴/입력값 검증 실패는 표준 비즈니스 에러로 처리
