# `#109` 커밋 단위 흐름 설명

작성일: `2026-03-23`

## 문서 목적

이 문서는 `#109` 작업을 **실제로 커밋했다면 어떻게 끊는 게 자연스러운지** 기준으로 설명하는 문서다.

`work-log.md`가 작업 기록이라면,
이 문서는 그보다 조금 더 "커밋 단위 흐름"에 집중한다.

즉 각 단위마다 아래를 본다.

- 왜 이 단위가 먼저 필요한가
- 여기서 무엇을 바꿨나
- 왜 이걸 한 뒤 다음 단위로 넘어갈 수 있었나
- 이 시점에서 무엇은 아직 일부러 안 건드렸나

이 문서는 요약 문서가 아니라 **흐름 설명 문서**다.  
따라서 한 단계씩 읽으면 이번 작업이 어떻게 전개됐는지 따라갈 수 있게 썼다.

---

## 전체 흐름 한 줄 요약

이번 작업은 아래 순서로 진행됐다.

1. 전제를 바꿨다
2. 새 모델을 세웠다
3. 생성 API를 붙였다
4. 조회 API를 붙였다
5. 브리지 fallback을 넣었다
6. backfill/cleanup 전략을 정했다
7. 정렬 API를 붙였다
8. 생성 정책을 엄격하게 고쳤다
9. cleanup 준비를 코드에 심었다
10. backfill SQL을 넣었다
11. visible 조회를 읽기 쉬운 구조로 다시 잘랐다

즉 "새 모델을 만들고 바로 old 코드를 지우는" 방식이 아니라,  
**새 모델 도입 -> 브리지 유지 -> backfill -> cleanup 준비** 순서로 갔다.

---

## Commit Unit 1. 설계 기준을 뒤집는 문서 변경

### 이 단위가 먼저였던 이유

처음에는 `전역 Tag + 매장별 순서 보조 테이블` 방향으로 갈 수도 있었다.

하지만 요구사항이 명확해지면서 핵심 전제가 바뀌었다.

- 태그는 전역이면 안 된다
- 태그는 특정 매장 안에서만 통용돼야 한다

이건 구현 디테일이 아니라 도메인 전제를 바꾸는 일이다.  
그래서 코드를 먼저 건드리면 안 되고, 문서를 먼저 다시 고정해야 했다.

### 이 단위에서 한 일

- 이슈 문서 갱신
- 설계 문서 재작성
- TDD 시작점 재설정

핵심 결정은 아래였다.

- `global tags + shop_tag_orders` 폐기
- `shop_tags` 도입 채택
- 정렬보다 먼저 "태그의 정체성 변경"부터 구현

### 왜 이 다음에 바로 구현으로 갈 수 있었나

이 단위로 인해 이후 모든 코드 변경의 기준이 생겼다.

즉 다음 단계부터는
"태그는 전역이 아니라 매장 로컬"이라는 전제를 흔들지 않고 갈 수 있게 됐다.

### 이 시점에 일부러 안 한 것

- 실제 코드 수정
- 마이그레이션 추가
- API 추가

이 단위는 순수하게 기준을 고정하는 단계였다.

---

## Commit Unit 2. `shop_tags` 모델 도입

### 왜 이 단위가 두 번째였나

새 요구사항의 핵심은 조회 API가 아니라 태그의 정체성 변화였다.

즉 먼저 코드에 생겨야 할 건:

- `shopId`를 갖는 태그 엔티티
- 매장 내부 정렬을 갖는 태그 엔티티

였다.

### 이 단위에서 한 일

- `ShopTag` 엔티티 추가
- `ShopTagRepository` 추가
- `shop_tags` 테이블 생성 migration 추가
- 같은 이름 태그가 다른 매장에 각각 존재 가능한 서비스 테스트 추가

핵심은

- `(shop_id, name)` 유니크
- `(shop_id, sort_order)` 유니크

를 모델에 박아 넣은 것이다.

### 이 단위가 만든 변화

