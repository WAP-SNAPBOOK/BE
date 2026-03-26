# `#109` 코드 해설 문서

작성일: `2026-03-23`

## 문서 목적

이 문서는 `#109` 작업에서 지금까지 실제로 바뀐 코드를 **파일별로, 위에서 아래로, 빠짐없이** 설명하는 문서다.

목표는 두 가지다.

1. 지금 들어간 변경을 "왜 이렇게 생겼는지" 코드 기준으로 따라갈 수 있게 한다.
2. 나중에 cleanup 할 때 "어떤 코드가 최종형이고, 어떤 코드는 과도기 fallback인지" 다시 판단할 수 있게 한다.

이 문서는 설계 문서가 아니라 **코드 해설 문서**다.  
즉 문서 안의 설명 기준은 실제 파일 내용이다.

---

## 읽는 순서

추천 읽기 순서는 아래다.

1. 도메인/DB 모델
2. Repository
3. Service
4. Controller
5. Migration SQL
6. 테스트

이 순서로 읽으면 "모델 -> 쿼리 -> 유스케이스 -> API -> DB 이행 -> 검증" 흐름이 자연스럽다.

---

## 1. `src/main/java/com/example/easybooking/shop/domain/ShopTag.java`

### 파일 역할

이 파일은 `#109`에서 새로 도입한 **매장 로컬 태그 엔티티**다.

예전에는 태그가 전역 `Tag`였다.  
이 파일은 그 전제를 깨고, "태그는 매장 소속이다"를 코드로 표현한다.

### 위에서 아래로 해설

- `package com.example.easybooking.shop.domain;`
  - 이 클래스가 `shop` 도메인 안에 속한다는 뜻이다.

- `jakarta.persistence.*`, `org.hibernate.annotations.*`
  - JPA 엔티티로 DB 테이블에 매핑하기 위한 import다.
  - `CreationTimestamp`, `UpdateTimestamp`는 생성/수정 시간을 자동으로 채우기 위한 Hibernate 기능이다.

- `@Entity`
  - 이 클래스가 JPA 엔티티라는 선언이다.

- `@Table(...)`
  - 실제 DB 테이블명이 `shop_tags`임을 선언한다.
  - `uniqueConstraints` 두 개가 중요하다.
  - `uq_shop_tags_shop_name`
    - `(shop_id, name)` 조합이 유일해야 한다.
    - 같은 매장 안에서는 같은 이름 태그를 두 번 만들 수 없다는 뜻이다.
  - `uq_shop_tags_shop_sort`
    - `(shop_id, sort_order)` 조합이 유일해야 한다.
    - 같은 매장 안에서 정렬 번호가 겹치면 안 된다는 뜻이다.
  - `@Index(name = "idx_shop_tags_shop_sort", columnList = "shop_id, sort_order")`
    - 매장별 정렬 조회가 자주 일어나므로 이 순서로 인덱스를 준 것이다.

- `@Getter`
  - 필드 읽기용 getter를 Lombok이 자동 생성한다.

- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
  - JPA가 엔티티를 만들 때 기본 생성자가 필요하다.
  - 외부에서 아무렇게나 `new ShopTag()` 하지 못하게 `PROTECTED`로 막았다.

- `private Long id;`
  - 엔티티의 PK다.
  - `@GeneratedValue(strategy = GenerationType.IDENTITY)`라서 DB auto increment를 사용한다.

- `private Long shopId;`
  - 태그의 소속 매장이다.
  - 이 필드가 생기면서 태그는 더 이상 전역 개념이 아니게 된다.

- `private String name;`
  - 태그 이름이다.
  - 길이 제한은 `100`.

- `private Integer sortOrder;`
  - 이 매장 안에서의 표시 순서다.
  - 이전 설계에서는 별도 `shop_tag_orders` 테이블에 둘 수도 있었지만, 지금은 태그 자체 속성으로 둔 상태다.

- `createdAt`, `updatedAt`
  - 생성/수정 이력 필드다.

- `public static ShopTag create(Long shopId, String name, Integer sortOrder)`
  - 엔티티 생성용 팩토리 메서드다.
  - 왜 생성자를 직접 쓰지 않냐면, 도메인 모델 생성 방식을 통일하고 JPA 기본 생성자 노출을 줄이기 위해서다.

- `public void updateSortOrder(Integer sortOrder)`
  - 정렬 변경 API에서 이 값을 바꿀 수 있게 열어둔 메서드다.
  - 현재 구현은 repository update query를 주로 쓰지만, 도메인 수준에서도 이 필드를 바꿀 수 있게 남겨둔 셈이다.

### 이 파일이 생긴 이유

이 클래스 하나로 아래 요구사항이 가능해졌다.

- 같은 이름 태그가 다른 매장에 각각 존재 가능
- 태그 정렬을 매장별로 독립 관리 가능
- hidden 태그를 매장 안에 보존 가능

---

## 2. `src/main/java/com/example/easybooking/shop/dto/request/CreateShopTagRequest.java`

### 파일 역할

매장 태그 생성 API용 요청 DTO다.

