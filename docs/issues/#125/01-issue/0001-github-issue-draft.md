# `[FEATURE]: Add owner tag management APIs`

Issue: `#125`  
Created: `2026-05-01`  
Repository: `WAP-SNAPBOOK/BE`  
Issue URL: `https://github.com/WAP-SNAPBOOK/BE/issues/125`  
Branch: `feature/jiseob/#125`

---

## Background

프론트에서 점주용 메뉴/카테고리 관리 화면을 추가하려면 매장 로컬 카테고리(`shop_tags`)를 관리할 수 있는 API가 필요하다. 현재 API 문서와 구현에는 태그 생성, visible 태그 조회, 정렬, 메뉴 연결/해제만 있고 카테고리 이름 수정/삭제 및 관리용 전체 조회가 없다.

## Problem

- `GET /api/shops/{shopId}/tags`는 활성 메뉴에 연결된 visible 태그만 반환한다.
- 새 카테고리를 생성해도 메뉴에 연결되기 전에는 관리 화면에서 다시 조회되지 않을 수 있다.
- 카테고리 이름 수정/삭제 API가 없어 점주가 관리 화면에서 기본적인 편집을 수행할 수 없다.
- 프론트가 점주 소유 매장의 `shopId`를 안정적으로 얻을 수 있는 응답 필드가 부족하다.

## Goal

점주 메뉴/카테고리 관리 화면 연동에 필요한 최소 API를 제공한다.

## Scope

- 점주용 전체 카테고리 조회 API 추가
- 매장 로컬 카테고리 이름 수정 API 추가
- 매장 로컬 카테고리 삭제 API 추가
- `/shop/link` 응답에 `shopId` 추가
- API 명세 문서 갱신

## Out Of Scope

- 프론트엔드 화면 구현
- 테스트 코드 추가
- 태그 소프트 삭제용 DB 스키마 변경
- legacy 전역 태그 모델 제거

## Acceptance Criteria

- [ ] 점주는 `GET /api/shops/{shopId}/tags/manage`로 메뉴 연결 여부와 관계없이 모든 매장 로컬 태그를 조회할 수 있다.
- [ ] 점주는 `PATCH /api/shops/{shopId}/tags/{tagId}`로 매장 로컬 태그 이름을 수정할 수 있다.
- [ ] 같은 매장 안에서 중복 이름으로 수정하면 `SHOP_TAG_ALREADY_EXISTS`가 반환된다.
- [ ] 점주는 `DELETE /api/shops/{shopId}/tags/{tagId}`로 매장 로컬 태그를 삭제할 수 있다.
- [ ] 다른 점주의 매장/태그에 대한 관리 요청은 차단된다.
- [ ] `/shop/link` 응답에서 프론트가 `shopId`를 확인할 수 있다.

## Risks

- 현재 `shop_tags`에는 `deleted_at` 또는 `is_visible`이 없어 삭제는 물리 삭제로 처리해야 한다.
- legacy `tags`와 `shop_tags`가 공존하는 과도기 모델이라, 메뉴 연결 삭제 범위를 명확히 제한해야 한다.

## References

- `docs/api/03-api-spec.md`
- `src/main/java/com/example/easybooking/shop/presentation/TagController.java`
- `src/main/java/com/example/easybooking/shop/service/TagService.java`
