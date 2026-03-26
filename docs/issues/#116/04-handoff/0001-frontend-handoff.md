# `#116` 프론트 전달 문서

작성일: `2026-03-26`
대상 브랜치: `feature/jiseob/#116`
대상 팀: `FE`

## 왜 바뀌었는가

- 로컬 UI 확인을 위해 카카오 실계정을 여러 개 돌려 쓰는 비용이 컸다.
- 기존 `loadtest_code_*` 우회는 실제 `/oauth/login/kakao*` 경로 안에 섞여 있어서 프론트가 공식 계약으로 사용하기 어려웠다.
- 그래서 실제 카카오 OAuth와 분리된 `dev-only auth` 경로를 별도로 추가했다.

## 프론트에서 알아야 할 핵심 변경

### 1. 새 개발용 인증 API

- `POST /dev/auth/login`
- `GET /dev/auth/personas`
- `POST /dev/auth/reset/persona/{personaKey}`

이 세 API는 `local` / `test` 프로필에서만 노출된다.

### 2. 기존 카카오 로그인 경로는 유지

- `POST /oauth/login/kakao`
- `POST /oauth/login/kakao/local`

둘 다 여전히 실제 카카오 OAuth 경로다.

### 3. 예전 `loadtest_code_*` 우회는 제거

- `/oauth/login/kakao*`에 `loadtest_code_*`를 넣어 쓰는 방식은 더 이상 사용하지 않는다.
- 프론트 로컬 확인은 앞으로 `dev auth` API를 기준으로 붙어야 한다.

## 권장 프론트 연동 흐름

### A. 테스트 로그인 버튼 렌더링

1. 개발 모드에서만 `GET /dev/auth/personas` 호출
2. 응답의 `personaKey`, `description`, `signedUp`, `userType`를 사용해 버튼/목록 표시

### B. persona 로그인

`POST /dev/auth/login`

```json
{
  "personaKey": "owner-1"
}
```

응답:

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

처리 규칙:

- `LOGIN_SUCCESS`면 기존 로그인 성공 처리와 **완전히 동일하게** 토큰 저장 및 라우팅
- `SIGNUP_REQUIRED`면 `accessToken` 필드에 들어온 **임시 토큰**으로 기존 회원가입 API 호출

## persona 목록

현재 제공 persona:

- `owner-1`
- `customer-1`
- `customer-2`
- `new-owner-1`
- `new-customer-1`

의미:

- `owner-1`, `customer-1`, `customer-2`: 이미 가입된 계정 확인용
- `new-owner-1`, `new-customer-1`: 회원가입 필요 흐름 확인용

실제 가입 여부는 항상 `GET /dev/auth/personas`의 `signedUp`으로 판단한다.

## 현재 백엔드 전제 / 한계

- `owner-1`, `customer-1`, `customer-2`는 "그런 역할의 persona key"이지, DB에 사용자가 자동 생성돼 있다는 뜻은 아니다.
- 현재 BE 구현은 `providerId` 기준으로 사용자가 DB에 있으면 `LOGIN_SUCCESS`, 없으면 `SIGNUP_REQUIRED`를 반환한다.
- 즉 빈 로컬 DB에서는 `owner-1`도 회원가입 필요 상태로 보일 수 있다.
- 샵/예약 데이터가 들어있는 기존 화면을 바로 확인하려면 별도 seed 또는 bootstrap 후속 작업이 필요하다.
- 프론트는 persona 이름 자체보다 `GET /dev/auth/personas`의 `signedUp` 값을 진실 소스로 보는 편이 안전하다.

## reset 사용법

반복 QA 전에 아래를 호출하면 된다.

```
POST /dev/auth/reset/persona/{personaKey}
```

예:

```
POST /dev/auth/reset/persona/new-owner-1
```

응답:

```json
{
  "personaKey": "new-owner-1",
  "deleted": false,
  "message": "persona user not found"
}
```

```json
{
  "personaKey": "owner-1",
  "deleted": true,
  "message": "persona user deleted"
}
```

## 프론트 구현 시 주의사항

- `role`보다 `userType`을 기준으로 OWNER/CUSTOMER 분기를 유지하는 편이 안전하다.
- `dev auth`는 개발 모드에서만 보이게 하고, 운영/배포 빌드에서는 노출하지 않는다.
- 기존 카카오 로그인 성공 후처리를 재사용하고, dev 전용 토큰 저장 로직을 새로 만들지 않는다.
- 기존 사용자 상태를 전제로 하는 화면은 `signedUp=true` persona가 있어야 바로 검증 가능하다.
- 부하테스트용 인증은 이번 범위가 아니다. 별도 후속 이슈로 처리한다.