### 위에서 아래로 해설

- `@Getter`
  - 요청 바디 값을 읽기 위한 getter 생성

- `@NoArgsConstructor`
  - Jackson이 JSON -> 객체로 역직렬화할 때 기본 생성자가 필요하다.

- `@NotBlank`
  - `name`이 `null`, 빈 문자열, 공백만 있는 문자열이면 validation 실패하도록 한다.

- 생성자 `CreateShopTagRequest(String name)`
  - 테스트나 수동 생성 시 편하게 쓰기 위한 생성자다.

### 왜 기존 `CreateTagRequest`를 안 썼나

`CreateTagRequest`는 예전 전역 태그 API 의미를 담고 있다.  
새 모델은 "매장 로컬 태그 생성"이므로, 의미를 분리하기 위해 별도 DTO를 만들었다.

---

## 3. `src/main/java/com/example/easybooking/shop/dto/request/UpdateShopTagOrderRequest.java`

### 파일 역할

`PUT /api/shops/{shopId}/tags/order` 요청 DTO다.

### 위에서 아래로 해설

- `private List<Long> tagIds;`
  - 프론트가 현재 보이는 태그 전체를 순서대로 보내는 배열이다.

- `@NotEmpty`
  - 빈 배열은 허용하지 않는다.
  - 이유는 이 API가 "visible 태그 전체 overwrite" 계약이기 때문이다.

### 이 DTO가 표현하는 정책

이 DTO는 partial move가 아니라 **전체 visible 배열 overwrite** 정책을 전제로 한다.

즉:

- `[3, 1, 2]`를 보내면 visible 태그 전체 순서를 그렇게 덮어쓴다.
- 일부만 보내는 방식은 지원하지 않는다.

---

## 4. `src/main/java/com/example/easybooking/shop/dto/response/TagResponse.java`

### 파일 역할

태그 응답 DTO다.

### 위에서 아래로 해설

- `private final Long id;`
- `private final String name;`
  - 현재 응답에는 태그의 최소 정보만 담는다.

- `public TagResponse(Tag tag)`
  - 예전 전역 `Tag` 엔티티를 응답으로 바꾸는 생성자다.

- `public TagResponse(ShopTag tag)`
  - 새 매장 로컬 `ShopTag` 엔티티를 응답으로 바꾸는 생성자다.

### 이 파일이 중요한 이유

이 DTO가 두 생성자를 모두 갖고 있어서, 현재 시스템은 아직 아래 둘을 동시에 버티고 있다.

- old world: 전역 `Tag`
- new world: 매장 로컬 `ShopTag`

즉 이 DTO는 전환기의 브리지 역할도 한다.

---

## 5. `src/main/java/com/example/easybooking/errors/errorcode/ShopErrorCode.java`

### 파일 역할

`shop` 도메인 관련 비즈니스 에러 코드를 모아둔 enum이다.

### 이번 이슈에서 새로 의미가 커진 코드

- `SHOP_MENU_MISMATCH`
  - 요청 `shopId`와 `menuId`가 같은 매장 소속이 아닐 때 사용

- `SHOP_TAG_MISMATCH`
  - 요청 `shopId`와 `shopTagId`가 같은 매장 소속이 아닐 때 사용

- `INVALID_SHOP_TAG_ORDER`
  - 정렬 API에 중복/누락/다른 집합이 들어올 때 사용

- `SHOP_TAG_ALREADY_EXISTS`
  - 같은 매장에 같은 이름 태그를 다시 만들려 할 때 사용

### 이 enum이 중요한 이유

`#109`를 진행하면서 검증 로직이 늘어났고, 단순 `RuntimeException`이 아니라  
"어떤 계약을 어겼는지"를 API 응답에 싣기 위해 명시적 비즈니스 에러 코드가 필요해졌다.

---

## 6. `src/main/java/com/example/easybooking/shop/domain/ShopMenuTag.java`

### 파일 역할

메뉴와 태그의 연결 엔티티다.

### 이 파일이 특히 중요한 이유

이 엔티티는 지금 **전환기의 핵심 브리지**다.

예전에는:

- `shop_menu_id`
- `tag_id`

만 있었다.

지금은:

- `shop_menu_id`
- `tag_id`
- `shop_tag_id`

를 함께 가질 수 있다.

### 위에서 아래로 해설

- `@Table(name = "shop_menu_tags", uniqueConstraints = {...})`
  - 기존 유니크 제약 `uq_shop_menu_tags (shop_menu_id, tag_id)`는 유지
  - 새 유니크 제약 `uq_shop_menu_tags_shop_tag (shop_menu_id, shop_tag_id)` 추가

- `private Long shopMenuId;`
  - 어느 메뉴에 붙은 태그인지

- `private Long tagId;`
  - legacy 전역 `tags.id`
  - nullable로 바꾼 이유는 최종적으로 이 컬럼을 제거할 준비를 하기 위해서다.

- `private Long shopTagId;`
  - 새 `shop_tags.id`
  - 이게 앞으로의 주 식별자다.

- `create(Long shopMenuId, Long tagId)`
  - legacy row를 만들기 위한 팩토리

