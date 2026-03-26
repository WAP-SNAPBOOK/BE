# `#109` 작업 기록

작성 시작일: `2026-03-22`

## 문서 목적

이 문서는 `#109` 작업을 **커밋 가능한 작은 단위**로 나눠 기록하는 작업 로그다.

각 단계마다 아래를 남긴다.

- 무엇을 바꿨는지
- 왜 그 단계를 먼저 했는지
- 어디까지 검증했는지
- 아직 하지 않은 것과 다음 단계가 무엇인지

앞으로 `#109`를 진행하면서 이 문서에 계속 누적 기록한다.

---

## 기록 원칙

### 1. 커밋 단위로 자른다

- 구조 변경과 동작 변경을 한 단계에 섞지 않는다.
- 한 단계는 사용자가 diff를 보고 "지금 커밋해도 되겠다"라고 판단할 수 있을 정도로 작게 유지한다.

### 2. 실제 git commit과는 구분한다

- 현재까지는 **커밋 단위로 작업**했지만, 실제 `git commit`은 하지 않았다.
- 이유:
    - 사용자 요청대로 직접 확인 후 커밋할 수 있게 하기 위함
    - 자동 커밋은 프로젝트 규칙에도 맞지 않음

즉:

- `커밋 단위로 작업했다` = 맞다
- `실제로 커밋했다` = 아니다

### 3. 한 단계가 끝날 때마다 상태를 남긴다

- 성공한 검증
- 외부 요인으로 막힌 검증
- 다음 단계 진입 조건

---

## Step 0. 설계 기준 재정의

### 왜 했나

초기에는 `전역 Tag + 매장별 정렬 보조 테이블(shop_tag_orders)` 방향으로 탐색했지만,
중간에 요구사항이 명확해졌다.

- 태그는 전역적인 속성을 가지면 안 된다.
- 태그는 특정 매장 안에서만 통용되어야 한다.

이 요구사항은 단순 정렬 기능 추가가 아니라 **도메인 모델 자체를 다시 정의해야 하는 수준**이라서,
구현 전에 문서 기준을 먼저 갈아엎는 것이 필요했다.

### 무엇을 했나

아래 문서를 새 요구사항 기준으로 다시 작성했다.

- [0001-github-issue-draft.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/0001-github-issue-draft.md)
- [design-plan.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/design-plan.md)
- [plan.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/04-tdd/plan.md)

### 핵심 결정

- `global tags + shop_tag_orders` 안은 폐기
- 채택안은 `shop_tags` 기반 매장 로컬 태그 모델
- 첫 TDD 시작점도 정렬 조회가 아니라
  `매장 스코프 태그 생성 invariant`로 변경

### 이 단계에서 하지 않은 것

- 실제 코드 변경
- 마이그레이션 반영
- API 전환

---

## Step 1. 매장 로컬 태그 생성 invariant 최소 구현

### 왜 이 단계가 먼저였나

새 요구사항의 핵심은 조회나 정렬이 아니라 **태그의 정체성 변경**이다.

즉 먼저 고정해야 할 것은 아래 invariant다.

- 태그는 `shopId` 소속이어야 한다.
- 같은 이름 태그가 서로 다른 매장에서 각각 존재할 수 있어야 한다.

이걸 먼저 고정하지 않으면 이후의 조회, 메뉴 연결, 정렬 API도 모두 잘못된 전제를 따라가게 된다.

### 이번 단계 목표

- `ShopTag`라는 새 매장 로컬 태그 모델을 추가한다.
- 최소 서비스 메서드로 `shopId` 기준 태그 생성을 지원한다.
- 서로 다른 두 매장에서 같은 이름 태그를 만들 수 있다는 테스트를 추가한다.

### 무엇을 바꿨나

#### 새로 추가한 파일

- [ShopTag.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/domain/ShopTag.java)
- [ShopTagRepository.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/repository/ShopTagRepository.java)
- [V6__shop_tags.sql](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/resources/db/migration/V6__shop_tags.sql)
- [V6__shop_tags.sql](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/resources/db/migration-test/V6__shop_tags.sql)
- [ShopTagMigrationTablesTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/ShopTagMigrationTablesTest.java)

#### 수정한 파일

- [TagService.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/service/TagService.java)
- [TagResponse.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/dto/response/TagResponse.java)
- [TagServiceTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/service/TagServiceTest.java)

### 세부 변경 내용

#### 1. `shop_tags` 테이블 추가

`shop_tags`는 아래 의미를 가진다.

- `shop_id`: 태그 소속 매장
- `name`: 매장 내부 태그명
- `sort_order`: 매장 내부 정렬 순서

제약은 아래로 뒀다.

- `UNIQUE (shop_id, name)`
- `UNIQUE (shop_id, sort_order)`

즉:

- 같은 매장에서는 같은 이름 중복 금지
- 다른 매장에서는 같은 이름 허용

#### 2. `ShopTag` 엔티티 / 리포지토리 추가

도메인 레벨에서 태그의 소속과 정렬 순서를 함께 표현하도록 했다.

리포지토리에는 우선 아래만 넣었다.

- `findByShopIdAndName(...)`
- `findByShopIdOrderBySortOrderAsc(...)`
- `findMaxSortOrderByShopId(...)`

이 단계에서는 생성 invariant만 필요하므로 조회 메서드는 최소만 넣었다.

#### 3. `TagService.createShopTag(shopId, name)` 추가

기존 `createOrGet(name)`는 전역 태그 전제다.

이번 단계에서는 그 메서드를 제거하지 않고,
새 요구사항용 최소 메서드로 `createShopTag(shopId, name)`를 추가했다.

동작은 아래와 같다.

- 같은 매장에 같은 이름이 이미 있으면 기존 row 반환
- 없으면 해당 매장의 마지막 순서 뒤에 새 row 생성

이 방식은 다음 단계에서 API를 붙이기 쉬운 최소 구현이다.

#### 4. 테스트 추가

`TagServiceTest`에 아래 시나리오를 추가했다.

- 서로 다른 매장 2곳에서 같은 이름 `손관리`를 생성
- 각각 별도 row로 저장되는지 확인
- 두 row가 모두 `sort_order=0`으로 시작하는지 확인

### 이번 단계에서 제거/정리한 것

이전 전제 기반으로 임시 추가했던 아래 `shop_tag_orders` 스캐폴딩은 삭제했다.

