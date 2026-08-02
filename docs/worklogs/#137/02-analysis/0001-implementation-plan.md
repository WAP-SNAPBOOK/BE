# `#137 예약 액션 시간/점유 안정화` 구현 계획

작성일: `2026-06-18`

## 목적

예약 확정/수정 시 소요시간과 시작시간 검증을 강화하고, 확정 예약 수정 중 점유 블록 교체가 기존 자기 블록과 충돌하지 않도록 정리한다.

## 포함

- 확정/수정 소요시간을 `30~180분`, `30분 단위`로 제한
- 확정/수정 시작시간을 `30분 단위`로 제한
- 예약 수정 시 새 점유 블록 충돌 검사에서 현재 예약의 기존 블록은 제외
- 충돌 검사를 통과한 뒤 기존 블록을 삭제하고 새 블록을 저장

## 제외

- 고객 예약 생성 시간 단위 변경
- 직원 활성 상태 스키마 추가
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 예약 액션 시간 검증을 강화한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationConfirmRequest.java`
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationUpdateRequest.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `src/main/java/com/example/easybooking/errors/errorcode/ReservationErrorCode.java`
- 검증: `./gradlew compileJava`
- 완료 조건: 0분/음수/180분 초과/30분 단위가 아닌 액션 요청이 거부된다.

### 변경 단위 2

- 목표: 예약 수정 시 점유 블록 교체 순서를 안전하게 만든다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/ReservationTimeBlockWriter.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationTimeBlockRepository.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: `./gradlew compileJava`
- 완료 조건: 수정 대상 예약의 기존 블록은 충돌 검사에서 제외하고, 통과 후 블록을 교체한다.
