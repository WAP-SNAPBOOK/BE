# `#107` ShopMenu 가격 추가 TDD 계획

작성일: `2026-03-07`

## Ground Rules

- Red -> Green -> Refactor 순서를 지킨다.
- 구조 변경과 동작 변경을 한 단계에 섞지 않는다.
- 각 단계는 가능한 한 가장 작은 실패 테스트 1개로 시작한다.
- 테스트 식별자는 영문으로 작성하고 `@DisplayName`은 한국어로 작성한다.

## Scope

### Goals

- `ShopMenu`가 가격을 저장/조회할 수 있다.
- 메뉴 생성/수정/조회 API 응답에 가격이 포함된다.
- 예약 생성 시 메뉴 가격이 `ReservationMenuItem.priceSnapshot`으로 복사된다.

### Non-goals

- 기존 데이터 백필
- 가격 할인/세금/통화 정책 도입
- 가격 필수화

## Test List

- [ ] `ShopMenu` 엔티티가 `price` 컬럼을 매핑한다
  - 목적: 메뉴 원본 가격 저장소를 도메인에 추가
  - 입력: `ShopMenu.create(..., price=50000L, ...)`
  - 출력: 저장 후 `price=50000L`
  - 엣지케이스: `price=null` 허용
  - 관측 포인트: JPA 매핑과 getter 값

- [ ] 메뉴 생성 서비스가 가격을 응답에 담아 반환한다
  - 목적: 생성 API 계약에 가격 포함
  - 입력: `CreateShopMenuRequest(name, description, price=50000L, sortOrder)`
  - 출력: `ShopMenuResponse.price=50000L`
  - 엣지케이스: `price=null`
  - 관측 포인트: 서비스 응답 DTO

- [ ] 메뉴 수정 서비스가 전달된 가격만 반영한다
  - 목적: 수정 시 부분 업데이트 유지
  - 입력: 기존 `price=50000L`, 요청 `price=70000L`
  - 출력: 수정 후 `price=70000L`
  - 엣지케이스: 요청 `price=null`이면 기존 값 유지
  - 관측 포인트: update 이후 엔티티/응답 값

- [ ] 예약 메뉴 저장 시 원본 메뉴 가격이 `priceSnapshot`으로 복사된다
  - 목적: 가격 스냅샷 정합성 복구
  - 입력: `ShopMenu.price=50000L`
  - 출력: 저장된 `ReservationMenuItem.priceSnapshot=50000L`
  - 엣지케이스: 메뉴 가격이 없으면 `priceSnapshot=null`
  - 관측 포인트: `ReservationMenuItemService.saveMenuItems()` 결과

- [ ] 원본 메뉴 가격 변경 후 기존 예약의 `priceSnapshot`은 유지된다
  - 목적: 스냅샷 보존 보장
  - 입력: 예약 저장 후 메뉴 가격을 다른 값으로 변경
  - 출력: 기존 예약 항목의 `priceSnapshot`은 원래 값 유지
  - 엣지케이스: 없음
  - 관측 포인트: DB 재조회 값

## Notes

- Flyway migration 테스트는 별도 통합 시나리오보다 JPA 저장/조회와 서비스 테스트 중심으로 확인한다.
- `go`는 위 체크리스트의 다음 미체크 1개에 대해서만 수행한다.
- 구현 전 문서 승인 키워드 `동의`가 필요하다.