- `createResolved(Long shopMenuId, Long tagId, Long shopTagId)`
  - 브리지 row를 만들기 위한 팩토리
  - 현재 핵심 write path는 이 팩토리를 쓴다.

### 현재 상태 요약

이 엔티티는 아직 "완전한 최종형"이 아니다.  
지금은 old/new 둘을 같이 저장하는 과도기 구조다.

---

## 7. `src/main/java/com/example/easybooking/shop/repository/ShopMenuTagRepository.java`

### 파일 역할

`shop_menu_tags` 조회/삭제를 담당하는 repository다.

### 위에서 아래로 해설

- `findByShopMenuIdAndTagId(...)`
  - 예전 legacy 조회

- `findByShopMenuIdAndShopTagId(...)`
  - 새 `shopTagId` 기준 조회

- `findByShopMenuIdAndAnyTagId(...)`
  - 핵심 브리지 메서드다.
  - `(shopTagId == 요청값) OR (tagId == 요청값)`으로 찾는다.
  - old/new 어느 ID가 들어와도 같은 row를 찾게 하려는 의도다.

- `deleteByShopMenuIdAndAnyTagId(...)`
  - 삭제도 같은 논리로 브리지 처리한다.

### 왜 커스텀 JPQL이 필요한가

Spring Data 파생 메서드만으로는 "둘 중 하나에 매칭"을 깔끔하게 표현하기 어려워서 직접 JPQL을 썼다.

---

## 8. `src/main/java/com/example/easybooking/shop/repository/ShopMenuRepository.java`

### 파일 역할

메뉴 조회 repository다.

### 이번 이슈에서 바뀐 핵심

- `findByIdAndShopId(...)`
  - `shopId-menuId` 소속 검증용

- `findActiveByShopIdAndTagIds(...)`
  - 예전에는 `smt.tagId IN :tagIds`만 봤다.
  - 지금은 `(smt.shopTagId IN :tagIds OR smt.tagId IN :tagIds)`로 바뀌었다.

### 이 변경의 의미

메뉴 필터 API도 브리지 기간에는 old/new 둘을 다 받아줘야 한다는 뜻이다.

즉 지금은 `tagIds`라는 파라미터 이름은 그대로지만, 실제 의미는 과도기적으로 두 개다.

- legacy `tags.id`
- new `shop_tags.id`

최종 cleanup 때는 이 모호성이 없어져야 한다.

---

## 9. `src/main/java/com/example/easybooking/shop/repository/ShopTagRepository.java`

### 파일 역할

매장 로컬 태그 조회/수정 repository다.

### 위에서 아래로 해설

- `findByShopIdAndName(...)`
  - 같은 매장 안의 이름 중복 검사에 사용

- `findByShopIdOrderBySortOrderAsc(...)`
  - 매장 전체 태그를 정렬 순서대로 읽음
  - hidden 포함 전체를 읽을 때 쓴다.

- `findMaxSortOrderByShopId(...)`
  - 새 태그 생성 시 맨 뒤 번호를 계산하기 위해 사용
  - `coalesce(max(...), -1)`라서 태그가 하나도 없으면 `-1 + 1 = 0`으로 시작한다.

- `findVisibleByShopTagIdOrderBySortOrderAsc(...)`
  - 새 구조 기준의 정상 조회 쿼리다.
  - `shop_menu_tags.shop_tag_id`가 채워진 row만 대상으로 한다.
  - `join ShopMenuTag smt on smt.shopTagId = st.id`
    - 현재 `ShopTag`와 실제 연결 row를 직접 `id`로 이어준다.
  - `join ShopMenu m on m.id = smt.shopMenuId`
    - 연결된 메뉴를 붙인다.
  - `where st.shopId = :shopId and m.isActive = true`
    - 같은 매장, 활성 메뉴만 visible로 본다.
  - `distinct`
    - 같은 태그가 여러 메뉴에 붙어도 한 번만 나온다.

- `findVisibleByLegacyTagFallbackOrderBySortOrderAsc(...)`
  - 브리지 기간에만 필요한 fallback 조회 쿼리다.
  - 아직 `smt.shopTagId`가 비어 있는 row만 대상으로 한다.
  - `join Tag t on t.id = smt.tagId`
    - legacy global tag 이름을 읽어온다.
  - `smt.shopTagId is null and t.name = st.name`
    - direct 연결이 없을 때만 이름으로 임시 매칭한다.
  - 즉 이 메서드는 cleanup 때 제거 후보다.

- `shiftSortOrders(...)`
  - 정렬 업데이트 전에 모든 순서를 임시로 뒤로 밀어 unique 충돌을 피한다.

- `updateSortOrder(...)`
  - 특정 태그 하나의 `sort_order`를 최종값으로 쓴다.

### 이 repository가 표현하는 현재 상태

이 repository는 visible 조회를 두 경로로 나눠 갖고 있다.

- 정상 경로
- fallback 경로

즉 여전히 "최종형 + 브리지 fallback"을 함께 들고 있지만,
한 거대한 JPQL 하나가 아니라 의미별 메서드 두 개로 분리된 상태다.

