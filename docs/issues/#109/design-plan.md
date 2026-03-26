# `#109` 매장 로컬 태그 모델 전환 설계 계획

작성일: `2026-03-22`

## 문서 목적

이 문서는 `#109`의 전제를 다시 고정한다.

기존 문서는 "전역 `Tag`는 유지하고 매장별 순서만 따로 저장한다"는 방향을 전제로 했지만,
새 요구사항은 그보다 더 근본적이다.

- `Tag`는 전역적인 속성을 가지면 안 된다.
- 태그는 오직 특정 매장 안에서만 의미를 가진다.

따라서 이번 문서의 목표는 아래 두 가지다.

1. 전역 태그 모델을 폐기하고 매장 로컬 태그 모델로 전환하는 설계를 확정한다.
2. 그 위에 사용자 조회 / 메뉴 연결 / 정렬 변경 흐름을 다시 정의한다.

---

## 현재 구조와 새 전제의 충돌

현재 기준 브랜치의 태그 구조는 아래 전제를 갖는다.

- `tags`는 전역 테이블이다.
- `shop_menu_tags`는 메뉴와 전역 태그를 연결한다.
- `POST /api/tags`, `GET /api/tags`도 전역 API다.

하지만 새 요구사항은 아래를 뜻한다.

- 태그 식별자는 전역이 아니라 매장 스코프여야 한다.
- 같은 이름 태그가 다른 매장에서 동시에 존재 가능해야 한다.
- 메뉴-태그 연결도 같은 매장 소속 리소리끼리만 허용돼야 한다.

즉, 기존 `global tag + shop order` 접근은 요구사항과 충돌한다.

참고:

- 현재 워크트리에 임시로 추가된 `shop_tag_orders` 스캐폴딩은 이전 전제 기반 탐색 결과다.
- 본 문서 기준 채택안과는 맞지 않으므로 후속 구현 전에 정리 대상이다.

---

## 설계안 비교

## A안. 전역 `tags` 유지 + `shop_tag_orders`만 추가

### 개요

- 기존 전역 `tags`는 그대로 두고
- 매장별 정렬만 `shop_tag_orders`로 관리

### 장점

- 현재 코드와 가장 가깝다.
- 조회/정렬만 보면 구현이 빠르다.

### 단점

- 핵심 요구사항인 "태그는 매장 로컬"을 만족하지 못한다.
- 태그의 생성/수정/삭제 의미가 여전히 전역이다.
- 다른 매장과 식별자를 공유하는 모델이 유지된다.

### 결론

- 요구사항 불일치로 채택하지 않는다.

## B안. 기존 `tags` 테이블을 인플레이스 변환

### 개요

- `tags`에 `shop_id`, `sort_order`를 추가
- 기존 전역 태그를 매장별로 복제하면서 `shop_menu_tags`를 재매핑

### 장점

- 최종적으로는 테이블 수를 늘리지 않을 수 있다.
- 코드상 `Tag` 이름을 유지하기 쉽다.

### 단점

- 하나의 전역 태그 row를 여러 매장 row로 복제해야 해서 마이그레이션이 매우 공격적이다.
- 기존 `tag_id`를 참조하는 연결 데이터를 재작성해야 한다.
- 전환 중간 단계 설계가 복잡하다.

### 결론

- 가능은 하지만 위험도가 높아 기본안으로는 부적절하다.

## C안. `shop_tags` 신규 도입 + 연결 관계 전환

### 개요

- 새로운 `shop_tags` 테이블을 도입한다.
- 태그의 소속성과 순서를 `shop_tags`에 직접 저장한다.
- `shop_menu_tags`는 전역 `tag_id` 대신 `shop_tag_id`를 참조하게 바꾼다.

### 장점

- "태그는 매장 로컬"이라는 요구사항을 모델에 그대로 반영한다.
- 표시 순서가 태그 자체의 속성이 되어 보조 테이블이 필요 없다.
- 마이그레이션을 ADD -> backfill -> switch -> cleanup 단계로 나누기 쉽다.

### 단점

- 신규 테이블과 참조 컬럼 전환이 필요하다.
- 기존 전역 태그 API를 정리해야 한다.

### 결론

- **C안 채택**

---

## 최종 설계

## 1. 데이터 모델

### 신규 테이블 `shop_tags`

```sql
CREATE TABLE shop_tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_tags_shop_name (shop_id, name),
  UNIQUE KEY uq_shop_tags_shop_sort (shop_id, sort_order),
  INDEX idx_shop_tags_shop_sort (shop_id, sort_order)
);
```