- `ShopTagOrder.java`
- `ShopTagOrderRepository.java`
- `V6__shop_tag_orders.sql`
- `ShopTagOrderMigrationTablesTest.java`
- 테스트용 `V6__shop_tag_orders.sql`

이유:

- 새 설계 기준에서는 `shop_tag_orders`가 아니라 `shop_tags`가 태그와 순서를 함께 가져야 하기 때문이다.

### 검증 결과

#### 성공

- `./gradlew compileJava`
    - 결과: 성공

#### 막힌 것

- `./gradlew test --tests "com.example.easybooking.shop.service.TagServiceTest"`
    - 결과: 실패
    - 직접
      원인: [ReservationUnitTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/reservation/domain/ReservationUnitTest.java)
      의 컴파일 오류
    - 상세: `Reservation.createReservation(...)` 호출 인자 개수가 현재 시그니처와 맞지 않음

즉, 이번 단계의 태그 변경 자체보다 **워크트리의 별도 테스트 컴파일 오류**가 먼저 빌드를 막고 있다.

### 이번 단계에서 의도적으로 하지 않은 것

- `POST /api/shops/{shopId}/tags` 컨트롤러 추가
- 같은 매장 내 중복 이름 생성 실패 정책 확정
- `GET /api/shops/{shopId}/tags` 조회 구현
- 메뉴-태그 연결 구조를 `shop_tag_id`로 전환
- `PUT /api/shops/{shopId}/tags/order` 구현

### 현재 판단

이 단계는 "도메인 방향 전환 + 최소 생성 경로 추가"라는 기준에서 **커밋 가능한 단위**다.

사용자가 지금 확인해야 할 핵심은 아래다.

- `shop_tags`라는 새 모델 방향이 맞는지
- 같은 이름 태그의 교차 매장 허용 정책이 맞는지
- `createShopTag`를 임시 진입점으로 두는 방식이 괜찮은지

---

## 다음 단계 후보

현재 가장 자연스러운 다음 단위는 아래 둘 중 하나다.

### 후보 A. `POST /api/shops/{shopId}/tags` 추가

목적:

- 지금 만든 `createShopTag`를 실제 운영자 API로 올린다.
- 같은 매장 내 중복 이름 처리 정책까지 계약으로 고정한다.

장점:

- 도메인 변경을 바로 API로 연결할 수 있다.
- 이후 프론트/관리자 기능과 맞닿는다.

### 후보 B. `GET /api/shops/{shopId}/tags` 조회 추가

목적:

- 사용자용 visible 태그 조회를 먼저 만든다.

장점:

- 사용자 노출 계약을 빠르게 고정할 수 있다.

현재는 **후보 A가 더 자연스럽다**고 본다.  
이유는 생성 API가 있어야 이후 조회/연결/정렬의 데이터 생산 경로가 명확해지기 때문이다.

---

## 현재 상태 요약

- 문서 기준은 `shop_tags` 기반 매장 로컬 태그 모델로 확정
- 1차 구현 단위는 `매장 로컬 태그 생성 invariant`까지 반영
- 메인 코드 컴파일은 통과
- 테스트 전체는 unrelated `ReservationUnitTest` 컴파일 오류로 아직 막힘
- 실제 git commit은 하지 않았음

---

## Step 2. `POST /api/shops/{shopId}/tags` 생성 API 추가

### 왜 이 단계를 했나

Step 1에서 `ShopTag`와 `TagService.createShopTag(...)`까지는 들어갔지만,
아직 실제 애플리케이션에서 사용할 운영자 API가 없었다.

도메인 모델이 생겼다면 다음으로 필요한 최소 연결은 아래다.

- 점주가 자기 매장에 태그를 생성할 수 있어야 한다.
- 타인 매장에는 태그를 만들 수 없어야 한다.

즉, 이번 단계는 `ShopTag` 생성 경로를 **서비스 내부 helper 수준에서 실제 API 계약 수준으로 올리는 작업**이다.

### 이번 단계 목표

- `POST /api/shops/{shopId}/tags` 추가
- 점주 소유 검증 추가
- 성공 / 권한 실패 통합 테스트 추가

### 무엇을 바꿨나

#### 새로 추가한 파일

- [CreateShopTagRequest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/dto/request/CreateShopTagRequest.java)
- [TagControllerIntegrationTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/presentation/TagControllerIntegrationTest.java)

#### 수정한 파일

- [TagController.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/presentation/TagController.java)
- [TagService.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/service/TagService.java)
- [TagServiceTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/service/TagServiceTest.java)

### 세부 변경 내용

#### 1. 요청 DTO 분리

`CreateTagRequest`는 기존 전역 태그 API 의미를 담고 있어서,
매장 로컬 태그 생성과 의미가 섞이지 않게 `CreateShopTagRequest`를 따로 추가했다.

현재 필드는 단순히 `name` 하나지만, 이후 정책이 커져도 전역 API와 분리해서 진화시킬 수 있다.

#### 2. `POST /api/shops/{shopId}/tags` 추가

[TagController.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/presentation/TagController.java)
에 아래 엔드포인트를 추가했다.

- `POST /api/shops/{shopId}/tags`

특징:

- `@RequireAuthenticatedUser`를 사용해 점주 컨텍스트를 받음
- `201 Created`로 응답
- 응답 바디는 `TagResponse`

#### 3. owner 검증 추가

`TagService`에 아래 경로를 추가했다.

- `createShopTag(shopId, ownerUserId, name)`

이 메서드는 내부에서:

- `ShopReader.isShopOwnedBy(shopId, ownerUserId)` 검증
- 실패 시 `ShopErrorCode.SHOP_OWNER_MISMATCH`
- 성공 시 기존 `createShopTag(shopId, name)` 호출

즉, 이번 단계에서는 "운영자 API에서 최소한 자기 매장만 조작 가능하다"는 계약만 먼저 고정했다.

#### 4. 통합 테스트 추가

[TagControllerIntegrationTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/presentation/TagControllerIntegrationTest.java)
에 아래 2개를 추가했다.

- 점주가 자기 매장에 태그를 생성하면 `201 Created`
- 타인 매장에 생성 요청하면 `403 Forbidden + SHOP_OWNER_MISMATCH`

이 단계에서는 일부러 아래는 아직 테스트하지 않았다.

- 같은 매장 내 중복 이름 처리
- 입력값 validation 상세
- 정렬 재배치

범위를 작게 유지하려는 의도다.

### 이번 단계에서 한 의사결정

같은 매장에 같은 이름으로 다시 생성 요청했을 때,
이번 단계에서는 **기존 row 재사용** 정책을 그대로 유지했다.

