# 로컬 UI 확인을 위한 `dev-only` 인증/테스트 전략

작성일: `2026-03-24`
기준 저장소: `BE`

## 목적

카카오 실계정 다중 로그인 없이도 프론트 UI와 주요 사용자 동작을 반복 확인할 수 있는
개발용 인증 전략을 정리한다.

이 문서의 목표는 아래 세 가지다.

1. 백엔드가 어떤 테스트용 인증 계약을 제공해야 하는지 정리한다.
2. 프론트가 어느 정도까지 최소 변경으로 붙을 수 있는지 정리한다.
3. 이후 `Playwright`를 어떤 역할로 도입해야 하는지 순서를 정한다.

## 현재 상황 요약

현재 백엔드에는 아래 인증 관련 동선이 있다.

- `POST /oauth/login/kakao`
- `POST /oauth/login/kakao/local`
- `loadtest_code_*` 형식의 `accessCode`를 받으면 mock provider로 우회

관련 파일:

- `src/main/java/com/example/easybooking/auth/presentation/AuthController.java`
- `src/main/java/com/example/easybooking/auth/util/DelegatingOAuthProvider.java`
- `src/main/java/com/example/easybooking/auth/util/MockOAuthProvider.java`
- `src/main/java/com/example/easybooking/auth/SecurityConfig.java`

회원가입 흐름은 아래 계약을 사용한다.

- 로그인 결과가 기존 유저면 `LOGIN_SUCCESS`
- 신규 유저면 `SIGNUP_REQUIRED`
- 이후 `POST /user/customer/signup` 또는 `POST /user/owner/signup` 호출

즉, "가짜 인가코드 -> 임시 토큰 발급 -> 가입/로그인" 흐름 자체는 이미 일부 가능하다.

## 현재 방식의 문제

지금 구조를 그대로 프론트 개발 편의 기능으로 쓰는 것은 권장하지 않는다.

이유는 아래와 같다.

1. 실제 OAuth 경로와 테스트 우회가 같은 진입점에 섞여 있다.
2. `loadtest_code_*` 규칙이 컨트롤러 계약이 아니라 내부 구현에 숨어 있다.
3. `SecurityConfig`에는 `/oauth/login/kakao/loadtest`가 열려 있지만 실제 컨트롤러 매핑은 없다.
4. 테스트 계정 정책이 API 계약으로 드러나지 않아 프론트가 안정적으로 사용하기 어렵다.
5. 추후 운영/개발 경계를 분리하기 어렵고, 인증 우회가 남아 있는 구조로 오해될 수 있다.

정리하면, 현재 방식은 "임시 우회"로는 쓸 수 있어도 "로컬 UI 확인용 공식 개발 계약"으로는 부족하다.

## 권장 방향

핵심 방향은 아래 한 줄이다.

`실제 OAuth 흐름과 분리된 dev-only 인증 API를 만들고, 프론트는 개발 환경에서만 그 API를 사용한다.`

구체적으로는 아래처럼 나눈다.

- 백엔드: `local` 또는 `dev-auth` 전용 프로필에서만 노출되는 `dev auth API` 제공
- 프론트: 개발 모드에서만 보이는 `테스트 로그인` UI 제공
- 자동화: 이후 `Playwright`는 실제 카카오 로그인 대신 이 `dev auth API`를 사용해 세션 확보

## 왜 이 방향이 맞는가

### 1. 실제 로그인 계약을 보존할 수 있다

`AuthResponse`를 그대로 재사용하면 프론트는 기존 로그인 처리 코드를 거의 그대로 쓸 수 있다.

- 기존 회원: `LOGIN_SUCCESS`
- 신규 회원: `SIGNUP_REQUIRED`

즉, 프론트는 "카카오 로그인"과 "개발용 로그인"의 성공 후처리를 따로 만들 필요가 없다.

### 2. 계정 전환 비용이 크게 줄어든다

실계정 기반 테스트는 아래 비용이 크다.

- 계정 생성/관리
- 브라우저 세션 분리
- 로그아웃/재로그인 반복
- 모바일/PC 카카오 인증 상태 차이

반면 개발용 persona를 두면 버튼 클릭만으로 역할 전환이 된다.

### 3. `Playwright`가 제 역할을 하게 된다

`Playwright`는 "실제 카카오 OAuth를 자동화"하는 데 쓰기보다,
"이미 로그인된 상태에서 핵심 플로우 회귀를 검사"하는 데 쓰는 것이 훨씬 안정적이다.

