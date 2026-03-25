# `[FEATURE]: 로컬 UI 확인용 dev-only auth API 도입`

Issue: `#116`
Branch: `feature/jiseob/#116`
Updated: `2026-03-25`

## Background

카카오 실계정 다중 로그인 없이도 프론트 UI와 주요 사용자 동작을 반복 확인할 수 있는 개발용 인증 계약이 필요하다.

현재 백엔드에는 `loadtest_code_*` 기반 우회가 실제 OAuth 경로 안에 섞여 있어, 프론트가 안정적으로 사용할 공식 개발 API로 보기 어렵다.

## Goal

실제 카카오 OAuth와 분리된 `dev-only auth API`를 도입해, 개발 환경에서 고정 persona 기반으로 로그인/가입 필요/초기화 흐름을 반복 확인할 수 있게 만든다.

## Scope

- `POST /dev/auth/login`
- `GET /dev/auth/personas`
- `POST /dev/auth/reset/persona/{personaKey}`
- 기존 `AuthResponse` 계약 재사용
- `local` 또는 테스트 전용 프로필 제한

## Out Of Scope

- 운영용 OAuth 흐름 재설계
- 실제 카카오 로그인 자동화
- `reset/all`
- persona별 샘플 데이터 재시드

## Acceptance Criteria

- 실제 `/oauth/login/kakao*` 경로와 분리된 `dev-only` 인증 API가 존재한다.
- `local` 또는 테스트 전용 프로필에서만 노출된다.
- 고정 persona 기준으로 `LOGIN_SUCCESS` / `SIGNUP_REQUIRED` 흐름을 재현할 수 있다.
- persona 단위 reset이 가능하다.
- 프론트가 개발 모드에서 최소 변경으로 붙을 수 있다.
