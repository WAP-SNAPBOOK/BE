# `#116 dev-only auth` 구현 계획

<<<<<<< feature/jiseob/#116
작성일: `2026-03-25`
기준 이슈: `#116`
기준 브랜치: `feature/jiseob/#116`

## 목표

실제 카카오 OAuth 경로와 분리된 `dev-only auth API`를 도입하고, 기존 로그인 응답 계약을 유지한 채 로컬 UI 확인용 고정 persona 흐름을 제공한다.

## 포함 범위

- `AuthService`의 공통 로그인 후처리 분리
- `dev auth`용 persona/DTO/서비스 도입
- `POST /dev/auth/login`
- `GET /dev/auth/personas`
- `POST /dev/auth/reset/persona/{personaKey}`
- `loadtest_code_*` 우회 제거
- 최소 통합 테스트 추가

## 제외 범위

- `reset/all`
- persona별 샘플 데이터 시드
- 운영 환경용 추가 보호 장치
- 프론트 구현

## 단계별 구현 순서

1. `BE-116` 브랜치의 규칙/.gitignore 상태를 현재 기준으로 정리한다.
2. `AuthService`에서 `providerId` 기반 공통 로그인 후처리를 분리한다.
3. `dev auth`용 persona 정의와 서비스/DTO 뼈대를 추가한다.
4. `login`, `personas` 엔드포인트를 `local/test` 전용으로 추가한다.
5. `reset/persona`, 보안 허용 경로 조정, `loadtest` 우회 제거를 반영한다.
6. `DevAuth` 관련 테스트를 추가하고 최소 경로를 검증한다.

## 예상 파일
=======
작성일: `2026-03-24`  
기준 이슈: `#116`  
관련 브랜치: `feature/jiseob/#116`

## 목적

실제 카카오 OAuth 경로와 분리된 `dev-only auth API`를 도입해,
프론트가 로컬 개발 환경에서 고정 persona로 로그인/가입/초기화를 반복 확인할 수 있게 만든다.

이번 구현의 핵심은 아래 두 가지다.

1. 기존 `/oauth/login/kakao*` 경로에서 `loadtest` 우회를 제거한다.
2. `local` 전용 `/dev/auth/*` 경로를 추가해 `AuthResponse` 호환 계약을 제공한다.

## 구현 원칙

- 문서는 루트 `BE(develop)` 기준 `docs/...`에 남긴다.
- 구조 변경과 동작 변경을 나눈다.
- `dev auth`는 `local` 프로필에서만 노출한다.
- 프론트는 새 후처리를 만들지 않고 기존 로그인 후처리를 재사용할 수 있어야 한다.
- 초기 범위는 `login`, `personas`, `reset/persona`까지만 잡고, `reset/all`과 시드는 후순위로 둔다.

## 현재 코드 기준 핵심 문제

- `DelegatingOAuthProvider`가 `loadtest_code_*`를 실제 OAuth 경로 내부에서 우회 처리한다.
- `SecurityConfig`에 `/oauth/login/kakao/loadtest`가 열려 있지만 실제 컨트롤러 매핑은 없다.
- 테스트용 로그인 계약이 명시적 API가 아니라 내부 문자열 규칙으로 숨어 있다.
- `DevController`는 존재하지만 인증/초기화 계약을 담당하는 구조는 아니다.

## 목표 범위

### 포함

- `POST /dev/auth/login`
- `GET /dev/auth/personas`
- `POST /dev/auth/reset/persona/{personaKey}`
- persona 고정 매핑
- 기존 `AuthResponse` 재사용
- `local` 프로필 제한
- 최소 테스트 추가

### 제외

- `POST /dev/auth/reset/all`
- persona별 샘플 데이터 재시드
- 운영/배포 환경에서의 `dev auth` 노출
- 실제 카카오 로그인 흐름 재설계

## 구현 단계

### 1단계. 구조 정리

먼저 실제 OAuth 경로와 dev auth 경로가 같은 진입점을 공유하지 않도록 구조를 정리한다.

- [x] `AuthService`에 `providerId` 기반 공통 로그인 처리 메서드를 분리한다.
- [x] OAuth 경로는 `accessCode -> providerId` 해석만 담당하게 좁힌다.
- [x] `DelegatingOAuthProvider`, `MockOAuthProvider`, `loadtest_code_*` 의존을 제거한다.
- [x] `SecurityConfig`에서 `/oauth/login/kakao/loadtest` 허용 경로를 제거한다.

예상 결과:

- `/oauth/login/kakao*` 는 실제 카카오 OAuth만 처리한다.
- `dev auth`는 별도 컨트롤러/서비스에서만 처리된다.

### 2단계. dev auth 모델 도입

고정 persona를 코드에서 명시적으로 관리한다.

- [x] `auth/dev` 패키지에 persona 정의를 둔다.
- [x] persona는 최소 아래 정보를 가진다.
  - `personaKey`
  - `providerId`
  - `userType`
  - `description`
- 초기 persona 후보:
  - `owner-1`
  - `customer-1`
  - `customer-2`
  - `new-owner-1`
  - `new-customer-1`

결정:

- 초기 버전은 설정 파일보다 코드 상수/enum이 단순하다.
- `providerId`는 숫자 강제가 아니라 문자열로 둔다.

