# 매장 생성 직후 Schedule API 실패(`SHOP_SETTINGS_NOT_FOUND`) 및 #101 문서 정합성 수정

Issue: `#999`  
Created: 2026-02-27

---

## 배경/문제 정의

현재 점주가 `POST /shop`으로 매장을 생성한 직후 아래 API를 호출하면 `shop_settings` row 부재로 실패할 수 있다.

- `PUT /api/v1/shops/{shopId}/schedule/settings`
- `PUT /api/v1/shops/{shopId}/schedule/operating-times`

원인:
- 매장 생성 시 기본 Staff/Form은 생성하지만 `ShopSettings` 기본값 생성은 누락됨.
- Schedule 계열 서비스는 `shop_settings` 선행 존재를 강제(`readByShopId`)함.

동시에 `docs/issues/#101` 문서 일부는 실제 구현과 다른 전제(즉시 호출 가능, 인증 없음, settings 전체 필드 반영)를 포함해 프론트/QA와의 계약 혼선을 유발한다.

## 목표(Goals)

- 매장 생성 직후 Schedule API가 실패하지 않도록 `shop_settings` 초기 상태를 보장한다.
- Schedule Settings API의 문서-구현 불일치를 정리한다.
- `#101` 문서 중 구현 기준과 어긋난 인증/동작 설명을 정합화한다.

## 비목표(Non-goals)

- Staff CRUD, StaffHoliday 등 신규 도메인 확장
- Availability 응답 스펙 확장(점유 슬롯 상태 분리, 공휴일 분리 필드 추가)
- 예약/메뉴 도메인 구조 변경

## 요구사항

### 기능

- `POST /shop` 성공 시 `ShopSettings.createDefault(shopId)`를 함께 저장한다.
- 이미 `shop_settings`가 존재하는 경우(재시도/데이터 이슈) 중복 생성 없이 안전하게 처리한다.
- `PUT /api/v1/shops/{shopId}/schedule/settings`의 실제 반영 필드를 명확히 결정한다.
  - 선택지 A: 문서대로 `interval + bookingWindowDays + minBookingLeadMinutes + publicHolidayOff` 모두 반영
  - 선택지 B: 구현대로 interval만 반영하고 문서를 축소
- #101 문서에서 인증 요구사항/동작 설명을 현재 보안 설정과 일치시킨다.

### 비기능(성능/보안/운영)

- 기존 매장 생성 트랜잭션 내에서 원자적으로 처리되어 부분 실패가 없어야 한다.
- 멱등성: 중복 요청/재처리 시 `shop_settings.shop_id` unique 제약 위반 없이 안정 동작해야 한다.
- 운영 관측성: 실패 시 오류 코드/로그로 원인 파악 가능해야 한다.

## 수용 기준(AC)

- AC-1: `POST /shop` 이후 동일 트랜잭션 내 `shop_settings`가 기본값으로 생성된다.
- AC-2: 매장 생성 직후 `PUT /api/v1/shops/{shopId}/schedule/settings` 호출 시 `SHOP_SETTINGS_NOT_FOUND`가 발생하지 않는다.
- AC-3: 매장 생성 직후 `PUT /api/v1/shops/{shopId}/schedule/operating-times` 호출 시 `SHOP_SETTINGS_NOT_FOUND`가 발생하지 않는다.
- AC-4: `#101` 문서의 인증 표기와 실제 `SecurityConfig` 정책이 일치한다.
- AC-5: `#101` 문서의 settings 업데이트 필드 설명과 실제 구현이 일치한다.
- AC-6: 관련 테스트(통합/단위)에서 shop_settings 선행 수동 생성 없이도 온보딩 시나리오가 통과한다.

## 범위/의존성

- 범위:
  - Shop 생성 경로(`ShopService.createShop`)의 초기화 로직
  - Schedule Settings 갱신 정책(코드 또는 문서)
  - `docs/issues/#101` 관련 문서 정합성 수정
- 의존:
  - `shop_settings` 스키마(V5)와 unique(`shop_id`) 제약
  - 기존 `ShopScheduleService`/`ShopSettingsReader` 동작

## 리스크/운영 메모

- 이미 운영 DB에 `shop_settings` 없는 매장이 존재할 수 있어 백필(backfill) 필요 여부 검토가 필요.
- settings 업데이트 정책(A/B) 결정이 늦어지면 문서와 구현 드리프트가 재발할 수 있다.
- 테스트 픽스처가 기존처럼 수동 생성에 의존할 경우 회귀를 놓칠 수 있으므로 “실제 온보딩 경로” 기반 통합 테스트를 추가해야 한다.

## 작업 체크리스트

- [ ] `POST /shop` 경로에 `shop_settings` 기본 생성 로직 추가
- [ ] 중복 생성 방어(멱등) 처리 정책 적용
- [ ] settings 업데이트 정책 결정(A/B) 및 코드/문서 반영
- [ ] `ShopScheduleControllerIntegrationTest`에 “shop 생성 직후 settings/operating-times 호출” 케이스 추가
- [ ] `AvailabilityControllerIntegrationTest` 픽스처 의존성 재점검(수동 settings 생성 최소화)
- [ ] `docs/issues/#101/01-user-flow.md` 정합성 수정
- [ ] `docs/issues/#101/04-api-spec-by-flow.md` 인증/필드 반영 설명 수정
- [ ] `docs/issues/#101/03-api-spec.md`, `08-analysis` 문서와 교차 검수

## 관련 문서

- 근거 감사: `docs/issues/#999/00-intake/0003-issue-101-contradiction-audit.md`
- API 카탈로그/설계 점검: `docs/issues/#999/00-intake/0002-api-catalog-and-design-review.md`
- #101 User Flow: `docs/issues/#101/01-user-flow.md`
- #101 Flow별 API 명세: `docs/issues/#101/04-api-spec-by-flow.md`
