# `#125` 점주 태그 관리 API 구현 계획

작성일: `2026-05-01 21:40:25 +09:00`

## 목표

점주용 메뉴/카테고리 관리 화면에서 필요한 매장 로컬 태그 전체 조회, 이름 수정, 삭제 API를 제공한다.

## 변경 단위 1

- 목표: 점주용 전체 카테고리 조회 API를 추가한다.
- 분류: `Behavioral`
- 수정 대상:
  - `TagController`
  - `TagService`
  - `ShopTagRepository`
- 검증:
  - `compileJava`
  - API 문서에서 관리 목록 조회 계약 확인
- 완료 조건:
  - `GET /api/shops/{shopId}/tags/manage`가 소유자 검증 후 모든 `shop_tags`를 `sort_order` 순서로 반환한다.

## 변경 단위 2

- 목표: 매장 로컬 카테고리 이름 수정 API를 추가한다.
- 분류: `Behavioral`
- 수정 대상:
  - `ShopTag`
  - `UpdateShopTagRequest`
  - `TagController`
  - `TagService`
- 검증:
  - `compileJava`
  - 중복 이름 처리 코드 경로 확인
- 완료 조건:
  - `PATCH /api/shops/{shopId}/tags/{tagId}`가 소유자/소속 검증 후 이름을 수정한다.
  - 같은 매장 안의 중복 이름은 `SHOP_TAG_ALREADY_EXISTS`로 차단한다.

## 변경 단위 3

- 목표: 매장 로컬 카테고리 삭제 API를 추가한다.
- 분류: `Behavioral`
- 수정 대상:
  - `ShopMenuTagRepository`
  - `TagController`
  - `TagService`
- 검증:
  - `compileJava`
  - 삭제 전 연결 해제 코드 경로 확인
- 완료 조건:
  - `DELETE /api/shops/{shopId}/tags/{tagId}`가 소유자/소속 검증 후 메뉴 연결을 제거하고 `shop_tags` 행을 삭제한다.

## 변경 단위 4

- 목표: 프론트가 점주 매장 `shopId`를 얻을 수 있게 한다.
- 분류: `Behavioral`
- 수정 대상:
  - `LinkInfoResponse`
  - `ShopService`
  - `docs/api/03-api-spec.md`
- 검증:
  - `compileJava`
  - API 문서 응답 예시 확인
- 완료 조건:
  - `/shop/link` 응답에 `shopId`가 포함된다.

## 테스트 방침

사용자 요청에 따라 테스트 코드는 추가하지 않는다. 최소 검증은 `compileJava`로 수행한다.
