## 예약 확정 durationMinutes 10분 단위 검증 (1-F-1a)

- 날짜: 2026-02-07
- 이슈: `#97`
- 근거 SSOT: `docs/issues/#97/04-tdd/plan.md`의 **1-F-1a**

---

### 1) 어떻게 변경할지 (설계/접근 방식)

- 검증은 **요청 DTO(`ReservationConfirmRequest`)에서 Bean Validation으로 수행**한다.
- 단위는 “10의 배수”로 고정하고, `null/0/음수` 정책은 이번 단계에서 결정하지 않는다.
  - 따라서 구현은 **`durationMinutes == null`이면 통과**로 둔다.
- RED 단계 테스트는 컨트롤러까지 가지 않고, `jakarta.validation.Validator`로 DTO를 직접 검증한다.

---

### 2) 변경 근거 (이유, 장단점)

- **이유**: 확정 시 생성되는 점유 블록이 10분 단위이므로, 입력값도 동일한 단위로 정렬되어야 한다.
- **장점**: 요청 경계에서 빠르게 실패시키고, 서비스/도메인 로직을 단순하게 유지할 수 있다.
- **단점**: `@AssertTrue`는 violation 경로가 필드가 아니라 메서드로 잡힐 수 있다(필드 단위 메시지가 필요하면 커스텀 constraint 도입 고려).

---

### 3) 코드 수정 예시 (diff 또는 예시 코드)

```java
@AssertTrue(message = "예약 기간은 10분 단위(10, 20, 30, ...)만 허용됩니다.")
public boolean isDurationMinutesOn10MinuteBoundary() {
  if (durationMinutes == null) return true;
  return durationMinutes % 10 == 0;
}
```

---

### 4) 변경 후 예상 결과 (동작, 성능, 영향 범위)

- `durationMinutes=45`로 확정 요청 시, `@Valid`에서 검증 실패 → 400(검증 실패)로 차단
- `durationMinutes=10/20/30/60...`는 통과
- 영향 범위는 DTO + 테스트로 제한

