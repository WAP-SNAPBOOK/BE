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
