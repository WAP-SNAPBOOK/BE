# `#116 dev-only auth` 작업 로그

작성일: `2026-03-24`  
기준 이슈: `#116`  
관련 브랜치: `feature/jiseob/#116`

## Entries

### Entry 001

- Date: `2026-03-24 19:15`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `docs/issues/#116/...`
  - `.cursor/rules/...`
  - `AGENTS.md`
- What:
  - `#116` 이슈 초안과 구현 계획 문서를 루트 `develop` 기준 `docs/issues/#116/...` 아래로 정리했다.
  - 문서가 항상 루트 `BE(develop)`에서 보이도록 문서 기준 위치를 정리했다.
  - 이슈 기반 작업에 대해 plan/worklog를 남기는 규칙과 템플릿을 추가했다.
- Why:
  - 구현 전에 작업 단위와 기록 방식을 먼저 고정해야 이후 커밋 분해와 리뷰 흐름이 흔들리지 않는다.
  - 이번 이슈는 여러 파일에 걸친 API 분리 작업이라 단순 구현보다 문서 기준점이 먼저 필요하다.
- Verification:
  - 루트 `BE`에서 `docs/issues/#116/01-issue/...`, `docs/issues/#116/02-analysis/...` 문서 조회 확인
  - 새 rule/템플릿 파일 생성 확인
  - 자동 테스트는 아직 미실행
- Next:
  - `loadtest` 우회 제거와 `dev auth` 경로 분리를 위한 구조 정리 범위를 코드 기준으로 확정

### Entry 002

- Date: `2026-03-24 23:01`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `src/main/java/com/example/easybooking/auth/service/AuthService.java`
  - `src/main/java/com/example/easybooking/auth/dev/...`
  - `src/main/java/com/example/easybooking/errors/errorcode/AuthErrorCode.java`
- What:
  - `AuthService`에 `loginByProviderId()` 공통 로그인 처리를 추출했다.
  - `dev auth`용 persona enum, request/response DTO, 서비스 뼈대를 추가했다.
  - 잘못된 `personaKey`를 400으로 처리하기 위한 `INVALID_DEV_AUTH_PERSONA` 오류 코드를 추가했다.
- Why:
  - 실제 OAuth 흐름과 dev auth 흐름이 같은 로그인 후처리를 재사용하도록 만들기 위해 공통 축이 먼저 필요했다.
  - 엔드포인트를 붙이기 전에 내부 모델과 오류 체계를 먼저 고정해야 이후 동작 변경 범위가 명확해진다.
- Verification:
  - 변경 파일 기준 diff 확인
  - 자동 테스트는 이 단계에서는 아직 미실행
- Next:
  - `/dev/auth/*` 엔드포인트 추가, 보안 허용 경로 조정, 기존 `loadtest` 우회 제거

### Entry 003

- Date: `2026-03-24 23:05`
- Unit: `pre-commit`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/auth/dev/presentation/DevAuthController.java`
  - `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
  - `src/main/java/com/example/easybooking/auth/util/DelegatingOAuthProvider.java`
  - `src/main/java/com/example/easybooking/auth/util/MockOAuthProvider.java`
  - `src/test/java/com/example/easybooking/auth/dev/...`
- What:
  - `local/test` 프로필 전용 `/dev/auth/login`, `/dev/auth/personas`, `/dev/auth/reset/persona/{personaKey}` 엔드포인트를 추가했다.
  - 보안 허용 경로에 `/dev/auth/**`를 추가하고, 사용되지 않는 `/oauth/login/kakao/loadtest` 허용 경로를 제거했다.
  - 기존 `loadtest_code_*` 우회 클래스 두 개를 삭제했다.
  - `DevAuth` 전용 통합 테스트와 설정 테스트를 추가했다.
- Why:
  - 테스트용 로그인 계약을 실제 OAuth 경로와 분리해야 프론트가 내부 문자열 규칙 없이 명시적 API로 붙을 수 있다.
  - 허용 경로와 프로필 제한, 실제 응답 계약, 사용자 초기화까지 함께 검증해야 dev-only 인증 계약으로 쓸 수 있다.