## 제안하는 API 계약

### 1. 개발용 로그인

```http
POST /dev/auth/login
Content-Type: application/json

{
  "personaKey": "owner-1"
}
```

성공 응답은 기존 `AuthResponse`와 동일한 형태를 사용한다.

예상 persona 예시:

- `owner-1`
- `customer-1`
- `customer-2`
- `new-owner-1`
- `new-customer-1`

동작 규칙:

- 기존에 가입된 persona면 `LOGIN_SUCCESS`
- 아직 가입되지 않은 persona면 `SIGNUP_REQUIRED`
- `SIGNUP_REQUIRED`일 때는 기존 가입 API를 그대로 사용

### 2. 개발용 초기화

반복 QA를 위해 로그인보다 더 중요한 것은 초기화다.

예시:

```http
POST /dev/auth/reset
Content-Type: application/json

{
  "scope": "persona",
  "personaKey": "owner-1"
}
```

또는 더 단순하게 아래처럼 나눌 수 있다.

- `POST /dev/auth/reset/persona/{personaKey}`
- `POST /dev/auth/reset/all`

초기화 범위는 팀 상황에 맞게 선택한다.

- 최소: 해당 유저와 연관된 예약/채팅/샵 데이터 삭제
- 확장: 샘플 샵/메뉴/태그 재시드

### 3. 선택 사항: 현재 상태 조회

프론트 개발 편의상 아래 API가 있으면 유용하다.

```http
GET /dev/auth/personas
```

응답 예시:

```json
[
  {
    "personaKey": "owner-1",
    "role": "OWNER",
    "signedUp": true,
    "description": "기본 점주 테스트 계정"
  },
  {
    "personaKey": "new-customer-1",
    "role": "CUSTOMER",
    "signedUp": false,
    "description": "회원가입 플로우 확인용 계정"
  }
]
```

이 API는 필수는 아니지만 프론트 버튼 라벨, 상태 표시, QA 공유에 도움이 된다.

## persona 설계 원칙

테스트 계정은 무한 생성보다 "고정 persona"가 낫다.

권장 원칙:

1. 역할별 대표 계정을 소수만 유지한다.
2. persona마다 `providerId`를 고정한다.
3. 필요한 경우 샘플 데이터도 persona 단위로 고정한다.
4. 신규 가입 테스트용 persona는 일부러 미가입 상태를 유지한다.

예시:

| personaKey | 용도 | 초기 상태 |
| --- | --- | --- |
| `owner-1` | 점주 대시보드 확인 | 가입 완료, 샵 보유 |
| `customer-1` | 예약 생성/조회 확인 | 가입 완료 |
| `customer-2` | 점주와 다른 고객 경계 확인 | 가입 완료 |
| `new-owner-1` | 점주 가입 플로우 확인 | 미가입 |
| `new-customer-1` | 고객 가입 플로우 확인 | 미가입 |

## 백엔드 작업 범위

### 반드시 할 일

1. 현재 `loadtest_code_*` 우회 로직을 공식 `dev auth` API로 분리
2. `dev auth` API를 `local` 전용 프로필로 제한
3. persona와 `providerId` 매핑을 코드 또는 설정으로 명시
4. 응답을 기존 `AuthResponse`와 호환되게 유지
5. 반복 테스트용 reset API 제공

### 가능하면 같이 할 일

1. persona 목록 조회 API 제공
2. 샘플 데이터 시드 전략 정리
3. `DevController`와 역할이 겹치는 관리 API 정리

### 하지 않는 것이 좋은 일

1. 실제 `/oauth/login/kakao*` 경로 안에 테스트 분기를 계속 추가하는 것
2. `accessCode` 문자열 규칙을 프론트와 암묵적으로 공유하는 것
3. 운영 프로필에서도 개발용 인증 경로를 열어두는 것

## 프론트 작업 범위

프론트 작업은 필요하지만 크지 않아야 한다.

최소 범위는 아래 정도면 충분하다.

1. 개발 모드에서만 보이는 `테스트 로그인` UI 추가
2. persona 버튼 클릭 시 `POST /dev/auth/login`
3. 응답 토큰을 기존 로그인 후처리와 동일하게 저장
4. `SIGNUP_REQUIRED`면 기존 가입 화면으로 이동

예시 UI:

- `점주1로 로그인`
- `고객1로 로그인`
- `신규 고객으로 시작`

