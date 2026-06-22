# 테스트 자산 제거 작업 기록

- 일시: 2026-06-22 16:50:01 +09:00
- 작업 단위: 저장소의 테스트 자산 및 테스트 전용 Gradle 설정 제거
- 분류: Structural
- 변경 내용: `src/test` 전체를 삭제하고 `build.gradle`에서 JUnit, Spring Boot Test, 테스트용 Lombok 설정과 `useJUnitPlatform()` 구성을 제거했다.
- 이유: 기존 테스트 스위트를 전면 폐기하되 프로덕션 빌드는 유지하기 위해서다.
- 검증: `./gradlew clean build` 성공, 저장소 내 `src/test`, `src/integrationTest`, `*Test`/`*Tests` 소스 및 테스트 전용 Gradle 설정 0건 확인.
- 다음 작업: 필요 시 새 테스트 전략과 최소 테스트 기반을 별도 변경 단위로 설계한다.
