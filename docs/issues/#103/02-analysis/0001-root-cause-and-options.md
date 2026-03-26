# #103 원인 분석 + 대안 비교 (A/B/C)

Created: 2026-03-01  
Issue: `#103`

---

## 문제 재정의(한 문장)

매장 생성 직후 Schedule API가 `SHOP_SETTINGS_NOT_FOUND`로 실패하는 이유는, 온보딩 트랜잭션에서 `shop_settings` 초기화가 누락된 상태로 API/문서 계약이 분리되어 있기 때문이다.

---

## 원인 분석

### 가설

1. **초기화 누락 가설**  
   `ShopService.createShop()`가 기본 Staff/Form만 생성하고 `ShopSettings.createDefault()` 저장을 하지 않는다.

2. **숨은 선행조건 가설**  
   Schedule 서비스는 `shop_settings` 존재를 강제(`readByShopId`)하므로, 생성 누락 시 즉시 실패한다.

3. **계약 드리프트 가설**  
   Settings 업데이트 DTO는 여러 필드를 받지만, 서비스는 `intervalMinutes`만 반영해 문서/구현 계약이 어긋난다.

4. **문서-보안 불일치 가설**  
   일부 문서는 공개 API처럼 서술하지만 실제 보안은 `allowUrls` 외 전부 인증 필수다.

### 검증(증거/재현/로그/지표)

- 증거 A: 매장 생성 시 기본 Staff/Form 생성은 있으나 settings 생성은 없음
  - `src/main/java/com/example/easybooking/shop/service/ShopService.java:41`
  - `src/main/java/com/example/easybooking/shop/service/ShopService.java:45`
  - `src/main/java/com/example/easybooking/shop/service/ShopService.java:53`

- 증거 B: settings 존재 강제 경로
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:40`
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:50`
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:123`
  - `src/main/java/com/example/easybooking/availability/ShopSettingsReader.java:15`

- 증거 C: DTO-서비스 반영 불일치
  - DTO 필드: `bookingWindowDays`, `minBookingLeadMinutes`, `publicHolidayOff` 존재
  - `src/main/java/com/example/easybooking/availability/dto/request/UpdateShopScheduleSettingsRequest.java:10`
  - 서비스 반영: `updateInterval()`만 호출
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:51`

- 증거 D: 테스트도 settings를 수동 생성해서 전제 보완
  - `src/test/java/com/example/easybooking/availability/presentation/ShopScheduleControllerIntegrationTest.java:77`
  - `src/test/java/com/example/easybooking/availability/presentation/AvailabilityControllerIntegrationTest.java:331`

- 증거 E: 보안 정책은 allowUrls 외 인증 필수
  - `src/main/java/com/example/easybooking/auth/SecurityConfig.java:23`
  - `src/main/java/com/example/easybooking/auth/SecurityConfig.java:55`

- 재현 시나리오(개념)
  1. 점주가 `POST /shop` 성공
  2. 즉시 `PUT /api/v1/shops/{shopId}/schedule/settings` 호출
  3. `shop_settings` 미존재 시 `ShopSettingsReader.readByShopId()`에서 예외

- 로그/지표(현재 문서 기준)
  - 오류코드 `SHOP_SETTINGS_NOT_FOUND` 건수/엔드포인트 분포를 운영에서 별도 집계하지 않으면 원인 탐지가 느리다.

### 결론(원인)

근본 원인은 단일 버그가 아니라 아래 3가지의 결합이다.

1. **도메인 초기화 책임 누락**: shop 온보딩에서 `shop_settings`가 빠져 있음  
2. **강한 선행조건 + 완충 부재**: schedule API가 missing row를 복구하지 않고 즉시 실패  
3. **문서/구현 계약 드리프트**: settings 필드 반영 범위 및 인증 설명이 실제와 분리됨

---

## 대안

### 대안 A: 최소 변경(온보딩 초기화 추가 + 문서 축소 정합화)

- 내용
  - `POST /shop` 트랜잭션 내 `shop_settings` 기본 생성만 추가
  - settings API는 **interval-only 반영**으로 문서를 축소
  - 인증 표기는 현재 `SecurityConfig` 기준으로 정정

- 장점
  - 결함(`SHOP_SETTINGS_NOT_FOUND`)을 가장 빠르게 차단
  - 코드 변경 범위/회귀 범위 최소

- 단점
  - `bookingWindowDays/minBookingLead/publicHolidayOff`는 계속 “요청은 받지만 미반영” 상태
  - 중장기적으로 API 계약 품질이 낮게 유지됨

- 비용
  - 낮음 (소규모 코드 + 문서 수정 + 테스트 보강)