이유:

- 지금 단계의 핵심은 API 공개와 권한 검증이지 중복 정책 확정이 아님
- `409 Conflict`로 바꾸는 것은 다음 단위에서 별도로 다룰 수 있음

즉, 현재는 "생성 API를 올리는 최소 단계"에 집중했다.

### 검증 결과

#### 성공

- `./gradlew compileJava`
    - 결과: 성공

#### 아직 막힌 것

- `./gradlew test --tests "com.example.easybooking.shop.service.TagServiceTest"`
    -
    여전히 [ReservationUnitTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/reservation/domain/ReservationUnitTest.java)
    컴파일 오류로 중단

즉, 이번 단계도 테스트 전체 신뢰성은 외부 워크트리 상태에 막혀 있다.

### 이번 단계에서 하지 않은 것

- `GET /api/shops/{shopId}/tags`
- `PUT /api/shops/{shopId}/tags/order`
- `shop_menu_tags`의 `shop_tag_id` 전환
- 중복 이름 생성 요청의 실패 정책 확정

### 현재 판단

이 단계도 커밋 가능한 단위다.

사용자가 지금 확인해야 할 핵심은 아래다.

- `POST /api/shops/{shopId}/tags` 경로가 맞는지
- 생성 API에 owner 검증을 지금 단계에서 먼저 넣는 방향이 맞는지
- 같은 이름 재요청을 아직 `409`로 막지 않고 재사용으로 두는 임시 정책이 괜찮은지

---

## 다음 단계 후보 갱신

이제 다음 단계 후보는 아래 둘이다.

### 후보 A. 같은 매장 중복 이름 정책 확정

목적:

- 현재 `createShopTag`의 재사용 동작을
    - 그대로 둘지
    - `409 Conflict`로 바꿀지
      계약으로 확정

### 후보 B. `GET /api/shops/{shopId}/tags` 추가

목적:

- 사용자용 visible 태그 조회를 구현
- 활성 메뉴 연결 기준 / 정렬 기준을 고정

현재는 **후보 B가 더 자연스럽다**고 본다.  
이유는 생성 경로가 생겼으니 이제 읽기 계약을 붙여도 흐름이 자연스럽기 때문이다.

---

## Step 3. `GET /api/shops/{shopId}/tags` 사용자 조회 API 추가

### 왜 이 단계를 했나

Step 2까지는 매장 로컬 태그를 만들 수는 있었지만,
사용자 화면에서 실제로 노출할 태그 목록을 읽어오는 API가 아직 없었다.

`#109`의 사용자 가치 기준으로 보면 최소한 아래 계약은 빨리 고정해야 한다.

- 활성 메뉴에 연결된 태그만 보인다.
- 같은 태그가 여러 메뉴에 붙어도 한 번만 보인다.
- 매장 내부 저장 순서대로 보인다.

### 이번 단계의 제약

현재 구조는 과도기 상태다.

- `shop_tags`는 이미 도입됨
- 하지만 `shop_menu_tags`는 아직 전역 `tag_id`를 참조함

즉, 아직 `shop_tag_id`로 완전히 전환되지 않았기 때문에,
이번 단계의 조회 구현은 **태그명 매칭 기반 과도기 경로**로 넣었다.

이건 최종형이 아니라, 다음 구조 전환 전까지의 임시 브리지다.

### 무엇을 바꿨나

#### 수정한 파일

- [ShopTagRepository.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/repository/ShopTagRepository.java)
- [TagService.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/service/TagService.java)
- [TagController.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/presentation/TagController.java)
- [TagServiceTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/service/TagServiceTest.java)
- [TagControllerIntegrationTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/presentation/TagControllerIntegrationTest.java)

### 세부 변경 내용

#### 1. visible 태그 조회 쿼리 추가

`ShopTagRepository`에 `findVisibleByShopIdOrderBySortOrderAsc(shopId)`를 추가했다.

쿼리 개념은 아래와 같다.

- `shop_tags`
- `shop_menus`
- `shop_menu_tags`
- 기존 전역 `tags`

를 조인해서:

- 같은 매장
- 활성 메뉴만
- `shop_tags.name == tags.name`

인 항목만 visible로 본다.

즉, 지금 단계에서는 "메뉴에 붙은 전역 태그 이름"과 "매장 로컬 태그 이름"이 일치하면
그 매장 태그를 visible로 판단한다.

#### 2. 서비스 메서드 추가

`TagService.getVisibleShopTags(shopId)`를 추가했다.

동작은 단순하다.

- repository 조회
- `TagResponse` 변환

권한 검증은 넣지 않았다.

이유:

- 이 API는 사용자용 공개 조회 계약으로 잡고 있기 때문

#### 3. 컨트롤러 엔드포인트 추가

[TagController.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/presentation/TagController.java)
에 아래를 추가했다.

- `GET /api/shops/{shopId}/tags`

응답은 `List<TagResponse>`다.

#### 4. 테스트 추가

서비스 테스트와 통합 테스트에 아래 계약을 넣었다.

- 활성 메뉴에 연결된 태그만 반환
- 동일 태그가 여러 메뉴에 연결돼도 한 번만 반환
- `shop_tags.sort_order ASC` 순서 보장
- 비활성 메뉴 전용 태그는 제외

### 이번 단계에서 일부러 하지 않은 것

- `shop_menu_tags.shop_tag_id` 전환
- 메뉴 필터 API의 `tagIds` 의미 전환
- hidden 태그 재정렬 API
- 메뉴 연결 API의 `shopTagId` 전환

### 중요한 한계

이번 구현은 **과도기 구현**이다.

현재 조회는 이름 매칭에 의존한다.

즉:

- 최종형은 아님
- `shop_menu_tags`가 `shop_tag_id`를 직접 참조하도록 바뀌면 이 쿼리는 제거 대상

그래도 이번 단계를 넣은 이유는,
사용자 조회 계약을 먼저 고정해두고 다음 단계에서 연결 구조를 교체하기 위해서다.

### 검증 결과

#### 성공

- `./gradlew compileJava`
    - 결과: 성공

#### 실패 / 차단

- `./gradlew test --tests "com.example.easybooking.shop.service.TagServiceTest"`
    - 이번엔 테스트 코드 컴파일 이전에 `build/test-results/test/binary/output.bin` 삭제 실패로 중단
    - 즉, 로컬 빌드 산출물 파일 잠금 문제

