# `#128 점주/직원용 캘린더 조회 API` 작업 로그

작성일: `2026-06-17`  
기준 이슈: `#128`  
관련 브랜치: `feature/#128`

## Entries

### Entry 001

- Date: `2026-06-17 19:01`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `docs/backlog/2026-06-17-owner-calendar-follow-up-backlog.md`
- What:
  - `#128`에서 제외할 예약 상세, 액션, 변경 이력, 자동 메시지, 직원 권한 확장 범위를 백로그로 분리했다.
- Why:
  - 캘린더 조회 API와 예약 액션/이력/메시지 구현을 한 이슈에 묶으면 변경 범위가 커지고 검증 단위가 흐려진다.
- Verification:
  - 문서 추가 후 `git status --short --branch`로 변경 파일을 확인했다.
- Next:
  - `#128` 캘린더 조회 API 문서와 프론트 전달 문서를 작성한다.

### Entry 002

- Date: `2026-06-17 19:01`
- Unit: `pre-commit`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/reservation/presentation/OwnerCalendarController.java`
  - `src/main/java/com/example/easybooking/reservation/service/OwnerCalendarService.java`
  - `src/main/java/com/example/easybooking/reservation/dto/calendar/*`
  - `src/main/java/com/example/easybooking/reservation/ReservationReader.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationMenuItemReader.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationRepository.java`
  - `docs/issues/#128/frontend-api-handoff.md`
- What:
  - 점주 인증 사용자 전용 캘린더 조회 API를 추가했다.
  - 7일 날짜 스트립, 선택일 타임라인, 직원 컬럼, 미지정 컬럼, 예약 요약 응답을 조립했다.
  - 프론트 전달 문서를 제품 결정사항과 API 계약이 함께 보이도록 확장했다.
- Why:
  - 예약 관리 캘린더는 단순 목록 조회보다 더 많은 렌더링 정보를 요구하므로 전용 응답이 필요하다.
  - 프론트가 별도 확인 없이 화면 정책과 API 계약을 함께 읽을 수 있어야 한다.
- Verification:
  - `./gradlew compileJava`
- Next:
  - 후속 백로그 이슈에서 예약 상세 바텀시트, 액션, 이력, 자동 메시지를 분리 구현한다.
