# `#125` 구현 worklog

## 2026-05-01 21:40:25 +09:00

- 작업 단위: 점주 태그 관리 API 추가
- 커밋:
  - `3dbb7e0` `feat: add owner tag management APIs`
- 변경 내용:
  - `GET /api/shops/{shopId}/tags/manage` 추가
  - `PATCH /api/shops/{shopId}/tags/{tagId}` 추가
  - `DELETE /api/shops/{shopId}/tags/{tagId}` 추가
  - `/shop/link` 응답에 `shopId` 추가
  - `docs/api/03-api-spec.md` 갱신
- 이유:
  - 점주용 메뉴/카테고리 관리 화면에서 카테고리 전체 조회, 이름 수정, 삭제가 필요하다.
  - 기존 visible 태그 조회 API만으로는 메뉴에 연결되지 않은 카테고리를 관리할 수 없다.
- 검증:
  - `.\gradlew.bat compileJava` 통과
  - 내가 수정한 파일 대상 `git diff --check -- ...` 통과
- 다음 작업:
  - 브랜치 `feature/jiseob/#125` push
  - `develop` 대상 PR 생성

## 2026-05-01

- 작업 단위: 프론트 API handoff 문서 추가
- 변경 내용:
  - `docs/issues/#125/frontend-api-handoff.md` 추가
  - 점주 메뉴/카테고리 관리 화면 연동 순서, API 계약, mutation 후 재조회 기준, 제한사항 정리
- 이유:
  - 프론트에서 새 카테고리 관리 API와 기존 메뉴 API를 함께 연동할 때 필요한 기준을 한 문서로 전달하기 위함
- 검증:
  - 문서 변경만 수행
- 다음 작업:
  - 프론트 연동 중 추가로 필요한 메뉴별 태그 응답 확장 여부 확인

## 2026-05-02

- 작업 단위: 메뉴 목록 응답에 연결 카테고리 목록 추가
- 변경 내용:
  - `ShopMenuResponse.tags` 추가
  - `ShopMenuTagRepository.findShopTagsByMenuIds` bulk 조회 추가
  - 메뉴 생성/수정/목록 응답에 `shop_tags.id` 기준 태그 목록 포함
  - API 명세와 프론트 handoff 문서 갱신
- 이유:
  - 메뉴 수정 모달에서 현재 연결된 카테고리를 API 응답만으로 정확히 표시하기 위함
  - 새 관리 화면 계약은 legacy `tags.id`가 아닌 `shop_tags.id` 기준으로 고정하기로 했음
- 검증:
  - `.\gradlew.bat compileJava`는 증분 컴파일 산출물 상태 때문에 기존 패키지 class를 찾지 못하는 형태로 실패
  - `.\gradlew.bat clean compileJava` 통과
  - 이후 `.\gradlew.bat compileJava` 재실행 통과
- 다음 작업:
  - 프론트에서 `menu.tags` 기반 수정 모달 초기값 적용

## 2026-05-02

- 작업 단위: CI `compileTestJava` 실패 수정
- 변경 내용:
  - `ShopMenuManagementServiceTest`에서 변경된 서비스 생성자에 맞춰 `ShopMenuTagRepository` 주입 추가
- 이유:
  - `ShopMenuManagementService` 생성자에 `ShopMenuTagRepository`가 추가되면서 기존 테스트 생성 코드가 컴파일되지 않았음
- 검증:
  - `.\gradlew.bat build` 통과
- 다음 작업:
  - GitHub Actions 재실행 결과 확인
