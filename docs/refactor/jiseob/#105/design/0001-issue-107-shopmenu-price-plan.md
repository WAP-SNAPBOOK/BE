# `#107` ShopMenu 가격 추가 설계 요약

작성일: `2026-03-07`
관련 이슈: `#107`
현재 브랜치: `refactor/jiseob/#105`

## 1. 어떻게 변경할지

- `shop_menus`에 nullable `price BIGINT` 컬럼을 추가한다.
- `ShopMenu` 엔티티와 생성/수정 메서드에 `price`를 추가한다.
- `CreateShopMenuRequest`, `UpdateShopMenuRequest`, `ShopMenuResponse`에 `price`를 추가한다.
- 예약 생성 시 `ShopMenu.price`를 읽어 `ReservationMenuItem.priceSnapshot`에 저장한다.
- 테스트는 엔티티 매핑 -> 메뉴 서비스 -> 예약 스냅샷 순으로 TDD 진행한다.

## 2. 변경 근거

- 장점
  - 가격의 원본과 예약 시점 스냅샷이 명확히 분리된다.
  - 현재 `ReservationMenuItem.priceSnapshot` 필드를 의미 있게 살릴 수 있다.
  - `Long` 원단위 유지로 기존 DTO/테스트와 자연스럽게 연결된다.
- 단점
  - DB migration과 API 계약 변경이 함께 필요하다.
  - 기존 데이터는 `price=null` 상태로 남는다.
- 대안 비교
  - 예약 요청에서 가격을 직접 받는 방식보다 서버 정합성이 높다.
  - `priceSnapshot` 제거보다 현재 모델과 문서 방향에 더 잘 맞는다.

## 3. 코드 수정 예시

```diff
+ @Column(name = "price")
+ private Long price;

- public static ShopMenu create(Long shopId, String name, String description,
-                               Boolean isActive, Integer sortOrder)
+ public static ShopMenu create(Long shopId, String name, String description,
+                               Long price, Boolean isActive, Integer sortOrder)

- return ReservationMenuItem.create(reservationId, menuId, menu.getName(), null, sortOrder);
+ return ReservationMenuItem.create(
+     reservationId,
+     menuId,
+     menu.getName(),
+     menu.getPrice(),
+     sortOrder
+ );
```

## 4. 변경 후 예상 결과

- 메뉴 생성/수정/조회 응답에서 가격을 함께 다룰 수 있다.
- 신규 예약부터는 선택된 메뉴의 당시 가격이 `priceSnapshot`에 저장된다.
- 기존 예약 응답 구조는 유지되고, 신규 데이터만 더 정확해진다.
- 기존 가격 없는 메뉴는 계속 허용되며 `null`로 동작한다.

## 5. 문서 위치

- 분석: `docs/issues/#107/02-analysis/0001-root-cause-and-options.md`
- TDD SSOT: `docs/issues/#107/04-tdd/plan.md`
