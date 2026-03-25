# `[FEATURE]: 로컬 UI 확인용 dev-only auth API 도입`

Issue: `#116`  
Created: `2026-03-24`  
Repository: `WAP-SNAPBOOK/BE`  
Issue URL: `https://github.com/WAP-SNAPBOOK/BE/issues/116`  
Branch: `feature/jiseob/#116`

---

## Background

카카오 실계정 다중 로그인 없이도 프론트 UI와 주요 사용자 동작을 반복 확인할 수 있는 개발용 인증 전략이 필요하다.

현재 백엔드에는 `loadtest_code_*` 기반 우회 로직이 일부 존재하지만, 실제 OAuth 경로와 테스트 우회가 섞여 있어 공식 개발 계약으로 사용하기 어렵다.

## Problem

- 실제 OAuth 경로와 테스트 우회가 같은 진입점에 섞여 있다.
- 테스트 계정 정책이 API 계약으로 드러나지 않아 프론트가 안정적으로 사용하기 어렵다.
- 반복 QA를 위한 reset 경로와 persona 관리 전략이 정리되어 있지 않다.
- 로컬 UI 확인을 위해 카카오 계정을 여러 개 관리해야 하는 비용이 크다.

## Goal

실제 OAuth 흐름과 분리된 `dev-only` 인증 API를 도입해, 프론트가 개발 환경에서 고정 persona를 사용해 로컬 UI와 핵심 동작을 반복 확인할 수 있도록 한다.

## Scope

- `dev-only` 인증 API 계약 정의
- `AuthResponse` 호환 응답 구조 유지
- 고정 persona 및 `providerId` 매핑 전략 도입
- reset API 범위 정의
- 프론트가 최소 변경으로 붙을 수 있는 연동 기준 제공
- 이후 `Playwright` 회귀 자동화의 기반 마련

## Out Of Scope

- 운영용 카카오 로그인 흐름 재설계
- 실제 카카오 OAuth 자동화
- 사용자 관리용 별도 운영 UI

## Acceptance Criteria

- [ ] 실제 `/oauth/login/kakao*` 경로와 분리된 `dev-only` 인증 API가 정의된다.
- [ ] `local` 또는 전용 프로필에서만 노출되도록 제한된다.
- [ ] 고정 persona 기반으로 `LOGIN_SUCCESS` / `SIGNUP_REQUIRED` 흐름을 재현할 수 있다.
- [ ] 반복 테스트를 위한 reset API 범위가 정의된다.
- [ ] 프론트가 개발 모드에서만 사용할 최소 연동 방식이 정리된다.
- [ ] 이후 `Playwright`가 실제 카카오 로그인 대신 이 계약을 사용할 수 있다.

## Risks

- 개발용 인증 경로가 운영 환경에 노출될 수 있다.
- persona 데이터가 장기적으로 오염될 수 있다.
- 프론트에 dev 전용 분기가 과도하게 생길 수 있다.

## References

- 원본 계획 문서: `docs/refactor/testing/2026-03-24-local-ui-dev-auth-plan.md`