- Verification:
  - `./gradlew test --tests "*DevAuth*"` 실행 통과
- Next:
  - 필요 시 `reset/all` 또는 persona seed 전략을 후속 이슈로 분리

### Entry 004

- Date: `2026-04-10 14:20`
- Unit: `pre-commit`
- Type: `Behavioral`
- Scope:
  - `docs/issues/#116/02-analysis/0001-implementation-plan.md`
  - `src/main/java/com/example/easybooking/user/service/UserCleanupService.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/...`
  - `src/test/java/com/example/easybooking/user/service/UserCleanupServiceTest.java`
- What:
  - 사용자 강제 삭제 시 `reservations`를 바로 bulk delete 하지 않고, 예약 ID 기준으로 하위 예약 메뉴 입력값, 예약 메뉴, 예약 타임블록을 먼저 삭제한 뒤 예약 엔티티를 삭제하도록 정리했다.
  - 이번 hotfix 범위와 검증 기준을 `#116` 구현 계획에 추가했다.
  - 삭제 순서를 검증하는 `UserCleanupService` 회귀 테스트를 추가했다.
- Why:
  - `DELETE /dev/user` 실행 시 `reservation_menu_items.reservation_id -> reservations.id` FK 때문에 예약 삭제가 실패하고 있었다.
  - 동일한 패턴으로 `reservation_time_blocks`도 같은 예외를 만들 수 있어, 예약 하위 테이블을 명시적으로 먼저 정리해야 했다.
- Verification:
  - `./gradlew test --tests "com.example.easybooking.user.service.UserCleanupServiceTest"` 실행 통과
- Next:
  - 실제 `DELETE /dev/user` 경로에서 예약 하위 데이터가 있는 계정으로 재확인

### Entry 005

- Date: `2026-04-15 09:43`
- Unit: `pre-commit`
- Type: `Behavioral`
- Scope:
  - `docs/issues/#116/02-analysis/0001-implementation-plan.md`
  - `src/main/java/com/example/easybooking/user/service/UserCleanupService.java`
  - `src/main/java/com/example/easybooking/shop/repository/ShopMenuRepository.java`
  - `src/main/java/com/example/easybooking/shop/repository/ShopMenuTagRepository.java`
  - `src/main/java/com/example/easybooking/shop/repository/ShopMenuInputFieldRepository.java`
  - `src/test/java/com/example/easybooking/user/service/UserCleanupServiceTest.java`
- What:
  - 점주 사용자 삭제 시 `shop` 하위 리소스를 별도 정리 단계로 분리했다.
  - `shop_menus` 삭제 전에 `shop_menu_tags`, `shop_menu_input_fields`를 먼저 지우도록 repository 배치 삭제 메서드를 추가했다.
  - `shop_tags`, `shop_holidays`, `shop_operating_times`, `shop_settings`, `staff_operating_times`, `staff`도 `shop` 삭제 전에 정리하도록 `UserCleanupService`를 확장했다.
  - 점주 소유 리소스가 `shopRepository.deleteByOwnerId()` 전에 삭제되는지 검증하는 회귀 테스트를 추가했다.
- Why:
  - 기존 구현은 예약/채팅/폼/슬롯까지만 정리해서, 점주가 운영 설정/직원/메뉴/태그 데이터를 가진 경우 `shop` 삭제 시 FK 예외가 남을 수 있었다.
  - 점주 삭제를 안전하게 하려면 `shop` 하위 자원을 순서대로 비운 뒤 `shop`과 `user`를 삭제해야 한다.
- Verification:
  - `./gradlew test --tests "com.example.easybooking.user.service.UserCleanupServiceTest"` 실행 통과
- Next:
  - 필요 시 `DELETE /dev/user` 또는 persona reset 경로의 통합 시나리오를 별도 테스트로 추가 검토
