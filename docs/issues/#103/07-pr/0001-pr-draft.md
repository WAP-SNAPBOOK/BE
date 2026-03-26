# PR Draft - #103 ShopSettings 온보딩 초기화 및 Schedule Interval 분리

## Summary

- 문제: 매장 생성 직후 schedule API 호출 시 `SHOP_SETTINGS_NOT_FOUND`가 발생할 수 있었다.
- 목표: 온보딩 트랜잭션에서 `shop_settings` 기본 row를 보장하고, interval 변경 API 계약을 명확히 한다.
- 대안 검토:
    - A안: 기존 `PUT /schedule/settings`를 유지하고 interval만 수정
    - B안(선택): `PUT /schedule/interval`로 리소스를 분리하고 기존 update API는 제거
- 선택 이유: API 의미를 명확히 하면서도 이번 이슈 범위를 최소화할 수 있기 때문이다.

## Changes

- 온보딩 경로 보강: `ShopService.createShop()`에서 `shop_settings` 기본 생성 보장(`ensureDefaultByShopId`).
- interval API 분리: `PUT /api/v1/shops/{shopId}/schedule/interval` 추가 + 기존 `PUT /schedule/settings` 제거.
- 도메인 검증 강화: `intervalMinutes`는 `30/60`만 허용하고 위반 시 `AvailabilityException(INVALID_INTERVAL_MINUTES)` 반환.