### 변경 테이블 `shop_menu_tags`

- 기존 `tag_id` 참조를 점진적으로 폐기한다.
- 신규 `shop_tag_id`를 추가하고 `shop_tags(id)`를 참조하도록 전환한다.

### 의미

- 태그의 식별자는 `shop_tags.id`
- 태그명 uniqueness는 `shop_id + name`
- 순서도 `shop_id + sort_order`

즉, 태그의 정체성과 표시 순서가 모두 매장 내부 규칙으로 귀속된다.

---

## 2. API 재정의

### 운영자용 태그 생성

- `POST /api/shops/{shopId}/tags`

요청 예시:

```json
{
  "name": "손관리"
}
```

정책:

- 해당 매장에 같은 이름이 없으면 생성
- 최초 `sort_order`는 맨 뒤 append
- 다른 매장에 같은 이름이 있어도 허용

### 사용자용 태그 조회

- `GET /api/shops/{shopId}/tags`

정책:

- 활성 메뉴에 연결된 태그만 반환
- 중복 제거
- `shop_tags.sort_order ASC`

### 운영자용 태그 순서 변경

- `PUT /api/shops/{shopId}/tags/order`

요청 예시:

```json
{
  "tagIds": [4, 1, 3]
}
```

정책:

- visible 태그 전체만 받는다.
- canonical order는 `[요청 visible] + [hidden 기존 순서]`

### 메뉴-태그 연결/해제

- `POST /api/shops/{shopId}/menus/{menuId}/tags`
- `DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}`

변경점:

- `tagId`는 더 이상 전역 태그 ID가 아니다.
- 반드시 같은 `shopId` 소속 `shop_tag_id`여야 한다.

### 기존 전역 API 처리

- `POST /api/tags`, `GET /api/tags`는 더 이상 정합한 모델이 아니다.
- 전환 완료 후 제거하거나, 최소한 내부적으로는 `shop_tags` 기반으로 재설계해야 한다.
- 이번 이슈 범위에서는 **deprecated 대상으로 문서화**하는 쪽이 현실적이다.

---

## 3. visible / hidden 정책

## visible 태그

- 현재 활성 메뉴에 연결된 태그
- 사용자 조회 응답에 포함되는 태그
- 운영자 정렬 요청 payload에 포함되는 태그

## hidden 태그

- 비활성 메뉴에만 연결된 태그
- 현재 어떤 메뉴에도 연결되지 않았지만 매장에 남아 있는 태그

정책:

- hidden 태그 row는 `shop_tags`에 그대로 유지한다.
- 사용자 응답에는 포함하지 않는다.
- 정렬 변경 시 기존 상대 순서를 유지한 채 뒤로 보낸다.

이 설계에서는 기존처럼 `shop_tag_orders`를 따로 유지할 필요가 없다.  
순서 보존의 주체가 `shop_tags` 자체이기 때문이다.

---

## 4. 검증 정책

## owner 검증

운영자용 API는 아래를 공통 적용한다.

- `@RequireAuthenticatedUser`
- `ShopReader.isShopOwnedBy(shopId, ownerUserId)`
- 실패 시 `ShopErrorCode.SHOP_OWNER_MISMATCH`

## 소속 검증

아래 조합은 모두 같은 매장이어야 한다.

- `shopId`
- `menuId`
- `shopTagId`

검증 실패 시:

- `shopId-menuId` 불일치
- `shopId-shopTagId` 불일치
- 타 매장 태그 혼입

를 명시적인 business error로 막는다.

## 정렬 요청 검증

`PUT /api/shops/{shopId}/tags/order`는 아래를 모두 만족해야 한다.

- 현재 visible 태그 전체를 포함
- 중복 없음
- 누락 없음
- 타 매장 태그 혼입 없음
- hidden 태그는 payload에 포함하지 않음

---

## 5. 마이그레이션 전략

## Phase 1. ADD

- `shop_tags` 테이블 추가
- `shop_menu_tags.shop_tag_id` nullable 컬럼 추가
- 엔티티/리포지토리 추가
- 읽기 코드는 아직 기존 경로 유지

## Phase 2. Backfill

- 기존 `shop_menus + shop_menu_tags + tags`를 기준으로
  `distinct (shop_id, tag_name)` 집합을 추출해 `shop_tags` 생성
- 초기 순서는 매장 내부에서 기존 `tag.id ASC` 기반으로 부여
- `shop_menu_tags.shop_tag_id`를 backfill

### Backfill 상세 전략

#### Step 2-1. `shop_tags` 생성 backfill

