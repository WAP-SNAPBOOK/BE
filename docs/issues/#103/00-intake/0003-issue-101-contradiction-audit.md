# #101 문서-구현 모순 감사 (shop_settings 중심)

## 목적
- 사용자 제보: 점주가 `POST /shop` 직후
  - `PUT /api/v1/shops/{shopId}/schedule/settings`
  - `PUT /api/v1/shops/{shopId}/schedule/operating-times`
  호출 시 `shop_settings`가 없어 실패.
- `docs/issues/#101` 산출물(user flow/API spec/analysis)과 실제 코드 간 모순을 식별.

## 핵심 결론
1. **재현 가능 결함**: 매장 생성 시 `shop_settings` 기본 row가 생성되지 않는다.  
2. **문서 모순**: `#101` 문서에는 schedule API를 온보딩 플로우에서 즉시 사용 가능한 것으로 서술하지만, 실제 구현은 `ShopSettings` 선행 존재를 강제한다.  
3. **추가 모순**: `#101` 일부 문서는 공개 API라고 쓰지만, 현재 보안 설정상 대부분 인증 필수다.

## 근거 1) shop 생성 후 shop_settings 미생성
### 코드 근거
- `ShopService.createShop()`는 기본 Staff/기본 Form만 생성:
  - `src/main/java/com/example/easybooking/shop/service/ShopService.java:45`
  - `src/main/java/com/example/easybooking/shop/service/ShopService.java:53`
- `ShopSettings.createDefault()` 팩토리는 존재하지만 생성 호출 경로가 없음:
  - `src/main/java/com/example/easybooking/availability/domain/ShopSettings.java:39`

### schedule API가 settings 존재를 강제하는 근거
- settings 조회/수정/운영시간 수정 모두 `readByShopId()` 사용:
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:40`
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:50`
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:123`
- 미존재 시 예외:
  - `src/main/java/com/example/easybooking/availability/ShopSettingsReader.java:17`
  - `src/main/java/com/example/easybooking/availability/exception/ShopSettingsNotFoundException.java:8`
  - `src/main/java/com/example/easybooking/errors/errorcode/AvailabilityErrorCode.java:11`

### 테스트 근거 (선행 생성을 테스트에서 수동 보완)
- `ShopScheduleControllerIntegrationTest`에서 매번 수동 생성:
  - `src/test/java/com/example/easybooking/availability/presentation/ShopScheduleControllerIntegrationTest.java:77`
  - `src/test/java/com/example/easybooking/availability/presentation/ShopScheduleControllerIntegrationTest.java:103`
  - `src/test/java/com/example/easybooking/availability/presentation/ShopScheduleControllerIntegrationTest.java:134`
- `AvailabilityControllerIntegrationTest` fixture도 수동 생성:
  - `src/test/java/com/example/easybooking/availability/presentation/AvailabilityControllerIntegrationTest.java:331`

## 근거 2) #101 문서와 실제의 모순
### 모순 A: 온보딩 플로우상 즉시 schedule 설정 가능 전제 vs 실제 선행조건 누락
- 문서상 흐름
  - 매장 생성 다음 단계로 운영시간 설정 제시:
    - `docs/issues/#101/01-user-flow.md:18`
    - `docs/issues/#101/01-user-flow.md:22`
  - API 매핑에서 해당 API를 “존재”로 표기:
    - `docs/issues/#101/02-api-mapping.md:29`
    - `docs/issues/#101/02-api-mapping.md:30`
- 실제
  - `shop_settings` 없으면 schedule API 실패(위 코드 근거 참조).

### 모순 B: settings API가 여러 필드 업데이트한다고 문서화 vs 구현은 interval만 반영
- 문서
  - request에 `bookingWindowDays`, `minBookingLeadMinutes`, `publicHolidayOff` 포함:
    - `docs/issues/#101/04-api-spec-by-flow.md:190`
    - `docs/issues/#101/04-api-spec-by-flow.md:203`
  - 공휴일 토글도 같은 API로 가능하다고 서술:
    - `docs/issues/#101/04-api-spec-by-flow.md:301`
    - `docs/issues/#101/04-api-spec-by-flow.md:316`
- 구현
  - 실제 업데이트는 `intervalMinutes`만:
    - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:51`

### 모순 C: 문서의 “인증 없음(공개)” 표기 vs 현재 보안 설정
- 문서(예시)
  - 월 가용 조회 인증 없음:
    - `docs/issues/#101/04-api-spec-by-flow.md:739`
  - 일 가용 조회 인증 없음:
    - `docs/issues/#101/04-api-spec-by-flow.md:799`
  - 태그/메뉴 조회 인증 없음:
    - `docs/issues/#101/04-api-spec-by-flow.md:451`
    - `docs/issues/#101/04-api-spec-by-flow.md:545`
- 실제 보안
  - `allowUrls` 외 전부 인증 필요:
    - `src/main/java/com/example/easybooking/auth/SecurityConfig.java:54`
    - `src/main/java/com/example/easybooking/auth/SecurityConfig.java:55`
  - `/api/v1/shops/...`, `/api/shops/...`, `/api/tags`는 allowUrls에 없음.

## #101 문서 내부 상충(문서끼리 모순)
- `04-api-spec-by-flow.md`는 “실제 구현된 기능만 포함”이라고 적었지만:
  - `docs/issues/#101/04-api-spec-by-flow.md:5`
  - 같은 문서에서 공개 API/토글 반영을 서술(현재 구현과 불일치).
- 반면 `0001-current-app-feature-flow-analysis.md`는 `publicHolidayOff` 미반영을 이미 부분 커버로 명시:
  - `docs/issues/#101/08-analysis/0001-current-app-feature-flow-analysis.md:92`

## 영향
- 점주 온보딩 직후 schedule 설정 API 실패(404 `SHOP_SETTINGS_NOT_FOUND`) 가능.
- 문서 신뢰도 하락: 프론트/QA가 문서 기준으로 붙이면 예상과 다른 인증/동작에 부딪힘.

## 정리
- 사용자 제보 내용은 **코드 기준 사실**이다.
- #101 산출물 중 일부 문서는 현재 구현보다 앞선 가정(또는 누락된 선행조건)을 포함한다.
