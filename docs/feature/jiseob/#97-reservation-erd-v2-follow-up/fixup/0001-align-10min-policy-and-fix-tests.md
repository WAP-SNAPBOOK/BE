## #97 구현물 정리: 10분 단위 정책 정렬 + 깨진 테스트 수정

- 날짜: 2026-02-07
- 브랜치: `feature/jiseob/#97-reservation-erd-v2-follow-up`
- 배경: “원장 reschedule 허용” 요구 → 점유 최소 단위 **10분**으로 결정(ADR-0001)
  - ADR: `docs/issues/#97/03-adr/0001-adr-occupancy-granularity-10min.md`

---

## 1) 어떻게 변경할지 (설계/접근 방식)

### A. 시간 입력 최소 단위를 10분으로 정렬

- 예약 생성(`ReservationService.createReservation`)의 시간 검증을 **30분 → 10분**으로 변경한다.
- 검증은 저장/이벤트 발행 전에 수행한다(불필요한 save/rollback 방지).
- 에러 메시지(에러코드)도 “30분” → “10분”으로 정렬한다.

### B. 통합 테스트 실패 원인 제거(필수 입력/시간 형식 정렬)

- `ReservationChatPublishIntegrationTest`에서:
  - `staffId`를 세팅하지 않아(현재 구현상 필수) `ReservationException`이 발생한다.
  - `time=17:02`는 “10분 단위 정책”에도 맞지 않으므로 `time=17:10`(또는 `17:00`)으로 수정한다.
  - Shop 생성 시 자동 생성되는 기본 Staff를 `StaffRepository`로 조회해 `ReservationCreateRequest.staffId`에 세팅한다.

### C. 샘플 테스트 데이터(ReservationTimeBlock)도 10분 단위로 예시 정렬(선택)

- `ReservationTimeBlockWriterTest`의 예시 시간을 14:30 대신 10분 단위(예: 14:10/14:20)로 바꿔 “최소 단위=10분”을 드러낸다.

---

## 2) 변경 근거 (이유, 장단점)

- **이유**
  - 최소 단위가 10분이라면, “입력(time)”과 “점유 단위”가 엇갈리지 않도록 **검증과 메시지**를 일관되게 맞춰야 한다.
  - 현재 통합 테스트는 구현의 필수 입력(staffId) 정책을 따라가지 않아 실패한다.
- **장점**
  - 정책(10분 단위)과 코드/테스트가 정렬돼, 실패가 “진짜 결함”일 때만 발생한다.
  - 검증을 저장 전에 수행해 불필요한 DB 작업을 줄인다.
- **주의**
  - 고객 예약 UI/프론트가 아직 30분 단위를 가정하고 있어도, 30분은 10분의 배수이므로 호환은 유지된다.

---

## 3) 코드 수정 예시 (diff 또는 예시 코드)

### `ReservationService` 시간 검증(30 → 10)

```diff
- private void validateTimeIsOn30MinuteBoundary(LocalTime time) {
-     if (time.getMinute() % 30 != 0 || time.getSecond() != 0 || time.getNano() != 0) {
-         throw new ReservationException(ReservationErrorCode.INVALID_TIME_INTERVAL);
-     }
- }
+ private void validateTimeIsOn10MinuteBoundary(LocalTime time) {
+     if (time.getMinute() % 10 != 0 || time.getSecond() != 0 || time.getNano() != 0) {
+         throw new ReservationException(ReservationErrorCode.INVALID_TIME_INTERVAL);
+     }
+ }
```

### `ReservationChatPublishIntegrationTest` staffId/time 정렬

```diff
 formData.put("time", "17:10");

 // shop 생성 시 기본 staff 자동 생성됨 → staffId 세팅
 Long staffId = staffRepository.findByShopIdAndName(shopId, "기본")
     .orElseThrow()
     .getId();
 req.setStaffId(staffId);
```

---

## 4) 변경 후 예상 결과 (동작, 성능, 영향 범위)

- `./gradlew test`가 현재 실패 1건(`ReservationChatPublishIntegrationTest`) 포함해 **전체 통과**한다.
- 예약 생성 시 `time`이 10분 단위가 아니면 400(또는 비즈니스 예외)로 거절된다.
- 영향 범위
  - `ReservationService`의 시간 검증 로직(동작 변경)
  - 테스트 데이터/통합 테스트 수정(테스트 안정화)

---

## 5) 변경 대상 파일(예정)

- `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- `src/main/java/com/example/easybooking/errors/errorcode/ReservationErrorCode.java`
- `src/test/java/com/example/easybooking/ReservationChatPublishIntegrationTest.java`
- `src/test/java/com/example/easybooking/reservation/ReservationTimeBlockWriterTest.java` (선택)