이제 시스템은 처음으로 아래를 표현할 수 있게 됐다.

- 매장 A의 `손관리`
- 매장 B의 `손관리`

이 둘은 이름은 같지만 서로 다른 태그다.

### 왜 이 다음에 생성 API로 갈 수 있었나

모델이 없는데 API부터 만들 수는 없다.  
`ShopTag`와 repository가 생겼기 때문에, 다음 단계에서 점주가 실제로 태그를 만드는 API를 붙일 수 있게 됐다.

### 이 시점에 일부러 안 한 것

- 메뉴 연결 구조 전환
- 정렬 API
- visible 조회

즉 "새 태그 모델 존재"까지만 만들었다.

---

## Commit Unit 3. `POST /api/shops/{shopId}/tags` 생성 API 추가

### 왜 이 단위가 다음이었나

모델만 있으면 아직 도메인 객체가 저장소 안에만 갇혀 있다.  
실제 제품 흐름으로 보면 점주가 태그를 만들어야 이후 조회/연결/정렬이 의미를 갖는다.

그래서 다음으로 필요한 최소 동작은 생성 API였다.

### 이 단위에서 한 일

- `CreateShopTagRequest` 추가
- `POST /api/shops/{shopId}/tags` 추가
- owner 검증 추가
  \
- ]'[- 성공/권한 실패 통합 테스트 추가

### 핵심 의도

이 단위는 "매장 로컬 태그"를 **도메인 모델**에서 **운영자 API 계약**으로 끌어올리는 단계였다.

즉 이제 태그는 실제 점주 기능이 됐다.

### 왜 이 다음에 조회 API로 갈 수 있었나

조회는 읽기고, 읽을 대상이 먼저 존재해야 한다.  
생성 API가 붙었기 때문에 다음 단계에서 "사용자에게 어떤 태그를 보여줄지"를 읽는 흐름으로 넘어갈 수 있었다.

### 이 시점에 일부러 안 한 것

- 중복 생성 정책 확정
- 정렬 API
- 메뉴 연결 구조 변경

생성 경로만 최소한으로 올렸다.

---

## Commit Unit 4. `GET /api/shops/{shopId}/tags` 사용자 조회 추가

### 왜 이 단위가 필요했나

이슈의 사용자 가치 중 하나는 "해당 매장에 실제 보여줄 태그 목록을 안정적으로 내려주는 것"이었다.

생성만 있고 조회가 없으면 프론트는 여전히 쓸 수 없다.

### 이 단위에서 한 일

- `ShopTagRepository.findVisibleByShopIdOrderBySortOrderAsc(...)` 추가
- `TagService.getVisibleShopTags(...)` 추가
- `GET /api/shops/{shopId}/tags` 추가
- visible/중복제거/순서 테스트 추가

### 이 단위의 중요한 특징

이 시점에서는 아직 `shop_menu_tags`가 `shop_tag_id`를 직접 참조하지 않았다.

그래서 조회는 과도기적으로:

- `shop_tags.name`
- legacy `tags.name`

을 매칭해서 visible 태그를 계산했다.

즉 이 단위는 최종형이라기보다 **사용자 조회 계약을 먼저 고정하는 단계**였다.

### 왜 이 다음에 브리지 fallback 단계가 필요했나

조회는 붙었지만, 내부 구조는 아직 old/new 모델이 섞여 있었다.  
프론트 전환 전까지 안정적으로 버티려면 연결 테이블도 브리지 상태가 필요했다.

### 이 시점에 일부러 안 한 것

- `shop_menu_tags` 구조 전환
- `tag_id` 제거
- 정렬 API

---

## Commit Unit 5. `shop_menu_tags` 브리지 fallback 단계 도입

### 왜 이 단위가 중요했나

이 시점부터는 "새 모델로 가야 한다"와 "기존 프론트는 아직 안 깨져야 한다"가 충돌하기 시작했다.

즉 현실적으로 필요한 건:

