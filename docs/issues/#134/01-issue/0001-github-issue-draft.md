# `[REFACTOR]: 메뉴 태그 전환 모델과 soft delete 제약을 정리`

Issue: `#134`  
Created: `2026-07-03`  
Repository: `WAP-SNAPBOOK/BE`  
Issue URL: `https://github.com/WAP-SNAPBOOK/BE/issues/134`  
Branch: `refactor/jiseob/#134`

---

## Background

메뉴/태그 도메인은 전역 `tags`에서 매장 로컬 `shop_tags`로 전환 중이다. 현재 `shop_menu_tags`에는 `tag_id`와 `shop_tag_id`가 공존하고, 서비스 코드에는 legacy fallback이 남아 있다. 또한 `shop_menus.deleted_at`이 존재하지만 `UNIQUE(shop_id, name)`은 soft delete 정책과 충돌할 수 있다.

관련 코드:

- `src/main/java/com/example/easybooking/shop/domain/ShopMenu.java`
- `src/main/java/com/example/easybooking/shop/domain/ShopMenuTag.java`
- `src/main/java/com/example/easybooking/shop/domain/ShopTag.java`
- `src/main/java/com/example/easybooking/shop/service/TagService.java`
- `src/main/java/com/example/easybooking/reservation/service/ReservationMenuItemService.java`

## Problem

`tag_id`와 `shop_tag_id` 공존은 조회/삭제/예약 스냅샷 로직을 복잡하게 만든다. 전환 완료 시점이 명확하지 않으면 legacy fallback이 계속 남아 데이터 모델의 의미가 흐려진다. soft delete 컬럼이 있는 메뉴는 같은 이름 재생성 정책도 현재 유니크 제약과 맞지 않을 수 있다.

## Goal

태그 모델을 매장 로컬 태그 중심으로 수렴시키고, 메뉴/입력필드의 유니크 제약과 soft delete 정책을 명확히 한다.

## Scope

- `shop_menu_tags.tag_id` 제거 가능 여부와 전환 완료 조건을 확인한다.
- 전환 완료 시 `shop_menu_tags.shop_tag_id NOT NULL`, FK, `UNIQUE(shop_menu_id, shop_tag_id)` 정리를 검토한다.
- `TagService`의 legacy global tag API/fallback 제거 시점을 정한다.
- `ReservationMenuItemService`의 tag name snapshot 조회 경로를 `shop_tag_id` 기준으로 단순화할 수 있는지 검토한다.
- `shop_menus.deleted_at`을 실제 soft delete로 사용할지, 단순 비활성화만 사용할지 결정한다.
- soft delete를 유지한다면 `UNIQUE(shop_id, name)` 정책을 재검토한다.
- `shop_menu_input_fields`의 `UNIQUE(shop_menu_id, label)`과 비활성 필드 재생성 정책을 검토한다.

## Out Of Scope

- 메뉴 가격 정책 변경
- 예약 메뉴 스냅샷 구조 전면 변경
- 프론트 태그 관리 UX 재설계

## Acceptance Criteria

- [ ] 태그 모델의 최종 기준이 `shop_tags`인지 명확히 결정된다.
- [ ] legacy `tag_id` fallback 제거 또는 유지 조건이 문서화된다.
- [ ] `shop_menu_tags`의 유니크/FK/NOT NULL 제약이 최종 모델과 일치한다.
- [ ] 메뉴 soft delete와 이름 유니크 정책이 충돌하지 않는다.
- [ ] 입력 필드 label 유니크 정책이 비활성/재생성 시나리오와 맞는지 검증된다.

## Risks

- 기존 `tag_id` 기반 데이터가 남아 있으면 컬럼 제거 또는 NOT NULL 전환이 실패한다.
- 프론트가 legacy tag API를 아직 호출한다면 API 제거 시 장애가 발생한다.
- MySQL에서 soft delete 조건부 유니크를 구현하려면 별도 컬럼/인덱스 전략이 필요할 수 있다.

## References

- `src/main/java/com/example/easybooking/shop/domain/ShopMenu.java`
- `src/main/java/com/example/easybooking/shop/domain/ShopMenuTag.java`
- `src/main/java/com/example/easybooking/shop/service/TagService.java`
- `src/main/java/com/example/easybooking/reservation/service/ReservationMenuItemService.java`
- `docs/refactor/2026-04-08-current-data-model-design-analysis.md`
