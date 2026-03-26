# `loadtest auth` 분리 백로그

작성일: `2026-03-25`
관련 작업: `#116 dev-only auth`
상태: `backlog`

## 배경

`#116`에서 로컬 UI 확인용 `dev auth`를 실제 카카오 OAuth 경로와 분리했다.

이 과정에서 기존 `loadtest_code_*` 우회도 함께 제거됐다. 기존 구조에서는 `MockOAuthProvider`, `DelegatingOAuthProvider`가 실제 `/oauth/login/kakao*` 경로 내부에서 테스트 우회를 처리하고 있었다.

## 왜 별도 작업으로 분리해야 하나

- `dev auth`와 `loadtest auth`는 목적이 다르다.
- `dev auth`는 사람이 직접 UI를 확인하기 위한 소량 반복 사용에 가깝다.
- `loadtest auth`는 대량 자동화 요청을 위한 별도 인프라 성격이 강하다.
- 둘을 같은 경로 또는 같은 provider 우회에 섞으면 실제 OAuth 경로에 테스트 우회가 다시 스며든다.

## 목표

실제 `/oauth/login/kakao*` 경로를 유지한 채, `loadtest` 프로필 전용의 별도 인증 경로를 도입한다.

## 기대 방향

- `loadtest` 전용 컨트롤러/서비스로 분리
- 실제 OAuth 경로와 완전 분리
- `loadtest` 프로필에서만 활성화
- 필요하면 secret header 또는 allowlist 같은 추가 가드 적용
- 대량 사용자 시나리오에 맞는 계정/토큰 발급 전략 정의

## 후보 계약

예시:

```http
POST /loadtest/auth/login
POST /loadtest/auth/token
```

세부 계약은 별도 이슈에서 결정한다.

## 포함 범위 후보

- `loadtest` 전용 인증 API
- 대량 테스트용 사용자 식별 전략
- 부하테스트용 초기화/시드 전략
- 프로필 제한 및 보안 가드

## 제외 범위 후보

- 로컬 UI 확인용 `dev auth` 재설계
- 실제 카카오 로그인 경로 변경
- 운영 환경 노출

## 완료 기준 초안

- `loadtest` 환경에서 실제 카카오 OAuth 없이 토큰 발급이 가능하다.
- 실제 `/oauth/login/kakao*` 경로에는 테스트 우회가 남아 있지 않다.
- `dev auth`와 `loadtest auth`의 책임이 분리된다.
- 부하테스트 시나리오에서 재현 가능한 계정/토큰 전략이 문서화된다.