이전에는 [ReservationUnitTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/reservation/domain/ReservationUnitTest.java)
컴파일 오류가 있었고,
이번 실행에서는 그 전 단계에서 파일 잠금이 먼저 걸렸다.

즉 현재 테스트 실행 신뢰성은 여전히 불안정하다.

### 현재 판단

이 단계도 커밋 가능한 단위다.

다만 사용자가 확인해야 할 핵심 포인트가 하나 있다.

- `GET /api/shops/{shopId}/tags`를 지금 과도기 이름 매칭 방식으로 먼저 여는 것이 괜찮은가

이게 괜찮다면 다음 단계는 자연스럽게 아래다.

- `shop_menu_tags`를 `shop_tag_id`로 전환
- 메뉴 연결 API도 그 기준으로 바꾸기

---

## 다음 단계 후보 갱신

이제 다음 단계의 우선순위는 거의 고정됐다.

### 다음 우선순위. `shop_menu_tags` -> `shop_tag_id` 전환

목적:

- 현재 과도기 이름 매칭 제거
- 메뉴-태그 연결의 정체성을 `shop_tags.id`로 고정
- 이후 정렬/hidden 정책 구현을 안정적으로 이어가기

---

## Step 4. `shop_menu_tags` 이중 경로 fallback 단계 추가

### 왜 이 단계를 했나

사용자 피드백대로 지금은 프론트가 아직 완전히 전환되기 전이다.

즉 백엔드는 새 모델로 가야 하지만, 동시에 아래 둘을 당분간 모두 받아줘야 한다.

- 기존 클라이언트가 보내는 전역 `tag_id`
- 이후 클라이언트가 보내게 될 `shop_tag_id`

그래서 이번 단계는 `shop_menu_tags`를 한 번에 갈아엎는 대신,
**이중 경로 fallback 단계**를 넣는 작업이다.

핵심 목표는 아래다.

- 연결 테이블이 `shop_tag_id`를 저장할 수 있게 만든다.
- 기존 `tag_id` 기반 요청도 당장은 안 깨지게 유지한다.
- 새 요청이 오면 가능한 한 `shop_tag_id`와 `tag_id`를 둘 다 채워서 브리지 기간을 버틴다.

### 무엇을 바꿨나

#### 새로 추가한 파일

- [V7__shop_menu_tags_add_shop_tag_id.sql](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/resources/db/migration/V7__shop_menu_tags_add_shop_tag_id.sql)
- [V7__shop_menu_tags_add_shop_tag_id.sql](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/resources/db/migration-test/V7__shop_menu_tags_add_shop_tag_id.sql)

#### 수정한 파일

- [ShopErrorCode.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/errors/errorcode/ShopErrorCode.java)
- [ShopMenuTag.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/domain/ShopMenuTag.java)
- [ShopMenuTagRepository.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/repository/ShopMenuTagRepository.java)
- [ShopMenuRepository.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/repository/ShopMenuRepository.java)
- [ShopTagRepository.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/repository/ShopTagRepository.java)
- [TagService.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/service/TagService.java)
- [TagController.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/presentation/TagController.java)
- [ShopMenuTagJpaMappingTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/domain/ShopMenuTagJpaMappingTest.java)
- [TagServiceTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/service/TagServiceTest.java)

### 세부 변경 내용

#### 1. `shop_menu_tags`에 `shop_tag_id` 추가

새 마이그레이션에서 아래를 추가했다.

- nullable `shop_tag_id`
- `UNIQUE (shop_menu_id, shop_tag_id)`
- `shop_tags(id)` FK
- 관련 인덱스

중요한 점:

- 기존 `tag_id`는 아직 제거하지 않았다.
- 즉 지금은 `tag_id`와 `shop_tag_id`를 함께 들고 있는 과도기 구조다.

이게 이번 단계의 fallback 핵심이다.

#### 2. 엔티티/리포지토리도 이중 경로로 변경

`ShopMenuTag`는 이제 아래 두 식별자를 모두 가질 수 있다.

- `tagId`
- `shopTagId`

리포지토리에는 아래 fallback 경로를 추가했다.

- `findByShopMenuIdAndShopTagId(...)`
- `findByShopMenuIdAndAnyTagId(...)`
- `deleteByShopMenuIdAndAnyTagId(...)`

즉 요청이 old `tag_id`로 오든 new `shop_tag_id`로 오든,
서비스는 같은 메서드로 찾아서 지울 수 있다.

#### 3. 메뉴-태그 연결도 fallback 지원

`TagService.addTagToMenu(shopId, menuId, tagId)`는 이제 요청 ID를 아래 순서로 해석한다.

- 먼저 `shopTagRepository.findById(tagId)`로 `shop_tag_id`인지 확인
- 아니면 기존 전역 `Tag`의 `tag_id`로 해석

동작은 다음과 같다.

##### 새 `shop_tag_id`가 들어온 경우

- 해당 태그가 같은 매장 소속인지 검증
- 같은 이름의 전역 `Tag`가 없으면 생성
- `shop_menu_tags`에 `tag_id + shop_tag_id`를 함께 저장

##### 기존 `tag_id`가 들어온 경우

- 전역 `Tag`를 읽음
- 같은 이름의 `ShopTag`가 해당 매장에 없으면 자동 생성
- `shop_menu_tags`에 `tag_id + shop_tag_id`를 함께 저장

즉 기존 프론트가 예전 방식으로 요청해도,
백엔드는 내부적으로 `shop_tags` 쪽 데이터를 계속 채워가게 된다.

#### 4. 제거 API도 fallback 지원

`removeTagFromMenu(shopId, menuId, tagId)`도 같은 방식으로 바꿨다.

- 메뉴가 해당 매장 소속인지 먼저 확인
- 요청 ID가 `shop_tag_id`면 같은 매장 태그인지 확인
- 삭제는 `shop_menu_id + (shop_tag_id or tag_id)` 기준으로 수행

즉 old/new 어느 ID로 지워도 같은 row를 제거할 수 있다.

#### 5. 메뉴 필터/조회도 fallback 인지하도록 조정

이번 단계에서 아래 쿼리도 함께 보정했다.

- `ShopMenuRepository.findActiveByShopIdAndTagIds(...)`
- `ShopTagRepository.findVisibleByShopIdOrderBySortOrderAsc(...)`

의도:

- old `tag_id` 요청도 계속 동작
- new `shop_tag_id` 요청도 받아들일 준비

다만 이 부분은 아직 완전하지 않은 과도기다.

### 이번 단계에서 추가한 검증

