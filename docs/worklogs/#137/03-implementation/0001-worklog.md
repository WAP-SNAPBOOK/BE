# `#137 예약 액션 시간/점유 안정화` 작업 로그

작성일: `2026-06-18`

## Entries

### Entry 001

- Date: `2026-06-18 09:00`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `docs/issues/#137/02-analysis/0001-implementation-plan.md`
- What:
  - 예약 액션 검증과 점유 블록 교체 안정화 범위를 별도 이슈로 분리했다.
- Why:
  - 컴파일은 통과하지만 런타임에서 잘못된 소요시간이나 자기 블록 충돌이 생길 수 있어 별도 수정 단위가 필요하다.
- Verification:
  - 문서 작성
- Next:
  - 시간 검증과 점유 블록 교체 로직을 수정한다.

### Entry 002

- Date: `2026-06-18 09:10`
- Unit: `reservation-action-hardening`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/errors/errorcode/ReservationErrorCode.java`
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationConfirmRequest.java`
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationUpdateRequest.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationTimeBlockRepository.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationTimeBlockWriter.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- What:
  - 예약 확정/수정 소요시간을 `30~180분`, `30분 단위`로 제한했다.
  - 예약 확정/수정 시작시간을 `30분 단위`로 검증하도록 보강했다.
  - 예약 수정 시 현재 예약의 기존 점유 블록은 충돌 검사에서 제외하고, 통과 후 기존 블록을 삭제/flush한 뒤 새 블록을 저장하도록 바꿨다.
- Why:
  - 0분/음수 소요시간이 점유 블록 없이 통과할 수 있었고, 수정 시 자기 기존 블록과 충돌할 여지가 있었다.
- Verification:
  - `./gradlew compileJava`
- Next:
  - 없음.
