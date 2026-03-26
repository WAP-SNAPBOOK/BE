# `#109` 매장 로컬 태그 모델 전환 및 태그 관리 API 추가

## 배경

기존 태그 모델은 전역 `Tag` 중심이라서
`태그는 특정 매장 안에서만 통용돼야 한다`는 요구사항을 직접 표현하지 못했다.

이 PR은 태그 모델을 `shop_tags` 기반 매장 로컬 태그로 전환하고,
프론트가 사용할 생성/조회/정렬 API를 추가한다.

동시에 프론트 전환 전까지 서비스가 깨지지 않도록
`shop_menu_tags.tag_id`와 `shop_menu_tags.shop_tag_id`를 함께 버티는 브리지/fallback 구간도 넣는다.

---

## 핵심 변경

### 1. 매장 로컬 태그 모델 도입

- `shop_tags` 테이블 추가
- `ShopTag` 엔티티 / `ShopTagRepository` 추가
- `(shop_id, name)`, `(shop_id, sort_order)` 유니크 제약 추가

### 2. 태그 API 추가

- `POST /api/shops/{shopId}/tags`
  - 점주가 매장 태그 생성
  - 같은 매장 같은 이름은 `409 + SHOP_TAG_ALREADY_EXISTS`

- `GET /api/shops/{shopId}/tags`
  - 활성 메뉴에 연결된 visible 태그 조회
  - 중복 제거
  - `sort_order` 순서 보장

- `PUT /api/shops/{shopId}/tags/order`
  - visible 태그 전체 순서 재정렬
  - hidden 태그는 기존 상대 순서를 유지한 채 뒤로 배치

### 3. 메뉴-태그 연결 브리지 추가

- `shop_menu_tags.shop_tag_id` 추가
- `ShopMenuTag`에 `tagId + shopTagId` 공존 구조 추가
- 메뉴 태그 연결/삭제 API는 과도기 동안 legacy `tag_id`와 new `shop_tag_id`를 모두 수용

### 4. backfill 및 cleanup 준비

- `V8__backfill_shop_tags.sql` 추가
  - 기존 `shop_menu_tags + tags + shop_menus` 기준으로 `shop_tags` 생성
  - `shop_menu_tags.shop_tag_id` backfill

- legacy `/api/tags`는 deprecated 표시
- legacy fallback 사용 시 warn 로그 추가
- visible 조회는 direct path / legacy fallback path로 분리해 가독성 개선

---

## API 영향

### 새로 쓰는 API

- `POST /api/shops/{shopId}/tags`
- `GET /api/shops/{shopId}/tags`
- `PUT /api/shops/{shopId}/tags/order`

### 유지되지만 deprecated인 API

- `POST /api/tags`
- `GET /api/tags`

### 프론트 주의사항

- 새 구조 기준 태그 식별자는 `GET /api/shops/{shopId}/tags` 응답의 `id`
- 메뉴 태그 연결 요청의 필드명은 아직 `tagId`지만,
  과도기 동안 `shopTagId`도 같은 필드로 전달 가능
- 새 화면/새 연동에서는 `/api/tags` 사용 금지 권장

프론트 전달 문서:
- [frontend-api-handoff.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/frontend-api-handoff.md)

---

## 주요 커밋 흐름

1. `Structural: add shop tag model foundation`
2. `Behavioral: add POST /api/shops/{shopId}/tags`
3. `Behavioral: add GET /api/shops/{shopId}/tags`
4. `Structural: add shop_tag_id bridge to shop_menu_tags`
5. `Behavioral: support legacy tag fallback in menu tag flows`
6. `Behavioral: add PUT /api/shops/{shopId}/tags/order`
7. `Behavioral: reject duplicate shop tag creation with 409`
8. `Structural: add shop tag backfill migration and tests`
9. `Behavioral: deprecate legacy tag APIs and add fallback logs`
10. `Structural: split visible tag reads into direct and legacy paths`

---

## 테스트 / 검증

### 확인한 것

- `./gradlew compileJava` 성공
- 서비스 테스트 / 통합 테스트 / migration 테스트 코드 추가

### 현재 제한

전체 테스트 런은 현재 워크트리의 unrelated 테스트 문제 때문에 안정적으로 확인하지 못했다.

대표 이슈:
- `src/test/java/com/example/easybooking/reservation/domain/ReservationUnitTest.java`
  - 현재 시그니처 불일치로 컴파일 오류 발생 가능
- 일부 실행에서는 `build/test-results` 파일 잠금 이슈도 있었음

즉 이 PR은 `컴파일 확인 + 테스트 코드 추가`까지는 완료했고,
전체 테스트 런 안정화는 별도 워크트리 정리가 필요하다.

---

## 리스크

### 1. 전환기 이중 경로 복잡성

현재는 `tag_id`와 `shop_tag_id`를 함께 들고 가므로
로직 복잡도가 올라가 있다.

대신 이건 프론트 전환 전 호환성을 위해 의도적으로 남긴 상태다.

### 2. legacy 경로 미제거

`/api/tags`와 legacy fallback은 아직 살아 있다.

즉 이 PR은 최종 cleanup이 아니라
`전환 준비 완료` 상태를 만드는 PR이다.

### 3. backfill은 넣었지만 cleanup은 아직 아님

backfill SQL은 포함돼 있지만,
아직 `shop_menu_tags.tag_id` 제거까지는 하지 않았다.

---

## 롤백

롤백이 필요하면 아래 순서로 본다.

1. 프론트는 계속 legacy `/api/tags` 및 legacy `tag_id` 경로를 사용할 수 있음
2. 백엔드 동작상 브리지/fallback이 남아 있어 즉시 파국적 장애로 이어질 가능성은 낮음
3. 단, DB migration(`V6`, `V7`, `V8`)이 이미 적용된 경우 스키마 롤백은 별도 판단 필요

즉 애플리케이션 레벨 롤백보다
운영에서는 `fallback 유지 상태`를 이용해 완충하는 쪽이 더 현실적이다.

---

## 후속 작업

- 프론트가 `GET /api/shops/{shopId}/tags` 응답의 `id`를 기준으로 완전히 전환
- 로그 기준으로 legacy `/api/tags` 및 legacy `tag_id` fallback 사용 여부 확인
- 사용이 사라지면 cleanup PR에서 아래 제거
  - `/api/tags`
  - `TagService.createOrGet`, `getAllTags`
  - `resolveLegacyTagIds`
  - `shop_menu_tags.tag_id`
  - legacy fallback 조회 쿼리

---

## 관련 문서

- [design-plan.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/design-plan.md)
- [work-log.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/work-log.md)
- [commit-unit-flow.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/commit-unit-flow.md)
- [frontend-api-handoff.md](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#109/frontend-api-handoff.md)