---

## 10. `src/main/java/com/example/easybooking/shop/service/TagService.java`

### 파일 역할

이번 이슈의 핵심 유스케이스를 거의 다 들고 있는 서비스다.

이 파일을 이해하면 현재 구현 상태를 거의 다 이해한 셈이다.

### 클래스 레벨

- `@Service`
  - Spring 서비스 빈

- `@Slf4j`
  - cleanup 준비 단계에서 legacy 경로 사용 로그를 남기기 위해 추가했다.

- `@Transactional(readOnly = true)`
  - 기본은 조회 트랜잭션
  - 쓰기 메서드만 개별적으로 `@Transactional`

### 필드 해설

- `TagRepository tagRepository`
  - legacy global tag용
  - 전환이 끝나면 제거 대상

- `ShopReader shopReader`
  - owner 검증

- `ShopMenuReader shopMenuReader`
  - `shopId-menuId` 소속 검증

- `ShopTagRepository shopTagRepository`
  - 새 모델의 주 repository

- `ShopMenuTagRepository shopMenuTagRepository`
  - 메뉴-태그 연결 브리지

### 메서드별 해설

#### `createOrGet(String name)`

- legacy global tag 생성/재사용 메서드
- `@Deprecated`
- 호출 시 `log.warn(...)`
- 남겨둔 이유:
  - old `/api/tags`를 아직 완전히 제거하지 않았기 때문

#### `createShopTag(Long shopId, String name)`

- 새 매장 로컬 태그 생성 메서드
- 먼저 `findByShopIdAndName`으로 중복 확인
- 있으면 `SHOP_TAG_ALREADY_EXISTS`
- 없으면 `findMaxSortOrderByShopId + 1`
- 새 `ShopTag` 저장

이 메서드로 인해:

- 교차 매장 동일 이름 허용
- 동일 매장 동일 이름 금지

가 코드로 확정된다.

#### `createShopTag(Long shopId, Long ownerUserId, String name)`

- owner 검증이 필요한 API용 오버로드
- 먼저 `validateOwner`
- 그 다음 실제 생성 메서드 호출

#### `getAllTags()`

- legacy global tag 목록
- `@Deprecated`
- 호출 시 warn 로그 남김

#### `getVisibleShopTags(Long shopId)`

- 사용자용 visible 태그 목록
- 내부적으로 `readVisibleShopTags(shopId)`를 호출한다.
- `TagResponse`로 변환만 한다.

#### `readVisibleShopTags(Long shopId)`

- visible 조회를 읽기 쉬운 형태로 분해하기 위해 추가한 helper다.
- 하는 일은 아래 네 단계다.

1. `findVisibleByShopTagIdOrderBySortOrderAsc(...)` 호출
2. `findVisibleByLegacyTagFallbackOrderBySortOrderAsc(...)` 호출
3. 두 결과를 `ShopTag.id` 기준으로 dedup
4. `sortOrder`, `id` 기준으로 다시 정렬

- `Stream.concat(...)`
  - direct 경로 결과와 fallback 경로 결과를 하나의 stream으로 합친다.

- `Collectors.toMap(ShopTag::getId, Function.identity(), (left, right) -> left)`
  - 같은 `ShopTag`가 두 경로에서 동시에 잡혀도 하나만 남긴다.
  - merge 함수 `(left, right) -> left`는 먼저 들어온 값을 유지하겠다는 뜻이다.

- `sorted(Comparator.comparing(ShopTag::getSortOrder).thenComparing(ShopTag::getId))`
  - 최종 응답 순서를 안정적으로 맞춘다.
  - `id`는 tie-breaker 역할이다.

이 helper가 중요한 이유는,
전환기 브리지 로직을 repository의 큰 쿼리 하나에 숨기지 않고
service 레벨에서 "정상 경로 + fallback 경로를 합친다"는 의도를 직접 드러내기 때문이다.

#### `updateShopTagOrder(...)`

- 운영자 정렬 API의 핵심 메서드

동작 순서:

1. owner 검증
2. 현재 visible 태그 목록 조회
   - 여기서도 `readVisibleShopTags(shopId)`를 사용한다.
3. 요청 배열 검증
4. 전체 태그 목록 조회
5. canonical order 구성

```text
[요청 visible] + [hidden 기존 순서]
```

6. `shiftSortOrders(shopId, allTags.size())`
  - 일단 전부 뒤로 민다.
7. `updateSortOrder(...)`를 반복 호출
  - 최종 순서 `0..N-1`로 쓴다.

왜 2단계 갱신이 필요한가:

- `(shop_id, sort_order)` 유니크 충돌 방지

#### `addTagToMenu(shopId, menuId, tagId)`

- 메뉴에 태그 연결

동작 순서:

1. `readMenuInShop`으로 메뉴 소속 검증
2. `resolveTagIds(shopId, tagId)`로 입력 ID 해석
3. 이미 같은 `shopTagId` 연결이 있으면 중복 저장 안 함
4. `createResolved(menuId, legacyTagId, shopTagId)`로 브리지 row 저장