즉, 프론트는 "새 인증 시스템"을 만드는 것이 아니라
"개발 모드에서 호출할 인증 진입점 하나 추가" 정도로 끝내는 것이 맞다.

## `Playwright`는 어디에 두는가

`Playwright`는 2순위다.

먼저 해야 할 일:

1. `dev-only` 인증 API 확정
2. 프론트 개발용 로그인 버튼 연결
3. 로컬에서 점주/고객 핵심 동선 수동 확인 가능 상태 확보

그 다음 할 일:

1. `Playwright`에서 persona 로그인 유틸 작성
2. 로그인 후 핵심 시나리오 3~5개 자동화

권장 자동화 범위:

- 점주 로그인 -> 샵/메뉴 수정
- 고객 로그인 -> 예약 생성
- 점주 로그인 -> 예약 승인 또는 거절
- 고객 로그인 -> 내 예약 확인

즉, `Playwright`는 "로그인 문제 해결책"이 아니라
"로그인 문제를 dev auth로 해결한 뒤 회귀 자동화하는 도구"로 보는 것이 맞다.

## 단계별 진행 순서

### 1단계. 백엔드 계약 확정

- `POST /dev/auth/login`
- `POST /dev/auth/reset/...`
- 필요 시 `GET /dev/auth/personas`

이 단계에서 가장 중요한 것은
"프론트가 암묵 규칙 없이 붙을 수 있는 명시적 계약"을 만드는 것이다.

### 2단계. 백엔드 최소 구현

- persona 매핑
- 기존 `AuthResponse` 호환 응답
- 프로필 제한
- 기본 reset 로직

### 3단계. 프론트 최소 연동

- 개발 환경에서만 테스트 로그인 버튼 노출
- 기존 토큰 저장 로직 재사용
- 가입 필요 시 기존 플로우 연결

### 4단계. 수동 QA 기준선 확보

아래가 반복 가능해지면 성공이다.

- 점주 계정으로 로그인
- 고객 계정으로 로그인
- 신규 계정으로 가입 플로우 진입
- 필요 시 초기화 후 다시 반복

### 5단계. `Playwright` 도입

- persona 로그인 유틸 추가
- 핵심 흐름 자동화
- CI 또는 로컬 회귀 세트로 정리

## 리스크와 대응

### 리스크 1. 개발용 인증이 운영 경로로 오해될 수 있음

대응:

- `@Profile("local")` 또는 전용 프로필 사용
- URL도 `/dev/auth/*`로 명확히 분리
- 실제 OAuth 경로와 코드 레벨로 분리

### 리스크 2. persona 데이터가 장기적으로 오염될 수 있음

대응:

- reset API 제공
- 필요 시 persona별 seed 데이터 재구성
- QA 시나리오를 persona 기준으로 고정

### 리스크 3. 프론트가 dev auth 전용 분기를 너무 많이 가질 수 있음

대응:

- 응답 형식을 기존 로그인과 동일하게 유지
- 프론트는 "진입점만 다르고 후처리는 동일"하게 설계

## 권장하지 않는 대안

### 대안 1. 카카오 계정을 여러 개 만들어 계속 수동 테스트

단점:

- 반복 비용이 크다
- 세션 전환이 번거롭다
- 팀 내 공유가 어렵다

### 대안 2. 현재 `loadtest_code_*` 우회를 그대로 사용

단점:

- 공식 계약이 아니다
- 실제 OAuth와 테스트 우회가 섞인다
- 프론트에 내부 구현 규칙이 노출된다

### 대안 3. `Playwright`로 실제 카카오 로그인까지 자동화

단점:

- 외부 인증 의존성이 크다
- flaky 테스트가 되기 쉽다
- 로컬 개발 속도 개선 효과가 작다

## 최종 권장안

가장 실용적인 순서는 아래와 같다.

1. 백엔드가 `dev-only` 인증 API와 초기화 API를 만든다.
2. 프론트는 개발 모드에서만 테스트 로그인 버튼을 붙인다.
3. 점주/고객/신규 가입 시나리오를 persona 기반으로 수동 확인 가능하게 만든다.
4. 그 위에 `Playwright`를 붙여 핵심 회귀 시나리오를 자동화한다.

한 줄 요약:

`지금 먼저 필요한 것은 Playwright가 아니라, 프론트가 작게 붙을 수 있는 명시적 dev auth 계약이다.`
