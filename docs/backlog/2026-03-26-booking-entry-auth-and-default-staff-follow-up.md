# `booking-entry` 인증/기본 직원 계약 후속 검토

작성일: `2026-03-26`
관련 작업: `#115 링크/채팅 예약 진입에 직원 선택 컨텍스트 추가`
상태: `backlog`

## 배경

`booking-entry` 응답은 예약 시작 전에 필요한 `shopId`, `shopName`, `staffs`, `defaultStaffId`를 내려주기 위해 도입되었다.

검토 중 아래 두 가지 후속 논점이 확인되었다.

- 인증 엔드포인트가 실제 사용자 컨텍스트를 사용하는지
- `defaultStaffId`가 독립적인 계약 가치가 있는지

## 현재 상태

### 1. 인증 방식

- `GET /api/public/shops/{slugOrCode}/booking-entry` 는 공개 경로다.
- `GET /api/v1/shops/{shopId}/booking-entry` 는 `SecurityConfig`에서 인증이 필요한 경로다.
- 다만 컨트롤러와 서비스는 `@RequireAuthenticatedUser`를 받지 않고, 현재 사용자 정보를 사용하지 않는다.

즉 현재 구현은 아래 상태다.

- 토큰이 없으면 `/api/v1/...` 호출 불가
- 토큰이 있으면 호출 가능
- 하지만 호출 사용자가 해당 `shopId`를 볼 수 있는지에 대한 추가 권한 검사는 없음

### 2. `defaultStaffId`

- 현재 `defaultStaffId`는 `staffs`의 첫 번째 직원 `staffId`를 그대로 사용한다.
- 직원 정렬은 현재 `id ASC` 기준이다.

즉 현재 구현에서는 사실상 아래와 같다.

```text
defaultStaffId == staffs[0].staffId
```

## 왜 후속 검토가 필요한가

### 인증 관점

- 만약 `/api/v1/shops/{shopId}/booking-entry`가 단순히 "로그인 사용자 누구나 조회 가능" 이라면 현재 구현으로 충분하다.
- 반대로 점주 전용 조회이거나, 사용자별 응답 차이 또는 audit 목적의 `userId`가 필요하다면 `@RequireAuthenticatedUser`와 추가 권한 검사가 필요하다.

### 계약 관점

- `defaultStaffId`를 유지하면 서버가 "기본 직원 선택 정책"을 소유할 수 있다.
- 하지만 현재 정책이 단순히 첫 번째 직원이라면 프론트가 `staffs[0]`를 선택해도 결과가 같아서 중복 필드가 된다.
- 나중에 `displayOrder`, 대표 직원, 숨김 직원 제외 같은 정책이 생길 계획이 없다면 제거 후보다.

## 검토 항목

### 1. `/api/v1/shops/{shopId}/booking-entry` 권한 의미 확정

- 질문:
  - 로그인 사용자 누구나 조회 가능한가
  - 점주만 조회 가능해야 하는가
  - 사용자별로 다른 응답이 필요한가
- 결정 필요:
  - 현재처럼 Security 레벨 인증만 유지
  - `@RequireAuthenticatedUser`를 받고 서비스에서 권한 검사 추가

### 2. `defaultStaffId` 유지 여부 확정

- 질문:
  - 서버가 기본 선택 정책을 계속 책임질 것인가
  - 프론트가 `staffs[0]`를 기본 선택해도 충분한가
- 결정 필요:
  - `defaultStaffId` 유지
  - `defaultStaffId` 제거 후 `staffs`만 제공

## 완료 기준 초안

- `booking-entry`의 인증 의미가 문서와 코드에서 일치한다.
- `defaultStaffId` 유지/제거 여부가 명시적으로 결정된다.
- 결정 결과에 따라 API 문서, 컨트롤러, 테스트가 함께 정리된다.