`ShopErrorCode`에 아래를 추가했다.

- `SHOP_MENU_MISMATCH`
- `SHOP_TAG_MISMATCH`

이 단계에서 필요한 이유는,

- `shopId`와 `menuId` 소속 불일치
- `shopId`와 `shopTagId` 소속 불일치

를 명시적으로 막아야 fallback이 안전하게 동작하기 때문이다.

### 이번 단계의 fallback 의미

이 단계는 최종 상태가 아니다.

지금 구조는 의도적으로 아래를 동시에 허용한다.

- old world: 전역 `tag_id`
- new world: 매장 로컬 `shop_tag_id`

즉, 프론트 전환 전까지는 브리지 기간이다.

장점:

- 기존 프론트가 즉시 깨지지 않는다.
- 백엔드는 새 모델의 데이터를 계속 축적할 수 있다.

단점:

- 읽기/쓰기 경로가 일시적으로 복잡해진다.
- 완전 전환 후 반드시 cleanup이 필요하다.

### 검증 결과

#### 성공

- `./gradlew compileJava`
    - 결과: 성공

#### 아직 못한 것

- 테스트 실행은 현재 로컬 환경에서 계속 불안정하다.
- 이전에는 unrelated 테스트 컴파일 오류,
  다른 실행에서는 `build/test-results` 파일 잠금 문제가 있었다.

즉 이번 단계도 코드 수준 검증은 했지만,
테스트 런은 아직 신뢰 가능한 상태가 아니다.

### 이번 단계에서 하지 않은 것

- `shop_menu_tags.tag_id` 완전 제거
- API 파라미터를 `tagId`에서 `shopTagId`로 명시 전환
- backfill SQL
- hidden 태그 재정렬 API

### 현재 판단

이 단계는 "프론트 전환 전 fallback 브리지"라는 목적에서 커밋 가능한 단위다.

사용자가 지금 확인해야 할 핵심은 아래다.

- 기존 프론트 호환을 위해 `tag_id`와 `shop_tag_id`를 함께 저장하는 접근이 괜찮은지
- legacy `tag_id` 요청 시 `ShopTag`를 자동 생성하는 fallback 정책이 맞는지

---

## 다음 단계 후보 갱신

이제 다음 단계는 아래 둘 중 하나다.

### 후보 A. backfill / cleanup 준비

목적:

- 기존 `shop_menu_tags` 데이터에 `shop_tag_id`를 채우는 마이그레이션 전략 정리

### 후보 B. 정렬 API 구현

목적:

- `PUT /api/shops/{shopId}/tags/order`
- hidden 태그 순서 유지

현재는 **후보 B 전에 backfill / cleanup 전략을 먼저 한 번 정리하는 편이 안전하다**고 본다.

---

## Step 5. backfill / cleanup 전략 문서화

### 왜 이 단계를 했나

Step 4까지 오면서 구조는 이미 과도기 상태가 됐다.

- `shop_tags`는 도입됨
- `shop_menu_tags`에는 `shop_tag_id`가 추가됨
- 하지만 여전히 legacy `tag_id` fallback도 살아 있음

이 상태에서 바로 정렬 API나 cleanup으로 들어가면,
"언제 어떤 기준으로 old path를 걷어낼지"가 불명확해진다.

그래서 이번 단계는 코드를 더 늘리기보다,
전환 순서를 설계 문서에 구체적으로 고정하는 작업을 먼저 했다.

### 무엇을 했나

[design-plan.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/design-plan.md)에 아래 내용을 추가/구체화했다.

- `Phase 2. Backfill` 상세 전략
- `shop_menu_tags.shop_tag_id` 채우는 매핑 규칙
- backfill 직후 검증해야 할 체크 항목
- `Phase 3. Switch` 완료 조건
- `Phase 4. Cleanup` 상세 전략
- fallback 제거 순서
- `tag_id` 컬럼 제거 전 만족해야 할 조건

### 이번에 고정한 핵심 규칙

#### 1. backfill 생성 단위

`shop_tags`는 아래 기준으로 만든다.

```text
(shop_id, legacy tag.name)
```

즉 같은 이름의 전역 태그라도 매장이 다르면 서로 다른 `shop_tags` row가 된다.

#### 2. `shop_tag_id` backfill 규칙

각 `shop_menu_tags` row는 아래 경로로 새 태그에 연결한다.

```text
shop_menu_tags(tag_id)
-> tags(name)
-> (shop_id, name)
-> shop_tags(id)
```

#### 3. cleanup은 마지막 단계다

아래는 cleanup 직전까지 유지한다.

- `tag_id` 컬럼
- legacy `tag_id` fallback 코드
- 이름 매칭 브리지 조회

즉 지금은 일부러 이중 경로를 유지하고,
프론트 전환과 backfill 검증이 끝난 뒤 마지막에 제거한다.

### 왜 중요한가

지금 단계에서 제일 위험한 건
"이미 새 구조를 일부 도입했으니 old path를 바로 지워도 되겠지"
라고 판단하는 것이다.

하지만 실제론 아직 프론트 전환 전이므로,
old path 제거는 가장 마지막 단계여야 한다.

이번 문서화는 그 순서를 팀 차원에서 고정하려는 목적이다.

### 검증

이번 단계는 문서 작업만 수행했다.

즉:

- 코드 변경 없음
- 빌드/테스트 검증 없음

### 현재 판단

이 단계 역시 커밋 가능한 단위다.

이제 다음 단계는 문서 기준이 충분히 고정됐으므로,
다시 코드 단계로 돌아가 `PUT /api/shops/{shopId}/tags/order`를 구현해도 된다.

---

## 다음 단계 후보 갱신

이제 다음 우선순위는 아래로 본다.

### 다음 우선순위. 정렬 API 구현

목적:

- `PUT /api/shops/{shopId}/tags/order`
- visible 태그 전체 정렬
- hidden 태그는 기존 상대 순서 유지

이 단계부터는 `shop_tags.sort_order`를 실질적으로 운영하는 로직이 들어간다.

---

## Step 6. `PUT /api/shops/{shopId}/tags/order` 구현

### 왜 이 단계를 했나

`#109`의 핵심 기능 중 하나는 단순 조회가 아니라
운영자가 매장 태그 표시 순서를 바꿀 수 있어야 한다는 점이다.

Step 5까지는:

- 매장 로컬 태그 모델
- 생성 API
- visible 조회
- fallback 브리지

까지는 들어갔지만,
정작 `shop_tags.sort_order`를 운영자가 바꾸는 API는 아직 없었다.

