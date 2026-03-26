## 테스트 이름 규칙 정리 (phase label 금지)

- 날짜: 2026-02-07
- 이슈: `#97`
- 배경: `ReservationTimeBlockWriterRedTest`처럼 테스트 이름에 `Red/Green/Refactor` 같은 **TDD 단계 표기**가 들어가면,
  - 시간이 지나면 의미가 퇴색하고
  - 테스트가 “무슨 행동을 검증하는지”가 이름에서 드러나지 않는다.

---

### 1) 어떻게 변경할지 (설계/접근 방식)

- 테스트 클래스/메서드 이름을 **행위/결과 중심**으로 변경한다.
  - 예: `ReservationTimeBlockWriterRedTest` → `ReservationTimeBlockWriterTest`
  - 예: `...Red...`/`...Green...`/`...Refactor...` 등의 단계 표기는 제거한다.
- 강제 규칙으로 `.cursor/rules/my-custom-rules.mdc`에 다음을 추가한다.
  - **테스트 이름에 TDD phase label 금지**
  - **이름은 무엇을(행위) 왜(조건) 어떻게(결과) 검증하는지 드러내기**

---

### 2) 변경 근거 (이유, 장단점)

- 장점
  - 테스트가 읽히는 순간, 검증하는 행위/계약이 드러난다.
  - 단계(RED/GREEN)는 커밋/문서/로그로 추적하고, 테스트 이름은 “영구적 계약”으로 유지된다.
- 주의
  - 파일명과 클래스명은 Java 규칙상 일치해야 하므로 **리네임 시 파일도 함께 변경**한다.

---

### 3) 코드 수정 예시 (diff 또는 예시 코드)

```diff
- class ReservationTimeBlockWriterRedTest {
+ class ReservationTimeBlockWriterTest {
   @Test
   void saveAll_persistsAllBlocksAndAssignsIds() { ... }
 }
```

---

### 4) 변경 후 예상 결과 (동작, 성능, 영향 범위)

- 동작: 변경 없음(이름만 변경)
- 영향 범위: 테스트 파일 리네임 + 룰 문서 1곳 업데이트