### 3단계. dev auth API 추가

`local` 프로필에서만 활성화되는 컨트롤러와 서비스를 추가한다.

예정 계약:

```http
POST /dev/auth/login
GET /dev/auth/personas
POST /dev/auth/reset/persona/{personaKey}
```

세부 동작:

- `POST /dev/auth/login`
  - persona의 `providerId`로 공통 로그인 로직 호출
  - 기존 유저면 `LOGIN_SUCCESS`
  - 미가입 유저면 `SIGNUP_REQUIRED`
- `GET /dev/auth/personas`
  - persona 목록과 `signedUp` 상태 반환
- `POST /dev/auth/reset/persona/{personaKey}`
  - 가입된 유저가 있으면 `UserCleanupService`로 연관 데이터까지 정리
  - 미가입 persona면 no-op 또는 성공 응답

### 4단계. 기존 가입 플로우 연결 확인

`SIGNUP_REQUIRED` 응답 뒤 기존 가입 API가 그대로 동작해야 한다.

검증 포인트:

- [x] temp token subject가 문자열 `providerId`여도 기존 가입 로직이 문제없이 동작하는 구조인지 확인
- [x] `user/customer/signup`, `user/owner/signup` 이 `dev auth` temp token으로도 재사용 가능한 구조로 유지

### 5단계. 테스트 추가

최소 아래 검증은 자동화한다.

- [x] persona가 기존 유저일 때 `LOGIN_SUCCESS`
- [x] persona가 미가입 유저일 때 `SIGNUP_REQUIRED`
- [x] `GET /dev/auth/personas` 에서 `signedUp` 상태가 반영됨
- [x] `POST /dev/auth/reset/persona/{personaKey}` 호출 시 가입 유저가 삭제됨
- [x] `/dev/auth/*` 가 `local` 전용이라는 점이 최소 한 군데에서 보장됨

## 파일 단위 작업 후보
>>>>>>> develop

- `src/main/java/com/example/easybooking/auth/service/AuthService.java`
- `src/main/java/com/example/easybooking/auth/presentation/AuthController.java`
- `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
- `src/main/java/com/example/easybooking/auth/util/DelegatingOAuthProvider.java`
- `src/main/java/com/example/easybooking/auth/util/MockOAuthProvider.java`
<<<<<<< feature/jiseob/#116
- `src/main/java/com/example/easybooking/errors/errorcode/AuthErrorCode.java`
- 신규 `src/main/java/com/example/easybooking/auth/dev/...`
- 신규 `src/test/java/com/example/easybooking/auth/dev/...`

## 리스크

- `dev auth` 경로가 의도치 않게 다른 프로필에 노출될 수 있다.
- `loadtest` 제거 과정에서 기존 로컬 로그인 경로가 회귀할 수 있다.
- reset 범위가 넓어 연관 데이터 삭제가 과할 수 있다.

## 완료 기준

- `/oauth/login/kakao*` 경로에서 `loadtest` 우회가 제거된다.
- `local/test` 전용 `dev auth` API가 동작한다.
- 기존 사용자와 미가입 persona에 대해 각각 `LOGIN_SUCCESS`, `SIGNUP_REQUIRED`가 반환된다.
- persona 목록과 reset API가 동작한다.
- 관련 테스트가 통과한다.
=======
- `src/main/java/com/example/easybooking/user/presentation/DevController.java`
- `src/main/java/com/example/easybooking/user/service/UserCleanupService.java`
- `src/main/resources/application.yml`
- 신규 `src/main/java/com/example/easybooking/auth/dev/...`
- 신규 `src/test/java/.../auth/dev/...`

## 커밋 분해 기준

### Structural

- 공통 로그인 로직 추출
- `loadtest` 우회 제거
- persona 모델/DTO/서비스 뼈대 추가

### Behavioral

- `/dev/auth/login`
- `/dev/auth/personas`
- `/dev/auth/reset/persona/{personaKey}`
- 테스트 추가

## 리스크와 대응

### 리스크 1. local 전용 API가 다른 프로필에 노출될 수 있음

대응:

- 컨트롤러 또는 구성 클래스에 `@Profile("local")`
- `SecurityConfig` 허용 경로는 추가하되, 실제 빈 생성도 `local`로 제한

### 리스크 2. 기존 OAuth 로그인 회귀

대응:

- OAuth 경로는 가능한 한 `providerId` 변환 전 단계만 건드린다.
- 실제 로그인 성공/임시 토큰 발급 로직은 공통 메서드로 재사용한다.

### 리스크 3. reset 범위 과다

대응:

- 초기 버전은 `reset/persona/{key}`만 제공한다.
- `reset/all`은 후속 이슈로 미룬다.

## 완료 기준

- [x] `/oauth/login/kakao*` 에서 `loadtest` 분기가 제거된다.
- [x] `local` 전용 `dev auth` API가 추가된다.
- [x] 최소 5개 persona가 코드에서 명시적으로 관리된다.
- [x] `AuthResponse` 계약을 그대로 재사용한다.
- [x] persona별 로그인/미가입/초기화 테스트가 추가된다.
- [x] 루트 `BE(develop)`에서 이슈 문서와 구현 계획을 항상 확인할 수 있다.
>>>>>>> develop