- 백엔드는 `shop_tag_id`를 쓰기 시작해야 하고
- 프론트는 당분간 `legacy tag_id`를 보내도 살아야 한다

였다.

### 이 단위에서 한 일

- `shop_menu_tags`에 `shop_tag_id` 추가
- 엔티티에 `tagId + shopTagId` 공존 구조 추가
- repository에 `findBy...AnyTagId`, `deleteBy...AnyTagId` 추가
- `TagService.addTagToMenu/removeTagFromMenu`를 old/new 둘 다 해석 가능하게 변경
- `ShopMenuRepository`와 `ShopTagRepository`의 조회 쿼리도 브리지 상태 반영

### 핵심 아이디어

이 단위의 핵심은 **이중 식별자 공존**이다.

즉 같은 연결 row에:

- legacy `tag_id`
- new `shop_tag_id`

를 동시에 들고 있게 해서 전환기를 버티게 했다.

### 왜 이 다음에 문서로 backfill/cleanup 전략을 정리했나

브리지 코드는 강력하지만 오래 두면 복잡성만 커진다.  
그래서 이 다음 단계에서는 "언제 어떻게 이걸 치울지"를 문서로 먼저 정리해야 했다.

### 이 시점에 일부러 안 한 것

- `tag_id` 제거
- fallback 제거
- backfill SQL 작성

즉 "버틸 구조"만 만들었다.

---

## Commit Unit 6. backfill / cleanup 전략 문서화

### 왜 코드보다 문서가 먼저였나

브리지 구조를 만든 뒤 가장 위험한 건 즉흥적으로 정리하다가 데이터를 망치는 것이다.

그래서 다음으로 필요한 건 코드보다 순서였다.

### 이 단위에서 한 일

- `shop_tags` backfill 전략 정리
- `shop_menu_tags.shop_tag_id` backfill 규칙 정리
- cleanup 순서 정리
- `tag_id` 제거 전 만족해야 할 조건 정리

### 이 단위의 핵심 의미

이제 시스템은 아래 4단계를 분명히 갖게 됐다.

1. ADD
2. BACKFILL
3. SWITCH
4. CLEANUP

이 순서를 정해놨기 때문에 다음부터는 구현이 어디 단계인지 명확해졌다.

### 왜 이 다음에 정렬 API로 갈 수 있었나

브리지와 cleanup 전략이 정리되었으니, 다시 사용자/운영자 기능으로 돌아가도 전체 흐름을 잃지 않게 됐다.

### 이 시점에 일부러 안 한 것

- 실제 backfill SQL 작성
- cleanup 실행

전략만 고정했다.

---

## Commit Unit 7. `PUT /api/shops/{shopId}/tags/order` 정렬 API 추가

### 왜 이 단위가 다음이었나

이슈의 핵심 기능 중 하나가 정렬 API였고,
이전 단계들로 인해 이제 정렬의 기반인 `shop_tags.sort_order`가 준비돼 있었다.

즉 이제는 실제 운영 기능을 붙일 차례였다.

### 이 단위에서 한 일

- `UpdateShopTagOrderRequest` 추가
- `INVALID_SHOP_TAG_ORDER` 추가
- `updateShopTagOrder(...)` 서비스 로직 구현
- `PUT /api/shops/{shopId}/tags/order` 추가
- 성공/실패 테스트 추가

### 핵심 로직

정렬 규칙은 아래였다.

```text
[요청한 visible 태그] + [hidden 태그 기존 상대 순서]
```

그리고 `(shop_id, sort_order)` 유니크 충돌을 피하려고:

1. 전체 순서를 임시로 뒤로 밀고
2. 최종 순서를 다시 쓰는

2단계 업데이트를 넣었다.

### 왜 이 다음에 생성 API의 중복 정책을 정리했나

기능의 큰 뼈대는 이제 거의 다 생겼다.  
이제부터는 API 의미를 더 선명하게 다듬는 단계로 넘어갈 수 있었다.

