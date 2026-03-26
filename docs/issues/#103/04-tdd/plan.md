# TDD Plan - #103 ShopSettings 온보딩 초기화 및 Schedule Interval 정합성

## Ground Rules

- Red -> Green -> Refactor 순서를 지킨다.
- Refactor는 모든 테스트가 Green일 때만 수행한다.
- Tidy First 원칙을 적용해 구조 변경과 동작 변경을 같은 단계/커밋에 섞지 않는다.
- Defect protocol:
    1. API 레벨 failing test를 먼저 추가한다.
    2. 필요하면 원인을 최소 재현하는 단위/슬라이스 테스트를 추가한다.
    3. 두 테스트를 모두 Green으로 만든다.

---

## Scope

### Goals

- `POST /shop` 직후 `shop_settings` 기본 row가 자동으로 준비된다.
- 점주 Schedule API 호출 시 `SHOP_SETTINGS_NOT_FOUND`가 온보딩 표준 경로에서 재발하지 않는다.
- `PUT /schedule/interval`은 `intervalMinutes` 단일 리소스 갱신으로 동작한다.
- 정책 필드(`bookingWindowDays`, `minBookingLeadMinutes`, `publicHolidayOff`)는 후속 `booking-policy` API로 분리한다.

### Non-goals

- 운영 DB 과거 데이터 backfill
- 신규 도메인(StaffHoliday 등) 추가
- Availability 응답 스펙 확장

---

## 테스트 체크리스트

### [x] 1. Domain - ShopSettings interval 갱신 규칙

#### ~~1-1. updateInterval이 intervalMinutes를 변경한다~~ [x]

- 목적: interval 전용 API의 핵심 도메인 동작을 고정한다.
- 입력: 기본값 `ShopSettings` + `interval=60`
- 출력: `intervalMinutes=60`으로 변경된다.
- 관측 포인트: `ShopSettingsTest`에서 interval 변경

#### ~~1-2. 잘못된 수치 입력은 거부한다~~ [x]

- 목적: 30 / 60을 제외한 입력값은 예외 발생시킨다
- 입력: `intervalMinutes`가 `30`, `60`이 아닌 값 (`0`, 음수, `15` 등)
- 출력: AvailabilityException 발생
- 관측 포인트: 예외 타입/메시지 검증한다.

---

### [x] 2. Persistence - ShopSettings 기본 생성 멱등성

#### ~~2-2. settings가 이미 있으면 중복 생성하지 않는다~~ [x]

- 목적: unique(`shop_id`) 충돌 없이 재시도/중복 호출을 안전하게 처리한다.
- 입력: `shopId`에 기존 row 1건 존재
- 출력: 추가 insert 없이 기존 row 유지
- 엣지케이스: 기존 row가 기본값이 아니어도 덮어쓰지 않는다.
- 관측 포인트: row count=1, 기존 값 불변, 예외 미발생을 함께 확인한다.

---

### [x] 3. Service - Shop 생성 시 기본 Settings 보장

#### ~~3-1. createShop가 기본 staff/form과 함께 shop_settings를 만든다~~ [x]

- 목적: 온보딩 트랜잭션의 초기화 책임을 완성한다.
- 입력: 정상 owner + `CreateShopRequest`
- 출력: 생성된 `shopId`에 staff 1건, form 생성 호출, settings 1건 존재
- 엣지케이스: staff가 이미 있는 경우 기존 보호 로직과 충돌하지 않는다.
- 관측 포인트: `ShopDefaultStaffCreationTest`에서 `ShopSettingsRepository` 조회로 확인한다.

---

### [x] 4. API Integration - Schedule Interval 계약

#### ~~4-1. PUT /schedule/interval이 intervalMinutes를 저장하고 응답한다~~ [x]

- 목적: `PUT` 리소스 의미(단일 interval 리소스 전체 교체)를 API에 반영한다.
- 입력: `{"intervalMinutes": 60}`
- 출력: 200 OK + 응답 JSON의 `intervalMinutes=60`
- 엣지케이스: 동일 값 재요청 시에도 멱등하게 200 응답
- 관측 포인트: `ShopScheduleControllerIntegrationTest`에서 JSON path + repository 재조회로 검증한다.

---

### [x] 5. API Integration - 온보딩 직후 Schedule API 성공

#### ~~5-1. POST /shop 직후 PUT /schedule/interval이 성공한다~~ [x]

- 목적: `SHOP_SETTINGS_NOT_FOUND` 장애의 재발을 API 레벨에서 차단한다.
- 입력: 점주가 `POST /shop`으로 매장 생성 후 즉시 interval 업데이트 호출
- 출력: 200 OK, `SHOP_SETTINGS_NOT_FOUND` 미발생
- 엣지케이스: 생성 직후 첫 호출(사전 수동 seed 없음)
- 관측 포인트: 통합 테스트에서 shop 생성을 실제 경로로 수행하고 상태코드/에러코드 확인

#### ~~5-2. POST /shop 직후 PUT /schedule/operating-times가 성공한다~~ [x]

- 목적: 운영시간 설정 경로에서도 동일 장애가 재발하지 않음을 보장한다.
- 입력: 점주가 `POST /shop` 후 즉시 operating-times 업데이트 호출
- 출력: 200 OK, 운영시간 row 저장
- 엣지케이스: `DAILY`/`BY_DAY` 중 최소 1개 타입으로 재현
- 관측 포인트: 상태코드 + 저장 row 개수 + `SHOP_SETTINGS_NOT_FOUND` 미발생 확인

---

## Notes

### long-running 테스트 제외 기준

- 1차 RED/GREEN 사이클에서는 관련 테스트 클래스만 실행한다.
    - 예: `ShopSettingsTest`, `ShopSettingsWriterTest`, `ShopDefaultStaffCreationTest`,
      `ShopScheduleControllerIntegrationTest`
- 전체 `./gradlew test`는 섹션 5 Green 이후 회귀 확인 단계에서만 실행한다.

### 모호한 요구사항/질문 목록

- `PUT /schedule/interval`의 금지 필드 에러 응답 포맷(code/message)을 기존 에러 체계와 어떤 코드로 맞출지 확정 필요
- 수치 검증 실패 시 예외 타입을 `IllegalArgumentException`으로 둘지, 비즈니스 예외로 통일할지 확인 필요 -> 비즈니스 예외로 통일할 것.
- 후속 API 설계 확정 필요: `PATCH /schedule/booking-policy`, `GET /schedule/booking-policy`
- 문서 정합성 수정 범위(`#101`의 어떤 파일까지 동기화할지)는 구현 Green 이후 체크리스트로 확정 필요
- 추가 논의 링크:
    - `docs/issues/#103/02-analysis/0001-root-cause-and-options.md`
    - `docs/issues/#103/02-design/0001-design-plan.md`
