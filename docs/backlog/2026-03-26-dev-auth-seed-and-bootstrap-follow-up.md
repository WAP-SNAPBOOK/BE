# `dev auth` seed/bootstrap 후속 백로그

작성일: `2026-03-26`
관련 작업: `#116 dev-only auth`
상태: `backlog`

## 배경

`#116`에서 로컬 UI 확인용 `dev auth`를 실제 카카오 OAuth 경로와 분리했다.

현재 `dev auth`는 `personaKey -> providerId` 매핑 후, 해당 `providerId` 사용자가 DB에 있으면 `LOGIN_SUCCESS`, 없으면 `SIGNUP_REQUIRED`를 반환한다.

## 현재 한계

- `owner-1`, `customer-1` 같은 기존 계정용 persona가 "항상 로그인 성공"을 보장하지 않는다.
- 빈 로컬 DB에서는 대부분의 persona가 회원가입 필요 상태로 보일 수 있다.
- 샵/예약/채팅 같은 기존 데이터가 필요한 UI는 `dev auth`만으로 바로 확인하기 어렵다.
- 즉 현재 구현은 "인증 우회"는 제공하지만, "테스트용 기존 사용자 상태"까지는 보장하지 않는다.

## 왜 후속 작업이 필요한가

- 프론트가 점주 홈, 예약 목록, 기존 샵 관리 화면을 빠르게 확인하려면 최소한 일부 persona는 항상 재현 가능한 상태여야 한다.
- `signedUp`이 매번 DB 상태에 따라 달라지면 팀원이 로컬 상태를 맞추는 비용이 커진다.
- 반복 QA를 하려면 로그인뿐 아니라 사용자/샵/예약의 초기 상태까지 일정하게 되돌릴 수 있어야 한다.

## 목표

로컬 개발에서 `dev auth` persona를 사용할 때, 필요한 경우 기존 사용자/샵/예약 상태를 재현 가능한 방식으로 준비하거나 재설정할 수 있게 한다.

## 후보 방향

### 1. 개발용 seed 데이터

- `owner-1`, `customer-1`, `customer-2`에 대응하는 `providerId` 사용자를 미리 생성
- 필요하면 점주용 샵, 기본 메뉴, 예약 샘플도 함께 생성
- 장점: 단순하고 빠름
- 단점: 상태가 틀어지면 복구/재현이 불편할 수 있음

### 2. persona bootstrap API

- 예시:
  - `POST /dev/auth/bootstrap/persona/{personaKey}`
  - `POST /dev/auth/bootstrap/all`
- persona별로 사용자/샵/샘플 데이터를 idempotent하게 재생성
- 장점: 반복 QA와 초기화가 쉬움
- 단점: 구현 범위가 seed보다 큼

### 3. reset + bootstrap 조합

- 기존 `POST /dev/auth/reset/persona/{personaKey}` 유지
- 필요한 persona만 다시 bootstrap
- 가장 유연하지만, 최소 설계 결정이 더 필요함

## 포함 범위 후보

- 기존 persona용 사용자 seed 또는 bootstrap 전략 정의
- 점주 persona용 샵/메뉴/예약 샘플 데이터 범위 정의
- idempotent 재실행 보장
- 로컬/테스트 프로필 제한
- 프론트가 기대할 수 있는 persona 상태 문서화

## 제외 범위 후보

- 부하테스트용 인증/계정 전략
- 실제 카카오 OAuth 경로 변경
- 운영 환경 노출

## 완료 기준 초안

- 최소 1개 이상 점주 persona와 1개 이상 고객 persona가 로컬에서 재현 가능하게 준비된다.
- `GET /dev/auth/personas`의 `signedUp` 상태가 팀원이 기대하는 기준과 문서상 일치한다.
- 샵/예약이 필요한 주요 화면을 카카오 실계정 없이 확인할 수 있다.
- 초기화/재생성 절차가 문서로 정리된다.