### 이 시점에 일부러 안 한 것

- partial move API
- fallback 제거

---

## Commit Unit 8. 생성 API 중복 정책을 `409`로 확정

### 왜 이 단위가 필요했나

처음에는 `POST /api/shops/{shopId}/tags`가 같은 이름이면 기존 row를 재사용했다.

하지만 이건 생성 API 의미를 흐렸다.

- 생성인데 실제로는 조회처럼 동작
- 프론트가 "이미 있음"을 명확히 알기 어려움

### 이 단위에서 한 일

- `SHOP_TAG_ALREADY_EXISTS` 추가
- `createShopTag(shopId, name)`를 conflict 발생 방식으로 변경
- 서비스/통합 테스트 추가

### 정책 변화

이 단위 전:

- 같은 매장, 같은 이름 -> 기존 태그 재사용

이 단위 후:

- 같은 매장, 같은 이름 -> `409 + SHOP_TAG_ALREADY_EXISTS`

### 왜 이 다음에 cleanup 준비를 코드로 심었나

주요 API 의미가 정리됐으니, 이제 old path를 지울 준비를 코드에 남길 수 있었다.

---

## Commit Unit 9. legacy 경로 deprecated + warn 로그 추가

### 왜 바로 제거하지 않았나

프론트 전환 전이므로 old path를 지우면 위험했다.

하지만 계속 조용히 두는 것도 위험했다.  
실제로 누가 아직 쓰는지 알 수 없기 때문이다.

### 이 단위에서 한 일

- `/api/tags`를 `@Deprecated`
- `TagService.createOrGet`, `getAllTags`를 `@Deprecated`
- legacy `/api/tags` 사용 시 warn 로그
- legacy `tag_id` fallback 사용 시 warn 로그

### 이 단위의 의미

이 단위는 cleanup 자체가 아니라 **cleanup 준비를 관측 가능하게 만든 단계**다.

즉 이제 운영/테스트 로그를 보면:

- 전역 태그 API가 아직 쓰이는지
- legacy `tag_id` fallback이 아직 쓰이는지

를 확인할 수 있다.

### 왜 이 다음에 backfill SQL을 넣었나

관측 가능성까지 확보했으니, 이제 실제 데이터 이행 SQL을 추가해도 cleanup 타이밍을 통제할 수 있게 됐다.

---

## Commit Unit 10. backfill SQL과 migration-test 추가

### 왜 이 단위가 마지막 큰 준비물이었나

이전까지는 구조와 기능과 정책을 정리했다.  
하지만 기존 운영 데이터가 그대로면 새 구조는 반쪽짜리다.

그래서 실제로 필요한 마지막 준비는:

- 기존 데이터에서 `shop_tags` 생성
- `shop_menu_tags.shop_tag_id` 채우기

였다.

### 이 단위에서 한 일

- 운영용 `V8__backfill_shop_tags.sql` 추가
- test migration SQL 추가
- backfill 결과 migration test 추가

### 이 단위의 핵심 의미

이제 시스템은 단순히 새 모델을 "지원"하는 수준이 아니라,
기존 데이터를 새 모델로 **옮길 수 있는 경로**까지 갖게 됐다.

즉 cleanup으로 가기 전 필요한 준비물이 거의 다 갖춰진 상태다.

### 왜 여기서 멈추는 게 맞았나

프론트가 아직 전환 전이라면,
여기서 fallback까지 제거하는 건 이득보다 리스크가 크다.

따라서 이 시점의 올바른 판단은:

- 새 모델 도입 완료
- 브리지/fallback 유지
- backfill 준비 완료
- cleanup은 프론트 전환 후 별도 단위

였다.

---

## Commit Unit 11. visible 조회를 두 쿼리 + service merge로 분리

### 왜 이 단위가 필요했나

기존 visible 조회는 동작은 맞았지만 설명하기가 어려웠다.