이 메서드가 현재 fallback의 핵심이다.

#### `removeTagFromMenu(shopId, menuId, tagId)`

- 삭제도 소속 검증 후
- 요청이 `shopTagId`인 경우 같은 매장 태그인지 추가 검증
- 최종 삭제는 `deleteByShopMenuIdAndAnyTagId`

즉 old/new 어느 ID가 와도 삭제가 가능하다.

#### `validateOwner(...)`

- `ShopReader.isShopOwnedBy(...)`를 써서 점주 검증
- 실패 시 `SHOP_OWNER_MISMATCH`

#### `validateRequestedOrder(...)`

- 정렬 요청 검증 로직

검사 항목:

- `null`/빈 배열 금지
- 중복 금지
- visible 전체 길이와 일치해야 함
- visible 집합이 정확히 같아야 함

여기서 partial move를 막는다.

#### `readMenuInShop(...)`

- `shopMenuReader.getByIdAndShopId(...)` 호출
- 실패를 `SHOP_MENU_MISMATCH`로 변환

#### `resolveTagIds(...)`

- 입력 `tagId`가 실제로는 `shopTagId`일 수도 있고 legacy `tagId`일 수도 있으므로 해석하는 메서드

순서:

1. `shopTagRepository.findById(...)`
2. 있으면 `resolveShopTagIds(...)`
3. 없으면 `resolveLegacyTagIds(...)`

#### `resolveShopTagIds(...)`

- 입력이 새 `shopTagId`였을 때의 경로
- 같은 매장 소속인지 검증
- 같은 이름의 legacy `Tag`가 없으면 생성
- 결과로 `(legacyTagId, shopTagId)` 반환

#### `resolveLegacyTagIds(...)`

- 입력이 old `tag_id`였을 때의 fallback 경로
- warn 로그 남김
- legacy `Tag` 읽음
- 같은 이름의 `ShopTag`가 현재 매장에 없으면 자동 생성
- 결과로 `(legacyTagId, shopTagId)` 반환

이 메서드는 cleanup 때 가장 먼저 제거 후보가 될 가능성이 높다.

#### `private record ResolvedTagIds(...)`

- 반환값 묶음
- old/new 식별자를 함께 들고 다니기 위한 작은 구조체

---

## 11. `src/main/java/com/example/easybooking/shop/presentation/TagController.java`

### 파일 역할

태그 관련 HTTP API 진입점이다.

### 위에서 아래로 해설

- `createTag(...)`
  - `POST /api/tags`
  - legacy 전역 태그 생성
  - `@Deprecated`

- `createShopTag(...)`
  - `POST /api/shops/{shopId}/tags`
  - 새 운영자용 매장 태그 생성 API
  - `@RequireAuthenticatedUser`
  - owner 검증은 service에서 수행

- `getAllTags(...)`
  - `GET /api/tags`
  - legacy 전역 태그 목록
  - `@Deprecated`

- `getVisibleShopTags(...)`
  - `GET /api/shops/{shopId}/tags`
  - 사용자용 visible 태그 조회

- `updateShopTagOrder(...)`
  - `PUT /api/shops/{shopId}/tags/order`
  - 운영자용 정렬 변경 API

- `addTagToMenu(...)`
  - `POST /api/shops/{shopId}/menus/{menuId}/tags`
  - 내부적으로는 old/new ID 둘 다 해석 가능

- `removeTagFromMenu(...)`
  - `DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}`
  - 삭제도 old/new ID 브리지 상태

### 이 컨트롤러의 현재 상태

한 파일 안에 new API와 deprecated old API가 같이 있다.  
즉 이 컨트롤러 자체가 전환기의 현황을 보여준다.

---

## 12. `src/main/resources/db/migration/V6__shop_tags.sql`

### 파일 역할

운영용 `shop_tags` 테이블 생성 마이그레이션

### 줄별 의미

- `CREATE TABLE shop_tags (...)`
  - 새 매장 로컬 태그 테이블 생성

- `id BIGINT NOT NULL AUTO_INCREMENT`
  - PK

- `shop_id BIGINT NOT NULL`
  - 소속 매장

- `name VARCHAR(100) NOT NULL`
  - 태그 이름

- `sort_order INT NOT NULL`
  - 매장 내부 정렬

- `created_at`, `updated_at`
  - 생성/수정 시간

- `UNIQUE KEY uq_shop_tags_shop_name (shop_id, name)`
  - 동일 매장 중복 이름 금지

- `UNIQUE KEY uq_shop_tags_shop_sort (shop_id, sort_order)`
  - 동일 매장 정렬 번호 중복 금지

- `INDEX idx_shop_tags_shop_sort (shop_id, sort_order)`
  - 매장별 정렬 조회 최적화

---

## 13. `src/main/resources/db/migration/V7__shop_menu_tags_add_shop_tag_id.sql`

### 파일 역할

기존 연결 테이블에 `shop_tag_id`를 추가하는 운영용 마이그레이션

### 줄별 의미