따라서 이번 단계는 `sort_order`를 실제 기능으로 쓰게 만드는 첫 단계다.

### 이번 단계 목표

- `PUT /api/shops/{shopId}/tags/order` 추가
- owner 검증 추가
- visible 태그 전체 입력 검증 추가
- hidden 태그는 기존 상대 순서를 유지한 채 뒤로 보내는 canonical order 구현

### 무엇을 바꿨나

#### 새로 추가한 파일

- [UpdateShopTagOrderRequest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/dto/request/UpdateShopTagOrderRequest.java)

#### 수정한 파일

- [ShopErrorCode.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/errors/errorcode/ShopErrorCode.java)
- [ShopTagRepository.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/repository/ShopTagRepository.java)
- [TagService.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/service/TagService.java)
- [TagController.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/presentation/TagController.java)
- [TagServiceTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/service/TagServiceTest.java)
- [TagControllerIntegrationTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/presentation/TagControllerIntegrationTest.java)

### 세부 변경 내용

#### 1. 요청 DTO 추가

정렬 API는 visible 태그 전체 배열을 받으므로,
`UpdateShopTagOrderRequest`를 새로 추가했다.

형태:

```json
{
  "tagIds": [4, 1, 3]
}
```

#### 2. 새 에러 코드 추가

`ShopErrorCode`에 아래를 추가했다.

- `INVALID_SHOP_TAG_ORDER`

이 에러는 아래 상황에서 쓴다.

- visible 태그를 일부만 보낸 경우
- 중복 태그 ID를 보낸 경우
- 현재 visible 집합과 다른 ID가 들어온 경우

#### 3. 정렬 업데이트 서비스 추가

`TagService.updateShopTagOrder(shopId, ownerUserId, requestedVisibleTagIds)`를 추가했다.

동작 순서는 아래다.

1. owner 검증
2. 현재 visible 태그 목록 조회
3. 요청이 visible 전체와 정확히 일치하는지 검증
4. 현재 전체 `shop_tags` 순서 조회
5. canonical order 계산

```text
[요청 visible] + [hidden 기존 순서]
```

6. 순서 재할당

#### 4. 유니크 충돌 회피 방식

`shop_id + sort_order` 유니크가 있기 때문에,
정렬을 바로 `0..N-1`로 다시 쓰면 중간에 충돌할 수 있다.

그래서 이번 단계에서는 아래 2단계로 처리했다.

1. 해당 매장 전체 `sort_order`를 `+N`만큼 한 번 밀기
2. 최종 canonical order를 `0..N-1`로 다시 쓰기

이 방식이면 낮은 번호 슬롯이 비어 있는 상태에서 재배치하므로
중간 unique 충돌을 피할 수 있다.

#### 5. 컨트롤러 엔드포인트 추가

[TagController.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/presentation/TagController.java)에 아래를 추가했다.

- `PUT /api/shops/{shopId}/tags/order`

운영자 API이므로:

- `@RequireAuthenticatedUser`
- owner 검증 경로 사용

### 테스트에서 고정한 계약

#### 서비스 테스트

- visible 태그 순서를 바꾸면 hidden 태그는 뒤에 그대로 유지
- visible 일부만 보내면 실패

#### 통합 테스트

- 정렬 요청 성공 시 DB의 `sort_order` 반영
- visible 일부만 보내면 `400 + INVALID_SHOP_TAG_ORDER`

### 이번 단계에서 하지 않은 것

- partial move API
- hidden 태그를 payload로 직접 보내는 관리 API
- 프론트의 drag-and-drop 상세 UX 계약

### 검증 결과

#### 성공

- `./gradlew compileJava`
  - 결과: 성공

#### 아직 못한 것

- 테스트 실행은 여전히 현재 워크트리 상태상 신뢰하기 어렵다.
- 이전 단계들에서 확인한 unrelated 컴파일 오류 / 파일 잠금 이슈가 남아 있다.

즉, 이번 단계는 코드와 컴파일 기준으로는 맞춰졌고,
테스트 런은 환경 안정화 후 다시 확인해야 한다.

### 현재 판단

이 단계도 커밋 가능한 단위다.

사용자가 지금 확인해야 할 핵심은 아래다.

- 정렬 입력을 "visible 전체 배열 overwrite"로 두는 것이 맞는지
- hidden 태그를 자동으로 뒤에 유지하는 규칙이 맞는지
- unique 충돌 회피를 위한 2단계 sort 갱신 방식이 괜찮은지

---

## 다음 단계 후보 갱신

이제 큰 줄기의 남은 일은 cleanup 쪽으로 가까워졌다.

### 다음 우선순위. cleanup 준비 또는 중복 정책 확정

후보는 두 가지다.

1. 같은 매장 태그명 중복 생성 요청을 `409`로 바꿀지 여부 확정
2. fallback 사용 경로를 줄이기 위한 cleanup 준비

현재는 **중복 생성 정책부터 먼저 확정하는 편이 API 의미를 더 깔끔하게 만든다**고 본다.

---

## Step 7. 같은 매장 태그명 중복 생성 정책을 `409 Conflict`로 확정

### 왜 이 단계를 했나

지금까지 `POST /api/shops/{shopId}/tags`는 같은 이름이 이미 있으면
기존 row를 재사용하는 임시 정책이었다.

하지만 이 정책은 운영자 API 의미를 흐린다.

- 생성 요청인데 실제로는 조회/재사용처럼 동작함
- 프론트 입장에서 "이미 있는 태그를 다시 만들려 했다"는 오류를 알기 어려움
- API 계약이 모호해짐

그래서 이번 단계에서 생성 API 의미를 명확히 고정했다.

### 정책 결정

같은 매장에 같은 이름 태그가 이미 있으면:

- 더 이상 기존 row를 재사용하지 않음
- `409 Conflict`
- `SHOP_TAG_ALREADY_EXISTS`

즉:

- 교차 매장 동일 이름 허용
- 동일 매장 동일 이름 금지

라는 규칙을 API 수준에서도 명시적으로 확정했다.

### 무엇을 바꿨나

#### 수정한 파일

- [ShopErrorCode.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/errors/errorcode/ShopErrorCode.java)
- [TagService.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/service/TagService.java)
- [TagServiceTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/service/TagServiceTest.java)
- [TagControllerIntegrationTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/presentation/TagControllerIntegrationTest.java)

### 세부 변경 내용

#### 1. 새 에러 코드 추가

`ShopErrorCode`에 아래를 추가했다.