- 정상 경로와 fallback 경로가 한 JPQL 안에 같이 있었다
- `join`과 `or`를 한 번에 해석해야 했다
- cleanup 때 무엇을 지울지 경계가 흐릿했다

즉 지금 필요한 건 기능 추가가 아니라,
브리지 조회를 사람이 읽을 수 있게 다시 자르는 구조 변경이었다.

### 이 단위에서 한 일

- `ShopTagRepository.findVisibleByShopTagIdOrderBySortOrderAsc(...)` 추가
- `ShopTagRepository.findVisibleByLegacyTagFallbackOrderBySortOrderAsc(...)` 추가
- `TagService.readVisibleShopTags(...)` helper 추가
- `getVisibleShopTags(...)`, `updateShopTagOrder(...)`가 이 helper를 공통 사용하도록 변경

### 핵심 의도

이제 visible 조회의 의미가 아래처럼 분리돼 보인다.

- 정상 경로: `shopTagId`로 직접 연결된 태그
- fallback 경로: 아직 `shopTagId`가 없어서 이름으로 임시 매칭한 태그

그리고 service는 두 결과를 합치고,
중복을 제거하고,
정렬을 맞춘다.

즉 브리지 로직이 "큰 쿼리 속 숨은 조건"이 아니라
"분리된 두 경로를 명시적으로 합치는 코드"로 드러난다.

### 왜 이 단위가 의미 있나

cleanup 시점이 오면 제거 대상이 더 직접적으로 보인다.

- fallback repository 메서드 제거
- `readVisibleShopTags(...)`의 merge 일부 제거

즉 최종형으로 갈 때 무엇을 지우면 되는지가 더 분명해졌다.

### 이 시점에 일부러 안 한 것

- fallback 제거
- `tag_id` 제거
- 조회 계약 변경

즉 이 단위는 순수하게 가독성과 설명 가능성을 높이는 구조 변경이다.

---

## 지금 기준으로 보면 어디까지 왔나

지금 코드는 아래 상태다.

### 이미 끝난 것

- 새 모델 도입
- 생성 API
- 조회 API
- 정렬 API
- 브리지 fallback
- backfill SQL
- deprecated / 관측 가능 상태

### 아직 일부러 남겨둔 것

- `/api/tags`
- `TagService.createOrGet`, `getAllTags`
- `ShopMenuTag.tagId`
- `resolveLegacyTagIds`
- 이름 매칭 기반 visible 조회 fallback
- 메뉴 필터에서 `OR smt.tagId`

즉 지금은 "cleanup 전 마지막 안정 상태"라고 보면 된다.

---

## 만약 실제 커밋 메시지를 붙인다면

예시 흐름은 아래처럼 갈 수 있다.

1. `docs: redefine #109 around shop-local tags`
2. `feat: add shop_tags model and migration`
3. `feat: add POST /api/shops/{shopId}/tags`
4. `feat: add GET /api/shops/{shopId}/tags`
5. `feat: add shop_tag_id bridge to shop_menu_tags`
6. `docs: define backfill and cleanup phases for shop tags`
7. `feat: add PUT /api/shops/{shopId}/tags/order`
8. `fix: reject duplicate shop tag creation with 409`
9. `chore: deprecate legacy tag APIs and add cleanup logs`
10. `feat: add shop tag backfill migration and migration tests`

이 순서가 이번 작업의 실제 흐름과 가장 가깝다.

---

## 마지막 정리

이번 작업의 핵심은 "태그 기능 추가"가 아니었다.  
실제로는 **전역 태그 모델을 매장 로컬 태그 모델로 바꾸는 전환 작업**이었다.

그래서 커밋 단위도 기능별이 아니라 전환 단계별로 끊어졌다.

- 기준을 바꾸고
- 새 모델을 세우고
- API를 붙이고
- 브리지를 넣고
- backfill을 준비하고
- cleanup은 나중으로 미뤘다

이 흐름으로 이해하면 지금 코드가 왜 이렇게 생겼는지 가장 자연스럽게 읽힌다.