- 리스크
  - 제품팀/프론트에서 기대한 settings 전체 반영 니즈가 누적될 수 있음

- 운영 난이도
  - 낮음

### 대안 B: 문서 계약에 맞춰 기능 확장(온보딩 초기화 + settings 전체 필드 반영)

- 내용
  - 대안 A + `updateSettings()`에서 4개 필드 전체 반영
  - `ShopSettings` 도메인에 필요한 업데이트 메서드 추가
  - #101 문서군을 “구현 기준”으로 동기화

- 장점
  - API 계약 일관성 회복
  - 추가 필드 관련 프론트/QA 재해석 비용 감소

- 단점
  - 유효성 검증(음수/범위/널 정책)과 회귀 테스트 범위 증가
  - 기존 클라이언트가 interval만 의존하던 경로 검증 필요

- 비용
  - 중간 (도메인/서비스/테스트/문서 동시 정리)

- 리스크
  - 검증 규칙 정의가 약하면 새 필드에서 데이터 품질 문제 발생

- 운영 난이도
  - 중간

### 대안 C: 단계적 수렴(온보딩/백필 우선 + 설정 확장은 2단계)

- 내용
  - 1단계: 온보딩 기본 settings 생성 + 기존 누락 shop 백필 + 오류 지표 계측
  - 2단계: settings 전체 필드 반영 확장(또는 명시적 비지원 결정) + 문서 최종 동기화

- 장점
  - 사용자 장애를 먼저 제거하면서, 계약 확장을 안정적으로 분리 가능
  - 운영 데이터 누락(backfill)까지 커버

- 단점
  - 단계 관리/커뮤니케이션 비용 증가
  - 완료 전까지는 과도기 규칙이 존재

- 비용
  - 중간~높음 (운영 백필 + 단계별 테스트/릴리스)

- 리스크
  - 단계 1 이후 단계 2가 지연되면 드리프트가 다시 누적될 수 있음

- 운영 난이도
  - 중간

---

## 추천안

**추천: 대안 C(단계적 수렴)**

### 왜 이게 최선인지(트레이드오프 포함)

- 즉시 장애(`SHOP_SETTINGS_NOT_FOUND`)는 1단계에서 빠르게 제거해야 한다.
- 동시에 현 상태를 대안 A처럼 고정하면 “요청-반영 불일치”가 기술부채로 남는다.
- 대안 B를 한 번에 수행하면 품질은 좋지만, 현재 이슈의 긴급성(온보딩 실패) 대비 변경폭이 커진다.
- 따라서 **1단계 안정화 + 2단계 계약 수렴**이 리스크/속도 균형이 가장 좋다.

추천 실행 순서:
1. 온보딩 경로에 `shop_settings` 기본 생성 추가(멱등 방어 포함)
2. 운영 DB 누락 매장 백필 여부 판단 및 실행
3. settings 필드 정책(전체 반영 vs 일부 반영) 최종 결정
4. 결정에 맞춰 코드/문서/테스트 동시 수렴

---

## 위험/롤백/관측(Observability) 계획

### 위험

- 중복 생성 경쟁 시 `shop_settings.shop_id` unique 충돌 가능
- 백필 수행 시 기존 수동 데이터와 기본값 정책 충돌 가능
- 문서만 먼저 바뀌거나 코드만 먼저 바뀌는 드리프트 재발 가능

### 롤백

- 1단계(온보딩 생성) 문제 시: 생성 훅을 되돌리고 기존 동작으로 즉시 복귀 가능
- 2단계(필드 확장) 문제 시: interval-only 반영으로 즉시 회귀하고 확장 필드 무시 정책으로 임시 복구
- 백필 문제 시: 백필 대상/결과를 별도 로그로 남겨 부분 롤백 가능하게 설계

### 관측(Observability)

- 에러 관측
  - `SHOP_SETTINGS_NOT_FOUND` 발생 건수/엔드포인트별 건수
- 온보딩 관측
  - `POST /shop` 성공 대비 `shop_settings` 생성 성공률
  - 생성 실패 사유(unique 충돌/트랜잭션 롤백) 집계
- 계약 관측
  - `PUT /schedule/settings` 요청 필드 대비 실제 반영 필드 로그 샘플링
- 품질 게이트
  - "shop 생성 직후 schedule/settings + operating-times 호출" 통합테스트를 회귀 필수 케이스로 고정

---

## 참고

- `docs/issues/#103/01-issue/0001-github-issue-draft.md`
- `docs/issues/#103/00-intake/0003-issue-101-contradiction-audit.md`
- `docs/issues/#103/00-intake/0002-api-catalog-and-design-review.md`
- `docs/issues/#103/00-intake/0001-problem-scan.md`