- `SHOP_TAG_ALREADY_EXISTS`

HTTP status는 `409 Conflict`로 뒀다.

#### 2. 서비스 생성 정책 변경

`TagService.createShopTag(shopId, name)`는 이제 아래 순서로 동작한다.

1. 같은 `shopId + name` 태그가 있는지 확인
2. 있으면 `SHOP_TAG_ALREADY_EXISTS` 예외 발생
3. 없으면 새 row 생성

즉 이제 생성 API는 truly create만 담당한다.

#### 3. 테스트 추가

서비스 테스트:

- 같은 매장에 같은 이름을 다시 만들면 예외 발생

통합 테스트:

- `POST /api/shops/{shopId}/tags`
- 같은 이름 중복 시 `409 + SHOP_TAG_ALREADY_EXISTS`

### 이 결정의 의미

이제 API 의미가 아래처럼 더 선명해졌다.

- `POST /api/shops/{shopId}/tags`
  - 새 태그 생성
  - 이미 있으면 실패

반대로 legacy fallback은 여전히 일부 경로에 남아 있다.

예:

- old `tag_id` 기반 메뉴 연결 요청이 들어왔을 때
- 내부적으로 대응되는 `ShopTag`가 없으면 자동 생성 가능

즉 "운영자 명시적 생성 API"는 엄격하게 만들고,
"브리지 fallback"은 전환기 때문에 예외적으로 남겨두는 구조다.

### 검증 결과

#### 성공

- `./gradlew compileJava`
  - 결과: 성공

#### 아직 못한 것

- 테스트 런은 현재 워크트리/환경 문제 때문에 여전히 안정적으로 돌리지 못했다.

### 현재 판단

이 단계도 커밋 가능한 단위다.

지금 기준으로 생성 API 의미는 꽤 정리됐다.
남은 큰 축은 fallback cleanup을 언제 어떻게 줄일지와
최종적으로 `tag_id`를 제거하는 타이밍이다.

---

## Step 8. backfill SQL / migration-test 추가

### 왜 이 단계를 했나

Step 5에서 backfill 전략은 문서로 정리했지만,
실제 SQL은 아직 없었다.

지금 상태에서는:

- `shop_tags` 테이블은 이미 있음
- `shop_menu_tags.shop_tag_id`도 이미 추가됨
- fallback 코드도 일부 존재함

하지만 기존 데이터가 있는 환경이라면
`shop_tag_id`가 채워지지 않은 row가 남아 있을 수 있다.

따라서 cleanup이나 fallback 축소 전에,
기존 데이터를 새 구조로 옮기는 실제 migration이 필요했다.

### 무엇을 바꿨나

#### 새로 추가한 파일

- [V8__backfill_shop_tags.sql](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/resources/db/migration/V8__backfill_shop_tags.sql)
- [V8__backfill_shop_tags.sql](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/resources/db/migration-test/V8__backfill_shop_tags.sql)
- [ShopTagBackfillMigrationTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/ShopTagBackfillMigrationTest.java)

#### 수정한 파일

- [V6__shop_tags.sql](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/resources/db/migration-test/V6__shop_tags.sql)
- [V7__shop_menu_tags_add_shop_tag_id.sql](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/resources/db/migration-test/V7__shop_menu_tags_add_shop_tag_id.sql)

### 세부 변경 내용

#### 1. 운영용 `V8` backfill SQL 추가

운영용 마이그레이션은 두 일을 한다.

##### A. `shop_tags` 생성 backfill

기존 데이터를 기준으로:

```text
shop_menu_tags(tag_id)
-> tags(name)
-> shop_menus(shop_id)
```

를 따라가며 `(shop_id, name)` 기준의 missing `shop_tags` row를 생성한다.

정렬은 매장 내부에서:

- 기존 `legacy tag.id`
- 기존 `shop_tags` 최대 `sort_order`

를 기준으로 뒤에 이어 붙이는 방식으로 계산했다.

즉 이미 일부 `shop_tags`가 생성돼 있어도 충돌하지 않도록 했다.

##### B. `shop_menu_tags.shop_tag_id` backfill

각 row에 대해:

- `shop_menu_id -> shop_id`
- `tag_id -> tag.name`
- `(shop_id, name) -> shop_tags.id`

를 따라가 `shop_tag_id`를 채운다.

#### 2. migration-test도 같이 보강

H2에서 backfill 흐름을 최소 검증할 수 있게
test migration 쪽도 손봤다.

특히:

- `tags`
- `shop_menus`
- `shop_menu_tags`

의 최소 테이블을 `V6` test migration에서 만들고,
`V8` test migration에서 seed + backfill을 같이 수행하게 했다.

#### 3. backfill 결과 확인 테스트 추가

[ShopTagBackfillMigrationTest.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/test/java/com/example/easybooking/shop/ShopTagBackfillMigrationTest.java)에서 아래를 확인하도록 했다.

- 매장 10에 `손관리`, `발관리`가 생성되는지
- 매장 20에 `손관리`가 별도 row로 생성되는지
- `shop_menu_tags.shop_tag_id IS NULL`이 0건인지

즉 migration-test 기준으로는
"매장별 태그 분리"와 "`shop_tag_id` 채움"까지를 검증하는 셈이다.

### 이번 단계에서 하지 않은 것

- 운영 환경 데이터 검증 쿼리 실행
- 실제 `tag_id` 컬럼 제거
- fallback 코드 제거

### 검증

#### 성공

- 메인 코드 기준 `compileJava`는 깨지지 않음

#### 아직 못한 것

- migration test를 포함한 전체 테스트 실행은 여전히 현재 워크트리/환경 이슈 때문에 안정적으로 돌리지 못했다

### 현재 판단

이 단계는 cleanup 전에 필요한 준비물로서 커밋 가능한 단위다.

이제 남은 큰 작업은 아래 둘로 압축된다.

1. fallback 코드 축소 / 제거 시점 결정
2. 실제 `tag_id` 제거 전 마지막 cleanup 순서 반영

---

## Step 9. legacy 경로 deprecated / 관측 가능 상태로 전환

### 왜 이 단계를 했나

지금 시점에서 fallback을 바로 제거하면 프론트 전환 전에 깨질 수 있다.

하지만 반대로 아무 표시 없이 계속 두면,
"이 경로가 아직 실제로 쓰이는지"를 판단하기 어렵다.

즉 cleanup을 안전하게 하려면,
old path를 유지하되 **deprecated 상태**로 명시하고
실제 사용 시 관측 가능하게 만들어야 한다.

