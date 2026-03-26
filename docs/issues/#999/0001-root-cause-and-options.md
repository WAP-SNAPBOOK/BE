# `#107` ShopMenu 가격 모델 부재 원인 분석 및 대안

작성일: `2026-03-07`

## 문제 재정의

`ReservationMenuItem.priceSnapshot` 필드는 존재하지만 원본인 `ShopMenu.price`가 없어 예약 생성 시 가격 스냅샷이 항상 `null`로 저장된다.

## 원인 분석

### 가설

1. 가격 스냅샷 필드는 미래 확장을 염두에 두고 먼저 추가되었지만, 원본 메뉴 가격 모델은 도입되지 않았다.
2. `shop_services -> shop_menus` 전환 과정에서 가격 컬럼이 누락된 것이 아니라, 초기 스키마부터 가격 개념 자체가 비어 있었다.
3. 예약 상세 응답은 가격을 내려줄 수 있는 형태로 열려 있으나 저장 로직이 이를 채우지 못한다.

### 검증

- 스키마 근거
  - `src/main/resources/db/migration/V1__reservation_v2_phase1_add_tables_and_columns.sql`
  - `shop_services` 생성 시 `name`, `description`, `is_active`, `sort_order`만 존재하고 `price`가 없다.
- 리네임 근거
  - `src/main/resources/db/migration/V4__rename_shop_services_to_shop_menus.sql`
  - `shop_services`를 `shop_menus`로 바꾸는 과정에서도 `price` 추가는 없다.
- 도메인 근거
  - `src/main/java/com/example/easybooking/shop/domain/ShopMenu.java`
  - `ShopMenu`에 가격 필드와 관련 생성/수정 로직이 없다.
- 저장 로직 근거
  - `src/main/java/com/example/easybooking/reservation/service/ReservationMenuItemService.java`
  - `ReservationMenuItem.create(..., null, ...)`로 호출해 `priceSnapshot`을 의도적으로 비운다.
- 응답 근거
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationMenuItemResponse.java`
  - 예약 상세 응답에는 `priceSnapshot` 필드가 이미 포함되어 있다.
- 문서 근거
  - `api/02-api-mapping.md`
  - `ShopMenu.price` 추가 필요 항목이 이미 `P1`로 정리되어 있다.

### 결론

원인은 단순 DTO 누락이 아니라, 메뉴 가격의 원본 모델이 도메인/DB/API에 모두 빠져 있는 상태에서 예약 스냅샷 필드만 부분적으로 선도입된 구조 불일치다.

## 대안

### 대안 A. `ShopMenu.price` 추가 후 예약 시 `priceSnapshot` 복사

- 장점
  - 가격의 원본과 스냅샷의 책임이 분리된다.
  - 메뉴 조회 API와 예약 상세 API가 같은 가격 의미를 공유한다.
  - 기존 `priceSnapshot` 필드를 그대로 살릴 수 있다.
- 단점
  - DB migration, 엔티티, DTO, 서비스, 테스트를 모두 수정해야 한다.
- 비용
  - 중간
- 리스크
  - 기존 데이터는 `price=null` 상태로 남으므로 nullable 정책을 명확히 해야 한다.
- 운영 난이도
  - 낮음

### 대안 B. `ShopMenu`는 그대로 두고 예약 생성 요청에서 가격을 직접 받아 `priceSnapshot`에만 저장

- 장점
  - 변경 범위가 예약 생성 쪽으로 좁다.
  - `shop_menus` 스키마를 건드리지 않을 수 있다.
- 단점
  - 가격의 원본이 서버 도메인에 존재하지 않는다.
  - 클라이언트 입력을 그대로 가격 근거로 삼아 정합성이 약해진다.
  - 메뉴 조회 응답에는 여전히 가격이 없다.
- 비용
  - 중간
- 리스크
  - 클라이언트 변조/오류 입력으로 잘못된 가격 스냅샷이 저장될 수 있다.
- 운영 난이도
  - 중간

### 대안 C. `priceSnapshot` 제거 또는 계속 미사용

- 장점
  - 구현 비용이 가장 작다.
- 단점
  - 예약 상세 응답의 가격 슬롯이 의미 없는 필드로 남는다.
  - 향후 가격 이력 보존 요구를 충족하지 못한다.
  - 기존 문서와도 더 멀어진다.
- 비용
  - 낮음
- 리스크
  - 데이터 모델의 어색함이 계속 누적된다.
- 운영 난이도
  - 낮음

## 추천안

대안 A를 추천한다.

이유:

- 현재 구조는 이미 예약 스냅샷 개념을 채택했다.
- 스냅샷이 유효하려면 원본 가격이 서버 도메인에 존재해야 한다.
- 사용자 선택에 따라 가격은 `nullable`, 타입은 `Long` 원단위로 두면 기존 `priceSnapshot(Long)`와도 자연스럽게 맞는다.

## 위험 / 롤백 / 관측 계획

### 위험

- 기존 메뉴 데이터에는 가격이 없으므로 신규 응답에 `price=null`이 섞일 수 있다.
- 메뉴 생성/수정 API 계약이 바뀌므로 프론트와 DTO 정합성이 필요하다.

### 롤백

- 코드 롤백 시 `ShopMenu.price` 사용 로직만 제거하면 예약 저장은 다시 `null` 스냅샷으로 돌아갈 수 있다.
- DB 컬럼은 additive 변경이므로 즉시 삭제하지 않고 남겨두는 방식으로 롤백 부담을 줄인다.

### 관측

- 메뉴 생성/수정 테스트에서 `price` 반영 여부를 검증한다.
- 예약 생성 테스트에서 `priceSnapshot` 저장 여부를 검증한다.
- 예약 상세 테스트에서 기존 응답 필드가 유지되는지 회귀 확인한다.