- `ALTER TABLE shop_menu_tags ADD COLUMN shop_tag_id BIGINT NULL;`
  - 일단 nullable로 추가
  - 이유:
    - 기존 row가 이미 있으므로 바로 NOT NULL로 만들 수 없음
    - backfill 후 cleanup 단계에서 NOT NULL로 올릴 수 있음

- `ADD CONSTRAINT uq_shop_menu_tags_shop_tag UNIQUE (shop_menu_id, shop_tag_id);`
  - 같은 메뉴에 같은 `shopTagId`를 두 번 연결 못 하게 함

- `CREATE INDEX idx_shop_menu_tags_shop_tag ...`
  - `shop_tag_id` 기준 조회 최적화

- `ADD CONSTRAINT fk_smt_shop_tag FOREIGN KEY (shop_tag_id) REFERENCES shop_tags(id);`
  - 새 FK

### 이 SQL의 의미

기존 구조를 파괴하지 않고 새 구조를 얹는 **ADD 단계**다.

---

## 14. `src/main/resources/db/migration/V8__backfill_shop_tags.sql`

### 파일 역할

운영 데이터 backfill용 SQL

### 블록 1: `shop_tags` 생성

```sql
INSERT INTO shop_tags (shop_id, name, sort_order)
SELECT ...
```

이 블록은 아직 없는 `(shop_id, name)` 조합의 `shop_tags`를 만든다.

#### 내부 `missing` 서브쿼리

- `shop_menu_tags smt`
- `shop_menus sm`
- `tags t`
- `LEFT JOIN shop_tags st`

를 통해:

- 현재 연결은 있는데
- 아직 `shop_tags`에는 없는

조합만 뽑아낸다.

#### `MIN(t.id) AS min_tag_id`

- 같은 `(shop_id, name)` 안에서 가장 작은 legacy `tag.id`를 구한다.
- 초기 정렬의 기준값으로 쓰기 위한 장치다.

#### `existing` 서브쿼리

- 이미 존재하는 `shop_tags`의 `MAX(sort_order)`를 구한다.

#### 최종 `sort_order` 계산

```sql
COALESCE(existing.max_sort_order, -1)
    + ROW_NUMBER() OVER (PARTITION BY missing.shop_id ORDER BY missing.min_tag_id)
```

의미:

- 기존 태그가 하나도 없으면 `-1 + 1 = 0`부터 시작
- 기존 태그가 이미 있으면 그 뒤에 이어 붙임
- 매장별로 따로 계산

### 블록 2: `shop_menu_tags.shop_tag_id` 채우기

```sql
UPDATE shop_menu_tags smt
JOIN shop_menus sm ...
JOIN tags t ...
JOIN shop_tags st ...
SET smt.shop_tag_id = st.id
WHERE smt.shop_tag_id IS NULL;
```

의미:

- 메뉴의 매장과
- legacy tag 이름을 이용해
- 대응되는 `shop_tags.id`를 찾아
- `shop_tag_id`에 채운다.

### 이 SQL의 핵심 의도

새 테이블만 만드는 게 아니라, 기존 데이터를 새 모델로 실제 이행하는 단계다.

---

## 15. `src/test/resources/db/migration-test/V6__shop_tags.sql`

### 파일 역할

H2 테스트에서 `shop_tags`와 backfill 검증에 필요한 최소 테이블을 준비하는 SQL

### 왜 운영용과 다르나

테스트 환경은 H2이고, 운영은 MySQL에 더 가깝다.  
그래서 migration-test 쪽은 "테스트가 돌 수 있게 최소 구조를 재현"하는 데 초점을 둔다.

### 위에서 아래로 해설

- `CREATE TABLE IF NOT EXISTS tags`
  - legacy `tags` 최소 구조

- `CREATE TABLE IF NOT EXISTS shop_menus`
  - backfill에 필요한 최소 메뉴 테이블

- `CREATE TABLE IF NOT EXISTS shop_menu_tags`
  - backfill 전 legacy 연결 테이블

- `CREATE TABLE shop_tags`
  - 새 매장 로컬 태그 테이블

- 마지막 인덱스
  - 정렬 조회 대비

### 이 파일이 중요한 이유

운영용 migration만으로는 테스트에서 backfill 시나리오를 쉽게 재현하기 어려워서,  
테스트 전용 최소 스키마를 따로 만든 것이다.

---

## 16. `src/test/resources/db/migration-test/V7__shop_menu_tags_add_shop_tag_id.sql`

### 파일 역할

테스트용 `shop_tag_id` 추가 마이그레이션

### 줄별 의미

- `ADD COLUMN shop_tag_id BIGINT;`
  - 테스트 환경에서도 새 컬럼 추가

- `ADD CONSTRAINT uq_shop_menu_tags_shop_tag UNIQUE (...)`
  - 동일 제약 재현

- `CREATE INDEX idx_shop_menu_tags_shop_tag ...`
  - 최소 인덱스 재현

### 왜 FK가 없나

테스트 쪽은 최소 검증을 위한 구조라서 운영용과 완전히 동일하지 않을 수 있다.  
핵심은 backfill과 조회/저장 동작에 필요한 제약을 재현하는 것이다.

