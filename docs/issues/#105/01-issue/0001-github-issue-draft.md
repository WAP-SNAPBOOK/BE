# 예약 생성 요청 `formData` 즉시 제거 및 신규 스키마 전환

Issue: `#999`  
Created: 2026-03-02

---

## 배경/문제 정의

현재 예약 생성 API(`POST /api/reservations`)는 레거시 `formData`에서 `date/time`을 추출하는 구조에 의존하고 있다.

- 문서: `api/04-api-spec-by-flow.md:975`에서 `formData`를 `필수 N`으로 표기
- 구현:
    - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java:92`
    - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java:102,109`
    - `formData.get("date")`, `formData.get("time")` 직접 접근으로 `formData` 누락 시 NPE/500 위험

즉, 현재 계약은 문서/구현이 어긋나며, `formData` 제거 방향과도 불일치한다.

## 목표(Goals)

- `formData`를 예약 생성 요청 계약에서 즉시 제거한다.
- 신규 명시 필드 기반 요청 스키마(`date`, `time`, `requirements`, `imageUrls`)로 전환한다.
- 누락/불완전 입력은 500이 아닌 4xx로 일관 처리한다.
- 문서/구현/테스트를 단일 계약으로 일치시킨다.

## 비목표(Non-goals)

- 예약 API 버전 분리(v2 엔드포인트 신설)
- 메뉴/태그/가용성 도메인 기능 확장
- 예약 상세 응답 필드명 이슈 동시 해결

## 요구사항

### 기능

- 신규 요청 필드 도입: `date`, `time`, `requirements`, `imageUrls`
- `ReservationCreateRequest`에서 `formData`를 제거한다.
- `ReservationService.createReservation()`에서 `formData` 파싱 로직을 제거한다.
- `date`, `time` 등 필수값 누락 시 4xx로 응답한다.
- `formData`에 의존한 기존 요청은 4xx로 실패해야 한다(500 금지).
- API 문서(`api/03-api-spec.md`, `api/04-api-spec-by-flow.md`)에서 `formData`를 제거하고 신규 스키마만 남긴다.

### 비기능(성능/보안/운영)

- 기존 글로벌 예외 매핑 정책을 준수한다.
- 정상 경로 성능 저하가 유의미하지 않아야 한다.
- 이번 변경이 breaking change임을 운영 메모/릴리즈 노트에 명시한다.

## 수용 기준(AC)

- AC-1: 신규 필드(`date`, `time`) 기반 요청으로 예약 생성이 정상 동작한다.
- AC-2: `formData` 기반 기존 요청은 500이 아닌 4xx로 응답한다.
- AC-3: `date` 또는 `time` 누락 시 500이 아닌 4xx로 응답한다.
- AC-4: `api/03-api-spec.md`, `api/04-api-spec-by-flow.md`에서 `formData`가 제거되고 신규 스키마만 명시된다.
- AC-5: 신규 스키마 성공/필수값 누락 실패/레거시 요청 실패 테스트가 추가된다.

## 범위/의존성

- 범위:
    - `ReservationCreateRequest` 스키마 전환(`formData` 제거)
    - `ReservationService.createReservation()` 파싱 로직 전환
    - 예약 생성 API 문서 정합화
- 의존:
    - `GlobalExceptionHandler` 에러 매핑
    - 기존 예약 생성 플로우(`menuSelections` 유지)

## 리스크/운영 메모

- 기존 클라이언트가 `formData`에 의존 중이면 즉시 실패로 전환된다.
- 클라이언트 배포 타이밍과 서버 배포 타이밍 불일치 시 단기 장애 가능성이 있다.
- 에러 코드/메시지 계약을 프론트와 사전 합의하지 않으면 재작업 비용이 증가한다.

## 작업 체크리스트

- [ ] `ReservationCreateRequest` 신규 필드 정의(`date`, `time`, `requirements`, `imageUrls`)
- [ ] `ReservationCreateRequest`에서 `formData` 제거
- [ ] `ReservationService.createReservation()`에서 `formData` 파싱 제거
- [ ] 누락/불완전 입력 검증을 4xx로 정리(500 방지)
- [ ] 테스트 추가(신규 필드 성공, `date/time` 누락 실패, `formData` 기반 요청 실패)
- [ ] `api/03-api-spec.md`에서 `formData` 제거 및 신규 스키마 반영
- [ ] `api/04-api-spec-by-flow.md`에서 `formData` 제거 및 신규 스키마 반영
- [ ] 릴리즈 노트/운영 메모에 breaking change 공지

## 관련 문서

- 문제 스캔: `docs/issues/#999/00-intake/0001-problem-scan.md`
- API 명세(기능별): `api/03-api-spec.md`
- User Flow별 API 명세: `api/04-api-spec-by-flow.md`
