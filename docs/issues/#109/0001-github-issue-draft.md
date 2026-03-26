# `#109` 매장 로컬 태그 모델 전환 + 표시 순서 도입

작성일: `2026-03-22`

## Summary

기존 태그 모델은 사실상 전역 `Tag`를 전제로 한다.  
하지만 실제 제품 요구사항은 태그가 전역 사전이 아니라 **특정 매장 안에서만 의미를 가지는 로컬 분류값**이라는 점에 가깝다.

즉:

- `손관리`라는 이름의 태그가 여러 매장에서 동시에 존재할 수 있어야 한다.
- 한 매장의 태그 수정/정렬/삭제가 다른 매장에 영향을 주면 안 된다.
- 메뉴-태그 연결도 전역 `tagId`가 아니라 **해당 매장의 태그 식별자**를 기준으로 관리되어야 한다.

이번 이슈의 목적은 단순히 태그 표시 순서를 추가하는 것이 아니라,
**전역 태그 모델을 매장 로컬 태그 모델로 전환한 뒤 그 위에 표시 순서를 얹는 것**이다.

---

## 배경

현재 기준 브랜치에서 확인되는 상태는 다음과 같다.

- 전역 태그 API가 있다.
  - `POST /api/tags`
  - `GET /api/tags`
- 메뉴-태그 연결/해제 API가 있다.
  - `POST /api/shops/{shopId}/menus/{menuId}/tags`
  - `DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}`
- 메뉴 조회 API는 태그 필터를 지원한다.
  - `GET /api/shops/{shopId}/menus?tagIds=...`

하지만 현재 구조에는 아래 문제가 있다.

- 태그의 정체성이 전역이라 매장 소속성이 모델에 없다.
- 동일한 태그명이 매장마다 독립적으로 존재할 수 없다.
- 표시 순서를 매장 단위로 저장하려면 별도 보조 모델이 필요해진다.
- 메뉴 연결 API가 실제로는 "전역 태그를 매장 메뉴에 연결"하는 형태라 도메인 의미가 어긋난다.

---

## Goals

- 태그를 전역 개념이 아니라 **매장 소속 리소스**로 전환한다.
- 동일 이름 태그가 다른 매장에서 각각 독립적으로 존재할 수 있게 한다.
- 사용자용 `GET /api/shops/{shopId}/tags`는 활성 메뉴에 연결된 매장 태그만 반환한다.
- 태그 표시 순서는 매장 태그 자체의 속성으로 관리한다.
- 운영자용 태그 생성/정렬 변경/메뉴 연결 API가 모두 같은 매장 범위 안에서 동작하게 한다.

## Non-goals

- 프론트 drag-and-drop 상세 UI 설계
- 전역 인증/인가 구조 개편
- 메뉴 자체 정렬 정책 변경
- 태그 추천/자동완성용 전역 taxonomy 구축

---

## 요구사항

### 1. 태그는 매장 로컬이어야 한다

- 태그는 `shop_id` 소속을 가진다.
- 동일한 이름의 태그가 서로 다른 매장에서 공존할 수 있다.
- 같은 매장 안에서는 태그명이 중복되면 안 된다.

### 2. 사용자용 태그 조회

- 엔드포인트: `GET /api/shops/{shopId}/tags`
- 의미:
  - `shopId`의 활성 메뉴에 연결된 태그만 반환
  - 중복 없이 반환
  - 매장 내부 `sort_order ASC`로 반환
  - hidden 태그는 노출하지 않음

### 3. 운영자용 태그 생성

- 엔드포인트: `POST /api/shops/{shopId}/tags`
- 의미:
  - 해당 매장 전용 태그를 생성
  - 최초 생성 시 맨 뒤 순서로 append

### 4. 메뉴-태그 연결

- 엔드포인트: `POST /api/shops/{shopId}/menus/{menuId}/tags`
- 의미:
  - 요청의 `tagId`는 전역 태그가 아니라 **해당 매장 태그 ID**여야 한다.
  - 다른 매장 태그를 연결하려 하면 실패해야 한다.