기존 전역 `tags`와 `shop_menu_tags` 연결을 기준으로,
매장별로 아래 집합을 먼저 만든다.

```text
(shop_id, legacy_tag_name)
```

예:

```text
shop 10 + "손관리"
shop 10 + "발관리"
shop 20 + "손관리"
```

이 집합을 `shop_tags` row로 만든다.

정렬 초기값은 아래 규칙을 사용한다.

- 같은 매장 내부에서 `legacy tag.id ASC`
- 연속 정수 `0..N-1`

이유:

- 기존 전역 태그 기반 동작과 가장 유사한 초기 순서
- 운영자 입력 없이도 deterministic 하게 재구성 가능

#### Step 2-2. `shop_menu_tags.shop_tag_id` 채우기

각 `shop_menu_tags` row에 대해:

- `shop_menu_id -> shop_id`를 찾고
- `tag_id -> legacy tag.name`을 찾고
- 같은 `(shop_id, name)`의 `shop_tags.id`를 매핑해
- `shop_menu_tags.shop_tag_id`를 채운다

즉 매핑 규칙은 아래다.

```text
shop_menu_tags(tag_id)
-> tags(name)
-> (shop_id, name)
-> shop_tags(id)
```

#### Step 2-3. 불일치 검증 쿼리

backfill 직후에는 아래 검증이 필요하다.

- `shop_menu_tags.shop_tag_id IS NULL` row가 없는지
- 같은 `shop_id` 안에서 `shop_tags.name` 중복이 없는지
- `shop_tag_id`가 가리키는 `shop_tags.shop_id`와
  해당 `shop_menu`의 `shop_id`가 같은지

이 검증을 통과해야 switch 단계로 간다.

예:

```text
Shop A - global tag "손관리"(id=1) -> shop_tags(id=10, shop_id=A, name="손관리")
Shop B - global tag "손관리"(id=1) -> shop_tags(id=21, shop_id=B, name="손관리")
```

같은 전역 태그라도 매장별로 별도 row로 분리된다.

## Phase 3. Switch

- 서비스/컨트롤러가 `shop_tags`만 읽도록 전환
- 메뉴-태그 연결/해제도 `shop_tag_id` 기준으로 전환
- 사용자 조회 / 정렬 변경 / 태그 생성 API를 `shop_tags` 기준으로 동작시킴

### Switch 완료 조건

- `GET /api/shops/{shopId}/tags`가 이름 매칭 fallback 없이 동작
- 메뉴 필터 API가 `shop_tag_id` 기준으로도 정상 동작
- 태그 연결/해제 API가 `shop_tag_id`를 1급 식별자로 사용
- 신규 write path에서 `tag_id` fallback 생성이 더 이상 필요하지 않음

## Phase 4. Cleanup

- `shop_menu_tags.tag_id` 제거
- 더 이상 쓰지 않는 전역 `tags` API 정리
- 필요 시 `tags` 테이블 제거 또는 별도 taxonomy 용도로 분리 판단

### Cleanup 상세 전략

#### Step 4-1. fallback 제거

아래 코드를 제거 대상로 본다.

- `shop_tag_id or tag_id` 이중 조회
- legacy `tag_id` 입력을 받아 `ShopTag`를 자동 생성하는 fallback
- `shop_tags.name == tags.name` 기반 visible 조회 브리지

#### Step 4-2. API 의미 정리

아래 엔드포인트는 최종적으로 매장 로컬 태그 기준 의미만 가져야 한다.

- `POST /api/shops/{shopId}/menus/{menuId}/tags`
- `DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}`
- `GET /api/shops/{shopId}/menus?tagIds=...`

즉 여기서의 `tagId`는 더 이상 legacy `tags.id`가 아니라
`shop_tags.id`만 의미해야 한다.

#### Step 4-3. 스키마 정리

정리 순서는 아래를 권장한다.

1. `shop_menu_tags.shop_tag_id` NOT NULL 전환
2. `shop_menu_tags.tag_id` 참조 코드 제거 확인
3. `shop_menu_tags.tag_id` 컬럼 제거
4. 기존 `POST /api/tags`, `GET /api/tags` 제거 또는 deprecated 종료
5. 최종적으로 전역 `tags` 테이블 제거 여부 판단

#### Step 4-4. 롤백 포인트

cleanup 전까지는 `tag_id`가 남아 있으므로 비교적 안전하게 롤백 가능하다.

반대로 `tag_id` 컬럼 제거 이후에는 되돌리기 비용이 커진다.

따라서 `tag_id` 제거는 아래를 모두 만족한 뒤 마지막에 해야 한다.