---

## 17. `src/test/resources/db/migration-test/V8__backfill_shop_tags.sql`

### 파일 역할

테스트용 seed + backfill SQL

### 위에서 아래로 해설

- `INSERT INTO tags (id, name) ...`
  - legacy 전역 태그 두 개 준비
  - `손관리`, `발관리`

- `INSERT INTO shop_menus ...`
  - 매장 10, 20의 메뉴 seed

- `INSERT INTO shop_menu_tags ...`
  - legacy 연결 데이터 seed

- 이후 `INSERT INTO shop_tags ... SELECT ...`
  - 운영용 backfill 로직을 H2에서도 재현

- 마지막 `UPDATE shop_menu_tags ...`
  - `shop_tag_id`를 실제로 채움

### 이 SQL의 목적

테스트 시나리오를 self-contained하게 만드는 것이다.  
즉 migration-test만 돌려도:

- legacy 데이터가 생기고
- backfill이 돌고
- 결과를 바로 검증할 수 있다.

---

## 18. `src/test/java/com/example/easybooking/shop/service/TagServiceTest.java`

### 파일 역할

서비스 단위에서 `#109`의 핵심 규칙을 검증하는 테스트

### 클래스 구조 해설

- `@DataJpaTest`
  - JPA 중심 테스트

- `shopReader = mock(ShopReader.class);`
  - owner 검증은 DB까지 타지 않고 mock으로 제어

- `shopMenuReader = new ShopMenuReader(shopMenuRepository);`
  - 메뉴 소속 검증은 실제 repository 사용

### 각 테스트 설명

#### `createOrGet_createsNewTag`

- legacy global tag 생성이 아직 동작하는지 확인

#### `createOrGet_returnsExistingTag_whenDuplicate`

- legacy global tag는 여전히 이름 중복 시 재사용하는지 확인

#### `getAllTags_returnsAllTags`

- legacy global tag list 확인

#### `getAllTags_returnsEmptyList_whenNoTags`

- legacy list 빈 케이스

#### `addTagToMenu_createsLink`

- old `tag_id`로 메뉴 연결 요청이 들어와도
- 내부적으로 연결 row가 생기고
- 대응 `ShopTag`도 자동 생성되는지 확인

#### `createShopTag_createsShopLocalTag_andAllowsSameNameAcrossDifferentShops`

- 서로 다른 매장에서는 같은 이름 허용

#### `createShopTag_throwsException_whenSameNameAlreadyExistsInSameShop`

- 같은 매장 안에서는 같은 이름 금지
- 현재 정책이 `409` 방향임을 서비스 수준에서 고정

#### `getVisibleShopTags_returnsDistinctActiveTagsInStoredOrder`

- 활성 메뉴 연결만 반영
- 중복 제거
- 저장 순서 반영

#### `removeTagFromMenu_deletesLink`

- old/new 어느 쪽이든 연결 삭제 가능하다는 브리지 삭제 경로 확인

#### `addTagToMenu_supportsShopTagIdAndBackfillsLegacyTagId`

- 새 `shopTagId`가 들어왔을 때도
- legacy `tagId`를 내부적으로 함께 채우는지 확인

#### `updateShopTagOrder_movesVisibleTagsFirstAndKeepsHiddenRelativeOrder`

- visible 재정렬
- hidden은 뒤에서 기존 상대 순서 유지

#### `updateShopTagOrder_throwsException_whenVisibleTagIdsAreMissing`

- partial input 금지

### 이 테스트 파일이 보여주는 것

현재 시스템은 완전 새 모델만 테스트하는 게 아니라,
legacy 경로와 브리지 경로를 함께 테스트하고 있다.

---

## 19. `src/test/java/com/example/easybooking/shop/presentation/TagControllerIntegrationTest.java`

### 파일 역할

HTTP 레벨에서 태그 API 계약을 검증한다.

### 테스트 구조 해설

- `@SpringBootTest`
  - 실제 Spring 컨텍스트 사용

- `@AutoConfigureMockMvc(addFilters = false)`
  - 보안 필터는 끄고, 테스트가 직접 SecurityContext를 세팅한다.

- `authenticate(...)`
  - `AuthenticatedUser`를 SecurityContext에 넣는 헬퍼

### 각 테스트 설명

#### `createShopTag_createsTagForOwnedShop`

- 점주가 자기 매장에 태그 생성하면 `201 Created`
- DB에 실제 저장됐는지 확인

#### `createShopTag_returnsForbidden_whenUserIsNotShopOwner`

- owner 검증 실패 시 `403 + SHOP_OWNER_MISMATCH`

#### `createShopTag_returnsConflict_whenTagNameAlreadyExistsInSameShop`

- 같은 매장 중복 이름 생성 시 `409 + SHOP_TAG_ALREADY_EXISTS`

#### `getVisibleShopTags_returnsOnlyActiveDistinctTagsInStoredOrder`

- 사용자용 조회 계약
- 활성 메뉴만
- 중복 제거
- 저장 순서 반영