### 5. 운영자용 태그 순서 변경

- 엔드포인트: `PUT /api/shops/{shopId}/tags/order`
- 요청 예시:

```json
{
  "tagIds": [4, 1, 3]
}
```

- 의미:
  - 현재 visible 태그 전체 순서를 한 번에 덮어쓴다.
  - hidden 태그는 기존 상대 순서를 유지한 채 뒤로 보낸다.

---

## 확정 정책

### hidden 태그 정책

hidden 태그는 아래 둘을 포함한다.

- 비활성 메뉴에만 연결된 태그
- 현재 어떤 메뉴에도 연결되지 않았지만 매장에 남아 있는 태그

정책:

- hidden 태그 row는 삭제하지 않는다.
- 사용자 응답에는 포함하지 않는다.
- 재정렬 시 visible 뒤에 유지한다.

### `sort_order` 규칙

- `sort_order`는 매장 내부에서만 의미가 있다.
- 항상 `0..N-1` 연속 정수로 유지한다.

---

## 수용 기준

- AC-1: 태그 생성은 매장 소속으로 저장된다.
- AC-2: 같은 이름 태그가 서로 다른 매장에서 각각 생성될 수 있다.
- AC-3: 같은 매장 안에서는 동일 이름 태그를 중복 생성할 수 없다.
- AC-4: `GET /api/shops/{shopId}/tags`는 활성 메뉴에 연결된 태그만 반환한다.
- AC-5: 같은 태그가 여러 메뉴에 연결돼도 한 번만 반환한다.
- AC-6: 응답은 매장 내부 `sort_order ASC`를 따른다.
- AC-7: `PUT /api/shops/{shopId}/tags/order`는 visible 태그 전체를 중복/누락 없이 받아야 한다.
- AC-8: 정렬 변경 후 hidden 태그는 기존 상대 순서를 유지한 채 뒤로 이동한다.
- AC-9: 메뉴 연결 API는 `shopId`/`menuId`/`tagId`의 매장 소속이 모두 일치해야 한다.
- AC-10: 운영자용 API는 점주 소유 검증을 통과해야 한다.

---

## 구현 범위

### 새로 추가될 가능성이 큰 파일

- `src/main/java/com/example/easybooking/shop/domain/ShopTag.java`
- `src/main/java/com/example/easybooking/shop/repository/ShopTagRepository.java`
- `src/main/java/com/example/easybooking/shop/dto/request/CreateShopTagRequest.java`
- `src/main/java/com/example/easybooking/shop/dto/request/UpdateShopTagOrderRequest.java`
- `src/test/java/com/example/easybooking/shop/presentation/TagControllerIntegrationTest.java`
- `src/main/resources/db/migration/V6__shop_tags.sql`

### 수정 대상 파일

- `src/main/java/com/example/easybooking/shop/domain/ShopMenuTag.java`
- `src/main/java/com/example/easybooking/shop/presentation/TagController.java`
- `src/main/java/com/example/easybooking/shop/service/TagService.java`
- `src/main/java/com/example/easybooking/shop/repository/ShopMenuRepository.java`
- `src/main/java/com/example/easybooking/errors/errorcode/ShopErrorCode.java`
- `src/test/java/com/example/easybooking/shop/service/TagServiceTest.java`

---

## Test Plan

- `@DataJpaTest`
  - 매장 로컬 태그 생성
  - 매장별 이름 중복/교차 매장 동일 이름 허용
  - visible 태그 조회 정렬
  - hidden 태그 재정렬 유지
- `@SpringBootTest + MockMvc`
  - `POST /api/shops/{shopId}/tags`
  - `GET /api/shops/{shopId}/tags`
  - `PUT /api/shops/{shopId}/tags/order`
  - `SHOP_OWNER_MISMATCH`
  - `shopId-menuId-tagId` 소속 불일치 실패

---

## 한 줄 결론

- `#109`는 `shop_tag_orders`를 얹는 수준의 변경이 아니라, **태그를 매장 로컬 리소스로 재정의하고 그 위에 표시 순서를 올리는 구조 전환 작업**이다.