### 무엇을 바꿨나

#### 수정한 파일

- [TagService.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/service/TagService.java)
- [TagController.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/presentation/TagController.java)

### 세부 변경 내용

#### 1. legacy 전역 태그 API를 deprecated 표시

아래 엔드포인트에 `@Deprecated(forRemoval = false)`를 붙였다.

- `POST /api/tags`
- `GET /api/tags`

의미:

- 아직 제거하지는 않음
- 하지만 더 이상 권장 경로가 아님을 코드 수준에서 명시

#### 2. legacy 전역 태그 서비스도 deprecated 표시

`TagService`의 아래 메서드도 deprecated 표시했다.

- `createOrGet(String name)`
- `getAllTags()`

즉 새 작업은 `shop_tags` 경로를 기준으로 해야 하고,
전역 `tags` 경로는 cleanup 대상임을 분명히 했다.

#### 3. fallback 사용 로그 추가

아래 상황에서 `warn` 로그가 남도록 했다.

- legacy global tag create/list API 사용
- `resolveLegacyTagIds(...)` 경로를 타서 old `tag_id` fallback이 사용된 경우

즉 앞으로는 운영/테스트 로그를 보면:

- 전역 `/api/tags`가 아직 호출되는지
- 메뉴 연결 시 legacy `tag_id`가 아직 들어오는지

를 확인할 수 있다.

### 왜 이게 중요한가

cleanup의 제일 어려운 점은 "언제 지워도 되는지"를 모르는 것이다.

이번 단계로 최소한 아래 질문에는 답할 수 있게 됐다.

- 전역 `tags` API를 아직 누가 쓰는가
- 메뉴 연결 fallback이 아직 실제로 호출되는가

즉, 지금은 제거보다 **관측 가능성 확보**가 우선이라는 판단이다.

### 검증 결과

#### 성공

- `./gradlew compileJava`
  - 결과: 성공

### 현재 판단

이 단계도 커밋 가능한 단위다.

이제 다음 cleanup 단계는 더 명확하다.

- 로그상 legacy 경로 사용이 사라졌는지 확인
- 사라졌다면 fallback 코드 제거 / `tag_id` 제거 순서로 이동

---

## Step 10. visible 태그 조회 쿼리 분리로 가독성 개선

### 왜 이 단계를 했나

Step 4에서 사용자용 visible 태그 조회를 붙일 때는
과도기 fallback까지 한 JPQL 안에 넣어 빠르게 계약을 고정했다.

하지만 이후 코드를 설명하는 과정에서 문제가 분명해졌다.

- 같은 쿼리 안에 정상 경로와 fallback 경로가 섞여 있었다.
- `join` 순서와 `or` 조건을 한 번에 읽어야 해서 이해 비용이 컸다.
- 성능보다 먼저 유지보수성과 설명 가능성이 떨어졌다.

즉 지금 필요한 건 동작 변경이 아니라,
현재 브리지 구조를 **더 읽기 쉬운 형태로 분해하는 구조 변경**이었다.

### 이번 단계 목표

- visible 조회 로직의 의미를 더 직접적으로 드러낸다.
- 정상 경로와 fallback 경로를 repository 메서드 단위로 분리한다.
- 서비스에서 merge/dedup/sort를 담당하게 해 읽기 부담을 줄인다.

### 무엇을 바꿨나

#### 수정한 파일

- [ShopTagRepository.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/repository/ShopTagRepository.java)
- [TagService.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/service/TagService.java)
- [code-walkthrough.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/code-walkthrough.md)
- [commit-unit-flow.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/commit-unit-flow.md)

### 세부 변경 내용

#### 1. `ShopTagRepository`의 visible 조회를 두 쿼리로 분리

기존에는 아래 의미를 한 JPQL에 담고 있었다.

- 새 구조의 직접 연결 `smt.shopTagId = st.id`
- legacy fallback `smt.shopTagId is null and t.name = st.name`

이제는 이를 아래 두 메서드로 나눴다.

- `findVisibleByShopTagIdOrderBySortOrderAsc(...)`
- `findVisibleByLegacyTagFallbackOrderBySortOrderAsc(...)`

의미는 각각 명확하다.

- 첫 번째는 최종형에 가까운 정상 경로
- 두 번째는 전환기 동안만 필요한 fallback 경로

#### 2. 서비스에서 merge / dedup / sort 수행

`TagService`에 `readVisibleShopTags(shopId)` helper를 추가했다.

동작은 아래 순서다.

1. 정상 경로 조회
2. fallback 경로 조회
3. `ShopTag.id` 기준 dedup
4. `sortOrder`, `id` 기준 정렬

즉 이제 `getVisibleShopTags(...)`와 `updateShopTagOrder(...)`는
"visible 태그를 읽는다"는 공통 의도를 같은 helper로 공유한다.

#### 3. 동작은 유지하고 설명 가능성만 높임

이번 단계는 기능 계약을 바꾸지 않았다.

- visible 조건은 그대로
- fallback 유지도 그대로
- 최종 응답 순서도 그대로

바뀐 것은 "이 로직을 어디에서 어떻게 읽느냐"다.

### 왜 이 구조가 더 낫나

이전 구조는 쿼리 하나만 보면 끝난다는 장점이 있었지만,
정상 경로와 fallback 경로가 섞여 있어서 설명이 어려웠다.

반대로 지금 구조는:

- repository 메서드 이름만 봐도 두 경로가 분리돼 보이고
- service helper에서 브리지 merge를 의식적으로 수행하므로
- cleanup 때 fallback 메서드 하나와 merge 일부를 제거하면 된다는 점이 더 분명해진다

즉 현재 단계의 최적화는 DB 미세튜닝이 아니라
**전환기 코드를 사람이 이해할 수 있게 정리하는 것**이었다.

### 검증 결과

#### 성공

- `./gradlew compileJava`
  - 결과: 성공

### 이번 단계에서 하지 않은 것

- fallback 제거
- `shop_menu_tags.tag_id` 제거
- visible 조회 테스트 계약 변경

즉 이 단계는 순수하게 읽기 구조를 정리한 단계다.

### 현재 판단

이 단계도 커밋 가능한 단위다.

사용자가 지금 확인해야 할 핵심은 아래다.

- 복잡한 visible 조회를 repository 두 개 + service merge로 나눈 방향이 괜찮은지
- 이 정도 분리면 cleanup 때 fallback 제거가 더 명확하게 보이는지
