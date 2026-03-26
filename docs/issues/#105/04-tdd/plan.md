# TDD Plan - #105 ReservationCreateRequest `formData` 제거

## Ground rules
- Red -> Green -> Refactor 순서를 반드시 지킨다.
- Tidy First: 구조 변경(요청 DTO/파싱 경계 정리)과 동작 변경(검증/응답 코드 변경)을 분리한다.
- Defect protocol: API 레벨 실패 테스트를 먼저 추가하고, 필요하면 최소 재현 단위 테스트를 추가한 뒤 둘 다 통과시킨다.
- 사용자가 `go`라고 말하면 다음 미체크 테스트 1개만 진행한다.

## Scope

### Goals
- 예약 생성 요청에서 `formData`를 제거하고 신규 명시 필드(`date`, `time`, `requirements`, `imageUrls`)로 전환한다.
- `formData` 기반 레거시 요청은 500이 아닌 4xx로 명확히 실패시킨다.
- 필수값 누락(`date`, `time`)은 4xx로 일관 처리한다.
- 관련 문서(`api/03`, `api/04`)와 코드 계약을 일치시킨다.

### Non-goals
- 예약 API 버전 분리(v2 엔드포인트 신설)
- menuSelections 도메인 확장/리팩토링
- 예약 상세 응답 스키마 변경

## Test list

### [x] 1. API 계약 - 레거시 `formData` 요청 거부
#### ~~1-1. `formData`만 포함한 예약 생성 요청은 4xx를 반환한다~~ [x]
- 목적: 레거시 요청 차단 정책을 API 레벨에서 고정
- 입력: `POST /api/reservations` body에 `shopId`, `staffId`, `formData`만 포함
- 출력: 4xx(정책에 맞는 에러 코드/메시지)
- 엣지케이스: `formData` 내부에 `date/time`이 있어도 실패해야 함
- 관측 포인트: 응답 상태코드, 에러 코드, 500 미발생

### [x] 2. API 계약 - 신규 필드 필수값 검증
#### ~~2-1. `date` 누락 시 4xx를 반환한다~~ [x]
- 목적: 신규 계약의 필수값 검증 고정
- 입력: 신규 스키마 요청에서 `time`만 포함
- 출력: 4xx
- 엣지케이스: `date`가 빈 문자열/형식 오류일 때도 실패
- 관측 포인트: 상태코드, 검증 에러 바디

#### ~~2-2. `time` 누락 시 4xx를 반환한다~~ [x]
- 목적: 신규 계약의 필수값 검증 고정
- 입력: 신규 스키마 요청에서 `date`만 포함
- 출력: 4xx
- 엣지케이스: `time` 형식 오류(HH:mm 아님)
- 관측 포인트: 상태코드, 검증 에러 바디

### [x] 3. API 성공 경로 - 신규 스키마로 예약 생성
#### ~~3-1. 신규 필드 기반 요청으로 예약 생성이 성공한다~~ [x]
- 목적: 새 계약의 기본 성공 경로 보장
- 입력: `shopId`, `staffId`, `date`, `time`, `requirements`, `imageUrls`, `menuSelections`
- 출력: 201 + ReservationResponse
- 엣지케이스: `requirements`/`imageUrls` 생략 또는 빈값 허용 정책 확인
- 관측 포인트: 상태코드 201, 응답의 `date/time/status`, DB 저장 필드

### [x] 4. 서비스 레벨 - `formData` 의존 제거 확인
#### ~~4-1. `createReservation`이 요청 명시 필드만으로 동작한다~~ [x]
- 목적: 서비스 파싱 경로에서 `formData` 참조 제거를 검증
- 입력: 신규 필드가 포함된 ReservationCreateRequest
- 출력: Reservation 생성 성공
- 엣지케이스: optional 필드 null 처리
- 관측 포인트: `date/time`가 요청 필드 기준으로 저장되고 NPE가 발생하지 않음

### [x] 5. 회귀 방지 - 예외 매핑 안정화
#### ~~5-1. 잘못된 요청은 비즈니스/검증 예외로 4xx 매핑된다~~ [x]
- 목적: 500 회귀 방지
- 입력: 필수값 누락/형식 오류/레거시 payload
- 출력: 4xx
- 엣지케이스: 경계 입력(공백 문자열, null)
- 관측 포인트: GlobalExceptionHandler 경유 응답 포맷 일관성

### [x] 6. 문서 정합화
#### ~~6-1. `api/03-api-spec.md`에서 `formData`를 제거하고 신규 스키마로 갱신한다~~ [x]
- 목적: 기능별 API 문서와 구현 계약 동기화
- 입력: 예약 생성 섹션
- 출력: 신규 요청 필드 기준 명세
- 엣지케이스: 예시 payload와 표의 필수값 표기 불일치 제거
- 관측 포인트: 문서 내 `formData` 잔존 여부

#### ~~6-2. `api/04-api-spec-by-flow.md`에서 `formData`를 제거하고 신규 스키마로 갱신한다~~ [x]
- 목적: user flow 문서와 구현 계약 동기화
- 입력: B-4 예약 생성 섹션
- 출력: 신규 요청 필드 기준 명세
- 엣지케이스: 설명/표/예시 간 필드명 불일치 제거
- 관측 포인트: 문서 내 `formData` 잔존 여부

## Notes

### long-running 테스트 제외 기준
- 외부 인프라 의존(E2E, 부하테스트, 장시간 통합)은 본 사이클에서 제외한다.
- 기본은 빠른 API/서비스 레벨 테스트로 계약을 고정하고, 장시간 테스트는 별도 후속 이슈로 분리한다.

### 모호한 요구사항 / 추가 질문
- `requirements`는 기존 `formData.requests`와 1:1 매핑으로 고정할지 확인 필요.
- `imageUrls` 빈 배열 허용 여부와 최대 개수 제한 정책 확인 필요.
- 레거시 요청 실패 시 에러 코드를 신규 코드로 둘지 기존 코드 재사용할지 확정 필요.
- 관련 논의 문서:
  - `docs/issues/#105/00-intake/0001-problem-scan.md`
  - `docs/issues/#105/01-issue/0001-github-issue-draft.md`