- 프론트가 새 `shop_tag_id` 계약으로 전환 완료
- 백엔드 fallback 코드 제거 완료
- backfill 검증 쿼리 이상 없음
- 운영 환경에서 `tag_id` 사용 호출이 더 이상 없음

---

## 6. 변경 예시

## 예시 SQL 방향

```sql
ALTER TABLE shop_menu_tags ADD COLUMN shop_tag_id BIGINT NULL;

CREATE TABLE shop_tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL,
  ...
);
```

## 예시 코드 방향

```java
// before
tagService.createOrGet("손관리");

// after
tagService.create(shopId, "손관리", ownerUserId);
```

```java
// before
shopMenuTagRepository.save(ShopMenuTag.create(menuId, globalTagId));

// after
shopMenuTagRepository.save(ShopMenuTag.create(menuId, shopTagId));
```

변경 후 예상 결과:

- 태그는 매장 생성물로 취급된다.
- 같은 이름 태그가 매장별로 독립적으로 관리된다.
- 정렬 로직이 태그 자체 속성으로 단순화된다.

---

## 7. 리스크와 롤백

## 리스크

- backfill 시 하나의 전역 태그가 여러 매장 태그로 분기되므로 매핑 로직이 틀리면 메뉴 연결 정합성이 깨질 수 있다.
- `shop_menu_tags` 이중 컬럼 전환 기간에 읽기/쓰기 경로가 섞이면 버그가 생길 수 있다.
- 기존 전역 태그 API를 그대로 두면 새 모델과 충돌하는 호출이 계속 발생할 수 있다.

## 롤백

- Switch 이전이면 `shop_tag_id` 경로를 비활성화하고 기존 읽기/쓰기 경로로 되돌린다.
- `shop_tags` 테이블은 즉시 삭제하지 않고 보존한다.
- API 전환은 feature flag 또는 controller route 차단으로 되돌릴 수 있게 설계한다.

---

## 8. 테스트 전략

## TDD 우선순위

기본 우선순위는 아래와 같다.

1. `ShopTag` 생성이 매장 스코프 uniqueness를 지키는지
2. 같은 이름 태그가 다른 매장에서 공존 가능한지
3. 사용자 조회가 visible 태그만 정렬해서 반환하는지
4. 메뉴 연결이 타 매장 태그를 거부하는지
5. visible-only 재정렬 후 hidden이 뒤에 유지되는지

## 서비스 / JPA

- 매장 로컬 태그 생성
- 매장 내부 이름 중복 실패
- 교차 매장 동일 이름 허용
- 활성 메뉴 태그만 조회
- 중복 제거
- sort_order 정렬
- hidden 태그 순서 보존

## API / Integration

- `POST /api/shops/{shopId}/tags`
- `GET /api/shops/{shopId}/tags`
- `PUT /api/shops/{shopId}/tags/order`
- `SHOP_OWNER_MISMATCH`
- `shopId-menuId-shopTagId` 소속 불일치

## Migration

- `shop_tags` 테이블 생성
- backfill로 매장별 태그 row 생성
- `shop_menu_tags.shop_tag_id` backfill

---

## 9. 작업 순서 제안

1. 문서 기준 확정: 전역 태그 모델 폐기 방향 승인
2. `shop_tags` + `shop_menu_tags.shop_tag_id` 구조 설계/마이그레이션 초안
3. 첫 RED: 매장 로컬 태그 생성 uniqueness 테스트
4. `POST /api/shops/{shopId}/tags` 최소 구현
5. `GET /api/shops/{shopId}/tags` visible 조회 구현
6. 메뉴 연결/해제의 `shopTagId` 전환
7. `PUT /api/shops/{shopId}/tags/order` 구현
8. backfill / cleanup 마이그레이션

---

## 10. 작업 체크리스트

- [ ] `ShopTag` 엔티티/리포지토리 추가
- [ ] `shop_menu_tags`의 `shop_tag_id` 전환 설계
- [ ] `CreateShopTagRequest` 추가
- [ ] `POST /api/shops/{shopId}/tags` 구현
- [ ] `GET /api/shops/{shopId}/tags` 구현
- [ ] `PUT /api/shops/{shopId}/tags/order` 구현
- [ ] owner/소속 검증 추가
- [ ] backfill 마이그레이션 작성
- [ ] 기존 전역 `Tag` API deprecation 정리

---

## 한 줄 결론

- 채택안은 `global tags + shop_tag_orders`가 아니라, **`shop_tags`를 도입해 태그 자체를 매장 로컬 리소스로 전환하는 것**이다.
