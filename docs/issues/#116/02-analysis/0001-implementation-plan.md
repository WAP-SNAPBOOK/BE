# `#116 dev-only auth` 구현 계획

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

- `src/main/java/com/example/easybooking/auth/service/AuthService.java`
- `src/main/java/com/example/easybooking/auth/presentation/AuthController.java`
- `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
- `src/main/java/com/example/easybooking/auth/util/DelegatingOAuthProvider.java`
- `src/main/java/com/example/easybooking/auth/util/MockOAuthProvider.java`
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
