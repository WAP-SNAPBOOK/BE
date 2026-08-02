# `#138 예약 상세 응답 일정/직원 필드 보강` 작업 로그

작성일: `2026-06-18`

## Entries

### Entry 001

- Date: `2026-06-18 09:00`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `docs/issues/#138/02-analysis/0001-implementation-plan.md`
- What:
  - 예약 상세 응답 보강 범위를 별도 이슈로 분리했다.
- Why:
  - 수정 API 성공 응답만으로 프론트가 담당 직원과 실제 시작시각을 갱신하기 어렵다.
- Verification:
  - 문서 작성
- Next:
  - 상세 응답 DTO와 service 매핑을 수정한다.

### Entry 002

- Date: `2026-06-18 09:10`
- Unit: `reservation-detail-fields`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationDetailResponse.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- What:
  - 예약 상세 응답에 `startAt`, `staffId`, `staffName`을 추가했다.
  - 담당 직원이 없거나 조회되지 않으면 `staffName=null`로 응답하도록 처리했다.
- Why:
  - 수정 API 성공 응답만으로 프론트가 상세 바텀시트와 캘린더 블록을 다시 그릴 수 있어야 한다.
- Verification:
  - `./gradlew compileJava`
- Next:
  - 없음.
