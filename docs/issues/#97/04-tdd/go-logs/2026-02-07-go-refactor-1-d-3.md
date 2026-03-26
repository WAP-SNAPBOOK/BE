## go(REFACTOR) 준비 — #97 / 1-D-3 `time` 30분 단위 검증 리팩터링

- 날짜: 2026-02-07
- 이슈: `#97`
- plan 항목: `docs/issues/#97/04-tdd/plan.md`의 **1-D-3**
- 목표(REFACTOR): **테스트가 계속 통과하는 범위에서 구조만 정리**한다. (동작 변경 금지)

---

## 1) 어떻게 변경할지 (설계/접근 방식)

### 리팩터링 범위(구조 변경만)

- `ReservationService.createReservation()` 내부의 “30분 단위 검증” 조건식을 **private 메서드로 추출**한다.
  - 예: `validateTimeIsOn30MinuteBoundary(LocalTime time)`
- 테스트 파일의 **사용하지 않는 import 제거**(정적 분석 경고 제거).

### 변경 대상 파일

- `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- `src/main/java/com/example/easybooking/errors/errorcode/ReservationErrorCode.java` (변경 없음, 확인만)
- `src/test/java/com/example/easybooking/reservation/service/ReservationServiceCreateReservationStaffIdValidationTest.java`

---

## 2) 변경 근거 (이유, 장단점)

- **이유**
  - 조건식이 길어서 핵심 의도(“30분 단위 강제”)가 흐려진다.
  - 동일 정책이 다른 흐름(확정/리스케줄 등)으로 확대될 가능성이 높아, 재사용 가능한 형태로 준비해두는 편이 유지보수에 유리하다.
  - 테스트의 unused import는 의미 없는 잡음이므로 제거한다.

- **장점**
  - `createReservation()`의 읽기 흐름이 단순해진다.
  - 정책 검증 로직이 한 곳에 모여 수정 포인트가 명확해진다.

- **단점/주의**
  - (의도적으로) 예외 발생 시점/순서를 바꾸지 않기 위해, **검증 호출 위치는 그대로 유지**한다.

---

## 3) 코드 수정 예시 (diff 또는 예시 코드)

### (예시) 검증 로직 추출

```diff
// ReservationService.java
Reservation savedReservation = reservationWriter.save(newReservation);

- if (time.getMinute() % 30 != 0 || time.getSecond() != 0 || time.getNano() != 0) {
-     throw new ReservationException(ReservationErrorCode.INVALID_TIME_INTERVAL);
- }
+ validateTimeIsOn30MinuteBoundary(time);

// ...
+
+ private void validateTimeIsOn30MinuteBoundary(LocalTime time) {
+     if (time.getMinute() % 30 != 0 || time.getSecond() != 0 || time.getNano() != 0) {
+         throw new ReservationException(ReservationErrorCode.INVALID_TIME_INTERVAL);
+     }
+ }
```

### (예시) 테스트 unused import 제거

```diff
- import java.util.List;
```

---

## 4) 변경 후 예상 결과 (동작, 성능, 영향 범위)

- 동작: **변경 없음**  
  - 기존과 동일하게 `time=14:10`이면 `ReservationException(INVALID_TIME_INTERVAL)` 발생
  - 기존 테스트들이 그대로 통과
- 성능: 영향 없음(메서드 호출 1회 수준)
- 영향 범위: `ReservationService` 내부 구조 + 테스트 import 정리 수준

---

## 5) 실행/검증 계획

- `ReservationServiceCreateReservationStaffIdValidationTest` 전체 또는 최소 `1-D-3` 관련 테스트 1개 실행으로 통과 확인

