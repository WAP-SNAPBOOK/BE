# `#116 dev-only auth` 작업 로그

작성일: `2026-03-25`
기준 이슈: `#116`
기준 브랜치: `feature/jiseob/#116`

## Entries

### Entry 001

- Date: `2026-03-25 17:23`
- Unit: `c6129aa`
- Type: `Structural`
- What:
  - `BE-116` 브랜치에 현재 구현 규칙과 `docs` 추적용 `.gitignore` 변경을 반영했다.
  - 이전 `30-tidy-first-commit-discipline`를 현재 `30-implementation-discipline` 기준으로 교체했다.
- Why:
  - 현재 이슈 구현을 새 규칙 기준으로 다시 시작해야 해서, 먼저 작업 브랜치의 규칙 기준점을 맞춰야 했다.
  - `docs/issues/#116/...`를 이 워크트리 안에서 추적하려면 `.gitignore` 변경이 선행되어야 한다.
- Verification:
  - `git status --short`가 비어 있는 상태 확인
  - 변경 커밋 생성 확인
- Next:
  - `BE-116/docs/issues/#116` 아래에 issue/plan/worklog를 다시 만들고, 첫 코드 구조 변경 범위를 확정

### Entry 002

- Date: `2026-03-25 17:31`
- Unit: `pre-commit`
- Type: `Structural`
- What:
  - `AuthService`에 `loginByProviderId(String providerId)` 공통 로그인 후처리를 추출했다.
  - 기존 `oAuthLogin()`은 `accessCode -> providerId` 해석만 하고, 실제 로그인 성공/회원가입 필요 분기는 공통 메서드로 위임하도록 정리했다.
- Why:
  - 이후 `dev auth`가 실제 OAuth 경로와 같은 로그인 응답 계약을 재사용하려면, `providerId` 기준 공통 축이 먼저 필요하다.
  - 이 단계에서는 동작을 바꾸지 않고 내부 책임만 분리해야 다음 Behavioral 변경 범위를 줄일 수 있다.
- Verification:
  - `AuthService` 변경 diff 확인
  - 자동 테스트는 다음 단위에서 함께 실행 예정
- Next:
  - `dev auth`용 persona 정의, DTO, 서비스 뼈대를 별도 구조 변경 단위로 추가

### Entry 003

- Date: `2026-03-25 17:36`
- Unit: `pre-commit`
- Type: `Structural`
- What:
  - `auth/dev` 패키지에 persona enum, 로그인/목록/reset DTO, `DevAuthService`를 추가했다.
  - `AuthErrorCode`에 `INVALID_DEV_AUTH_PERSONA`를 추가했다.
  - 아직 컨트롤러와 보안 허용 경로는 연결하지 않았다.
- Why:
  - 다음 Behavioral 단위에서 엔드포인트를 추가할 때, persona 정책과 내부 서비스 계약이 먼저 고정돼 있어야 변경 범위를 좁힐 수 있다.
  - 외부 API 노출 전에 잘못된 `personaKey` 처리와 persona 메타데이터 구조를 먼저 정리하는 편이 리뷰가 쉽다.
- Verification:
  - 신규 클래스/enum/DTO 생성 diff 확인
  - 자동 테스트는 현재 단위 종료 후 실행 예정
- Next:
  - `login`, `personas` 엔드포인트를 추가하고 `local/test` 전용 노출을 연결

### Entry 004

- Date: `2026-03-25 17:40`
- Unit: `pre-commit`
- Type: `Behavioral`
- What:
  - `DevAuthController`를 추가해 `POST /dev/auth/login`, `GET /dev/auth/personas`를 노출했다.
  - `SecurityConfig` 허용 경로에 `/dev/auth/**`를 추가했다.
  - 두 API의 기본 동작을 검증하는 `DevAuthControllerIntegrationTest`를 추가했다.
- Why:
  - 프론트가 바로 붙을 최소 계약은 로그인과 persona 목록이므로, reset과 loadtest 제거보다 먼저 이 두 엔드포인트를 독립적으로 여는 편이 리뷰 범위가 명확하다.
  - profile 제한이 걸린 컨트롤러와 실제 응답 계약을 먼저 테스트로 고정해야 이후 제거 작업을 안전하게 할 수 있다.
- Verification:
  - `./gradlew test --tests "*DevAuthControllerIntegrationTest"` 실행 예정
- Next:
  - `reset/persona`, `loadtest` 제거, 프로필/보안 관련 마무리와 추가 테스트를 진행

### Entry 005

- Date: `2026-03-25 17:45`
- Unit: `pre-commit`
- Type: `Behavioral`
- What:
  - `POST /dev/auth/reset/persona/{personaKey}`를 추가했다.
  - `SecurityConfig`에서 `/oauth/login/kakao/loadtest` 허용 경로를 제거했다.
  - `DelegatingOAuthProvider`, `MockOAuthProvider`를 삭제해 실제 OAuth 경로가 `KakaoOAuthProvider`만 사용하도록 정리했다.
  - reset 동작 테스트와 `@Profile({"local", "test"})` 보장 테스트를 추가했다.
- Why:
  - `#116`의 핵심은 실제 OAuth 우회 제거와 명시적 dev-only API 분리이므로, 마지막 단위에서 남은 우회 경로를 없애야 한다.
  - reset과 프로필 제한 검증을 같이 닫아야 dev auth 계약을 실제로 재사용 가능한 상태로 끝낼 수 있다.
- Verification:
  - `./gradlew test --tests "*DevAuth*"` 실행 예정
- Next:
  - 전체 변경 상태 점검 후 남은 문서/코드 차이를 정리