#### `updateShopTagOrder_reordersVisibleTagsAndKeepsHiddenAfterThem`

- 운영자 정렬 API 성공 계약
- hidden이 뒤에 남는지까지 DB 상태로 확인

#### `updateShopTagOrder_returnsBadRequest_whenVisibleTagsAreMissing`

- visible 일부만 보내면 `400 + INVALID_SHOP_TAG_ORDER`

### 이 테스트가 중요한 이유

서비스 테스트가 규칙을 확인한다면,  
이 파일은 그 규칙이 실제 HTTP 응답 코드와 body에 어떻게 노출되는지 확인한다.

---

## 20. `src/test/java/com/example/easybooking/shop/domain/ShopMenuTagJpaMappingTest.java`

### 파일 역할

`shop_menu_tags` 엔티티 자체의 JPA 매핑/제약을 확인한다.

### 각 테스트 설명

#### `canPersistAndLoadShopMenuTag`

- `createResolved(...)`로 저장한 row가
- `tagId`, `shopTagId` 둘 다 제대로 매핑되는지 확인

#### `save_throwsException_whenDuplicateMenuAndTag`

- legacy 유니크 제약 유지 확인

#### `save_throwsException_whenDuplicateMenuAndShopTag`

- 새 유니크 제약 확인

#### `save_allowsSameMenuDifferentTag`

- 같은 메뉴에 서로 다른 legacy tag는 가능

#### `save_allowsSameTagDifferentMenu`

- 같은 태그를 다른 메뉴에 재사용 가능

### 이 테스트의 의미

브리지 구조가 들어가면서 제약이 하나 더 늘었기 때문에,  
엔티티 매핑 수준에서도 둘 다 기대대로 동작하는지 확인해야 했다.

---

## 21. `src/test/java/com/example/easybooking/shop/ShopTagMigrationTablesTest.java`

### 파일 역할

test migration 기준으로 `shop_tags` 테이블이 실제로 생성되는지 확인

### 핵심 포인트

- `spring.flyway.enabled=true`
- `spring.flyway.locations=classpath:db/migration-test`

즉 테스트용 migration만 돌린 뒤
`INFORMATION_SCHEMA.TABLES`를 조회해서 `SHOP_TAGS`가 있는지 확인한다.

이건 아주 작은 테스트지만,
"migration-test 파일 구성이 아예 틀어지지 않았는지"를 보는 안전장치 역할을 한다.

---

## 22. `src/test/java/com/example/easybooking/shop/ShopTagBackfillMigrationTest.java`

### 파일 역할

backfill migration 결과를 직접 검증하는 테스트

### 테스트 내용

- 매장 10의 `shop_tags` 순서가 `손관리`, `발관리`인지
- 매장 20에도 `손관리`가 별도 row로 생겼는지
- `shop_menu_tags.shop_tag_id IS NULL`가 0인지

### 이 테스트의 의미

이 테스트는 단순 테이블 생성이 아니라,
"기존 전역 태그 데이터를 실제로 매장 로컬 태그 구조로 잘 옮겼는가"를 본다.

즉 migration 쪽에서 가장 중요한 검증 중 하나다.

---

## 23. 현재 코드의 최종형 / 과도기형 구분

### 최종형에 가까운 코드

- `ShopTag`
- `createShopTag`
- `getVisibleShopTags`
- `updateShopTagOrder`
- `shopTagId` 기반 연결

### 과도기 브리지 코드

- `TagResponse(Tag tag)`
- `TagService.createOrGet`
- `TagService.getAllTags`
- `resolveLegacyTagIds`
- `ShopMenuRepository.findActiveByShopIdAndTagIds`의 `OR smt.tagId`
- `ShopTagRepository.findVisibleByLegacyTagFallbackOrderBySortOrderAsc(...)`
- `ShopMenuTag.tagId`
- `/api/tags`

이 구분을 알고 있어야 cleanup 때 무엇을 지우고, 무엇을 남길지 판단할 수 있다.

---

## 24. 지금 문서를 어떻게 써먹으면 좋은가

### 1. 코드 리뷰용

파일별로 "이게 왜 생겼지?"를 역추적할 때 사용

### 2. cleanup 준비용

legacy / fallback 코드를 골라낼 때 사용

### 3. 프론트 협업용

프론트가 봤을 때:

- 지금 어떤 API가 새 경로인지
- 어떤 API가 deprecated인지
- `tagId`가 과도기적으로 두 의미를 갖는 구간이 어딘지

를 설명할 때 사용

---

## 25. 마지막 요약

`#109`의 실제 코드는 크게 네 층으로 나뉜다.

- 새 모델: `shop_tags`
- 브리지 연결: `shop_menu_tags.tag_id + shop_tag_id`
- 운영자/사용자 API: 생성, 조회, 정렬
- cleanup 준비: deprecated + warn 로그 + backfill SQL

즉 지금 상태는 "매장 로컬 태그 모델은 이미 도입됐고,  
legacy 전역 태그 구조는 관측 가능한 fallback 상태로 남겨둔 전환기 구현"이라고 이해하면 된다.
