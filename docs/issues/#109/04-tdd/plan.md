# TDD Next Step Plan

## 1) Current Test Snapshot
- 최근 테스트 실행 결과: `./gradlew test --tests "com.example.easybooking.shop.service.TagServiceTest"` 실행 시 `:compileTestJava` 단계에서 실패한다.
- 현재 깨지는 테스트/컴파일 이슈: 사용자 워크트리의 `src/test/java/com/example/easybooking/reservation/domain/ReservationUnitTest`가 `Reservation.createReservation(...)` 시그니처와 맞지 않아 컴파일이 중단된다.
- 계약이 약한 테스트:
  - 현재 태그 테스트는 여전히 전역 `Tag` 전제를 깔고 있다.
  - "매장 스코프 uniqueness"와 "교차 매장 동일 이름 허용"을 보장하는 테스트가 없다.
  - 사용자 조회/정렬 테스트도 아직 전역 태그 모델을 벗어나지 못했다.
- 이번 스텝에서 건드리지 않을 범위(Non-goal):
  - 기존 `ReservationUnitTest` 수정
  - 전체 backfill 마이그레이션 구현
  - 정렬 변경 API 전체 구현

## 2) Next Smallest RED (exactly one)
- Test name: `createTag_createsShopLocalTag_andAllowsSameNameAcrossDifferentShops`
- Layer: `Domain/Service`
- Why this is the smallest next step:
  - 새 요구사항의 핵심은 "태그는 전역이 아니다"라는 정체성 변화다.
  - 이 invariant를 먼저 고정해야 이후 조회/연결/정렬 구현이 모두 올바른 방향으로 간다.
- Given / When / Then:
  - Given: 서로 다른 두 매장이 있다.
  - When: 각 매장에서 같은 이름의 태그 `손관리`를 생성하면
  - Then: 두 태그는 각각 생성되고, 서로 다른 `shopId` 소속의 별도 row여야 한다.
- Expected failure (error code/exception/value):
  - 현재는 전역 `Tag` 모델이라 두 번째 생성이 기존 row 재사용 또는 uniqueness 충돌 방향으로 흘러 요구사항을 만족하지 못한다.
- Observable contract (결과/상태/저장 결과):
  - 저장된 태그 row 수
  - 각 row의 `shopId`, `name`
  - 두 태그의 ID 분리 여부

## 3) GREEN Hint (minimum)
- 통과를 위한 최소 구현 방향(2~4줄):
  - `ShopTag` 엔티티와 `ShopTagRepository`를 추가한다.
  - `shopId + name` uniqueness를 모델에 반영한다.
  - `TagService.create(...)` 또는 전용 서비스 메서드를 매장 스코프 기반으로 바꾼다.
- 이번 스텝에서 하지 않을 것(리팩터링/확장):
  - visible 조회 API
  - 메뉴-태그 연결 전환
  - hidden merge 재정렬

## 4) Refactor Candidates (after green)
- 전역 `Tag`와 매장 로컬 `ShopTag` 책임 경계 정리
- 테스트 fixture에서 매장 생성 helper 도입
- 이후 `TagService`를 생성/조회/정렬 책임으로 분리할지 검토

## 5) Open Questions
- 기존 `POST /api/tags`, `GET /api/tags`를 즉시 제거할지, deprecate 후 단계적으로 걷어낼지 결정 필요
- `shop_menu_tags` 전환 중간 단계에서 `tag_id`와 `shop_tag_id`를 함께 둘 기간이 필요한지 결정 필요
