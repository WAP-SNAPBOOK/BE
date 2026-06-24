# `#138 예약 상세 응답 일정/직원 필드 보강` 구현 계획

작성일: `2026-06-18`

## 목적

예약 수정 성공 후 프론트가 상세 바텀시트와 캘린더 블록을 갱신할 수 있도록 예약 상세 응답에 실제 시작시각과 담당 직원 정보를 포함한다.

## 포함

- `ReservationDetailResponse`에 `startAt`, `staffId`, `staffName` 추가
- 상세 조회 응답 builder에 해당 필드 매핑

## 제외

- 변경 이력 조회 API
- 직원 활성 상태 스키마 추가
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 상세 응답에 일정/직원 필드를 추가한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationDetailResponse.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: `./gradlew compileJava`
- 완료 조건: 상세 응답으로 `startAt`, `staffId`, `staffName`을 확인할 수 있다.
