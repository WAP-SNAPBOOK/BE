# `#999` AI 기반 도메인 정책 정리와 TDD Workflow 과밀화 회고 분석

작성일: `2026-04-11`

## 문서 목적

이 문서는 `PR #95` 작업 브랜치가 실제로는 `PR #96`으로 병합된 시점(`84bc6ff`, `2026-01-25`)부터 현재까지의 저장소 이력을 기준으로,

1. AI를 활용해 도메인 정책을 어떻게 정리했는지
2. TDD workflow 문서와 절차가 어떤 식으로 과밀화되었는지
3. 그 절차가 AI의 테스트/구현 산출물에 어떤 패턴을 만들었고 왜 문제가 되었는지
4. 이후 저장소가 어떤 식으로 대응했는지

를 한 문서로 정리한다.

이 문서는 감상이 아니라 저장소 아티팩트 기반 회고다.

---

## 분석 범위와 증거 경계

### 범위

- 기준 시작점: `84bc6ff` (`Merge pull request #96 from WAP-SNAPBOOK/feature/jiseob/#95-reservation-erd-v2-flyway`)
- 기준 종료점: `2026-04-11` 현재 `develop`
- 주요 관찰 대상:
  - `.cursor/`, `AGENTS.md`
  - `docs/issues/#97`, `#99`, `#101`, `#105`, `#109`, `#115`, `#999`
  - `src/test`, `src/main`
  - `git log`, `git show`, `git blame`, 현재 테스트 실행 결과

### 직접 증명 가능한 것

- 어떤 규칙/문서/테스트/구현이 언제 추가되었는지
- 어떤 문서 구조와 절차가 강제되었는지
- 어떤 테스트가 리플렉션, broad assertion, 내부 결합 검증을 사용하는지
- 어떤 커밋이 작은 단위였고 어떤 커밋이 넓은 단위였는지
- 현재 저장소가 그 문제를 완화하기 위해 어떤 규칙/스킬/문서를 추가했는지

### 직접 증명할 수 없는 것

- 특정 코드 줄이 "AI가 직접 작성한 코드"인지 여부

저장소만으로는 줄 단위 AI 작성 여부를 증명할 수 없다.
따라서 이 문서는 아래 두 가지를 분리해서 본다.

- `AI-assisted workflow 흔적`
  - AI용 규칙/스킬/문서 구조
  - AI와의 논의 로그
  - AI를 전제로 한 `go`, `plan.md`, `green-design` 문서 체계
- `실제 결과물`
  - 코드/테스트/문서/커밋 패턴

---

## 한 줄 결론

이 저장소는 `2026-01-26` 이후 한동안 AI를 활용해 도메인 정책을 문서로 정교하게 고정하는 데는 성공했지만,
그 과정에서 `문서 게이트 + 상태 머신형 TDD 절차`를 repo-wide로 과도하게 강제해 실제 테스트 작성과 최소 구현 루프를 무겁게 만들었고,
그 결과 일부 저가치 테스트, 임시 구현, 내부 결합 테스트, 문서-코드 드리프트가 발생했다.

이후 저장소는 이 문제를 `규칙 재구성(#999)`, `테스트 스위트 정비`, `다음 최소 RED 1건 중심 skill` 도입으로 완화하는 방향으로 대응했다.

---

## 타임라인

### 2026-01-25: 기준 시점

- `84bc6ff`: `feature/jiseob/#95-reservation-erd-v2-flyway`가 `PR #96`으로 병합됨
- 실커밋:
  - `920e26a`: `chore(flyway): add Flyway and enable migrations`
  - `96188bb`: `db(migration): add reservation ERD v2 migrations (V1-V3)`

### 2026-01-26: DocOps/TDD 규칙 대량 도입

- `f04cc7f`: `.cursor/rules`, `.cursor/skills` 18개 파일을 한 번에 추가
- `4522ad6`: 이슈 번호 없으면 작업을 중단하고 요청하도록 하는 gate 도입

이 시점부터 문서/TDD workflow가 repo-wide 규칙으로 강하게 올라간다.

### 2026-02-05 ~ 2026-02-14: `#97`, `#99`, `#101` 본격 전개

- `#97`: ERD v2 전환 + `go(RED/GREEN/REFACTOR)` + `green-design` + ADR
- `#99`: 메뉴/태그/입력값 + 길고 세밀한 TDD plan
- `#101`: 예약 가능 시간 관리 + AI 참여 discussion log + 596줄짜리 TDD plan

이 시기 문서 체계와 TDD workflow가 가장 촘촘하다.

### 2026-03-07 ~ 2026-03-08: 규칙 재구성 논의

- `docs/issues/#999/0001-rules-restructure-discussion-log.md`
- `docs/issues/#999/0002-agent-rules-restructure-options.md`
- `docs/issues/#999/0002-current-agent-rules-summary.md`

저장소가 스스로 "문서/TDD 상태 머신 강제" 구조가 무겁다고 분석하기 시작한다.

### 2026-03-22 ~ 2026-03-23: 테스트 스위트 정비 + 더 작은 TDD step으로 전환

- `docs/refactor/testing/2026-03-22-test-suite-hardening-plan.md`
- `docs/issues/#109/04-tdd/plan.md`

`#109`에서는 기존 phase/checklist형 계획 대신 "다음 최소 RED 1건" 형식이 등장한다.

### 2026-03-25 이후: 현재 규칙 체계로 재정렬

- `AGENTS.md`
- `.cursor/rules/30-implementation-discipline.mdc`
- `.cursor/skills/doc-tdd-plan/SKILL.md`

핵심 규칙이 "지금 필요한 가장 작은 다음 변경"과 `Structural` / `Behavioral` 분리로 이동한다.

### 2026-04-11 현재 상태

- `./gradlew test` 전체 통과
- 다만 리플렉션 사용 테스트와 저가치 테스트 일부는 아직 남아 있다

---

## 1. AI로 도메인 정책을 정리한 방식

### 1-1. 저장소에 남은 가장 직접적인 흔적: AI 참여 논의 로그

`docs/issues/#101/03-adr/discussion-log.md`는 참여자를 아래처럼 명시한다.

- `User (요구사항 제공)`
- `AI Assistant (설계/분석)`

여기서 AI는 코드를 바로 쓰기보다 정책 결정을 구조화하는 역할을 했다.

#### `#101`에서 AI가 정리한 정책 예시

- 슬롯 생성 방식: 물리 슬롯이 아니라 계산 기반
- 운영 시간 구조: 요일별 row + 복수 블록 허용
- 휴무일 모델: 매장 레벨 중심
- 운영 시간 패턴: `DAILY / WEEKDAY_WEEKEND / BY_DAY`
- 월 응답 구조: `availableDates / holidayDates / closedDates`

즉 AI는 구현 디테일보다 먼저 "어떤 개념을 어떤 규칙으로 고정할지"를 문서로 정리하는 역할을 수행했다.

근거:

- `docs/issues/#101/03-adr/discussion-log.md`
- `docs/issues/#101/03-adr/adr-101-availability.md`
- `docs/issues/#101/02-analysis/analysis-options.md`

### 1-2. `#97`에서도 정책이 먼저 고정됐다

`#97`은 단순 기능 구현이 아니라 ERD v2 전환 이슈였고, 아래 정책이 문서로 먼저 정리됐다.

- 점유 granularity를 `10분`으로 볼지 `30분`으로 볼지
- confirm 시 reschedule을 허용할지
- default staff를 어떤 이름/정책으로 둘지
- unique constraint 충돌을 어떻게 예외 매핑할지

근거:

- `docs/issues/#97/03-adr/0001-adr-occupancy-granularity-10min.md`
- `docs/issues/#97/03-adr/0002-adr-confirm-reschedule-at-confirm.md`
- `docs/issues/#97/03-adr/0003-adr-default-staff-owner-name-and-first-staff.md`
- `docs/issues/#97/04-tdd/0002-exception-mapping-unique-constraint.md`

### 1-3. `#109`에서는 아예 "문서로 전제를 뒤집고 시작"했다

`docs/issues/#109/work-log.md`와 `docs/issues/#109/commit-unit-flow.md`를 보면,
태그 작업은 조회/정렬 구현보다 먼저 도메인 전제부터 다시 정의했다.

- `global tags + shop_tag_orders` 안 폐기
- `shop_tags` 기반 매장 로컬 태그 모델 채택
- 첫 TDD 시작점도 정렬 조회가 아니라 `매장 스코프 태그 생성 invariant`로 재설정

이건 AI가 강한 도메인 전제 변경을 다룰 때 "문서로 invariants를 먼저 고정"하는 방식으로 작동했음을 보여준다.

### 1-4. 평가

도메인 정책 정리 관점에서 AI workflow는 분명한 성과가 있었다.

- 구현 이전에 정책 충돌을 드러냈다
- ADR / discussion log / design plan을 통해 팀이 공유 가능한 결정 기록을 남겼다
- 전역 태그 vs 매장 로컬 태그처럼 설계 전제 자체가 바뀌는 작업에 특히 유효했다

즉 AI의 강점은 "테스트를 하나씩 쓰는 자동화"보다 "도메인 개념과 정책을 대화형으로 구조화하는 것"에 더 가깝게 나타났다.

---

## 2. TDD workflow 문서가 과밀화된 방식

### 2-1. 규칙이 repo-wide로 너무 높은 레이어에 올라갔다

`2026-01-26`에 도입된 규칙은 단순 스타일 규칙이 아니라 workflow 자체를 강제했다.

대표 예시:

- `.cursor/rules/10-docops-workflow.mdc`
  - 코드/테스트 변경 전에 문서 산출물 먼저
  - 이슈 번호 없으면 중단
  - 이슈 기반 문서 루트 강제
- `.cursor/rules/20-tdd-kentbeck-plan-go.mdc`
  - `plan.md` SSOT
  - `go(RED/GREEN/REFACTOR)` 프로토콜
- `.cursor/rules/40-git-pr-docs.mdc`
  - PR 문서 산출물 기본화

이 구조는 "복잡한 작업에서 선택적으로 쓰는 workflow"가 아니라 "기본 규칙"으로 올라간 상태였다.

### 2-2. 같은 프로토콜이 여러 곳에서 반복됐다

`docs/issues/#999/0002-agent-rules-restructure-options.md`는 아래를 명시한다.

- `동의 / go / plan.md` 프로토콜이
  - `.cursor/rules/10-docops-workflow.mdc`
  - `.cursor/rules/20-tdd-kentbeck-plan-go.mdc`
  - `.cursor/rules/my-custom-rules.mdc`
  - `AGENTS.md`
  에 반복된다

즉 절차가 한 곳에서만 정의된 것이 아니라 여러 파일에 중복되어 agent 행동을 계속 같은 방향으로 밀었다.

### 2-3. 이슈별 문서 묶음이 커졌다

주요 이슈 문서량:

| 이슈 | 파일 수 | 줄 수 | 특징 |
|------|--------:|------:|------|
| `#97` | 26 | 2127 | intake, analysis, ADR, TDD, go-logs, green-design, PR draft |
| `#101` | 11 | 1854 | scan, analysis, ADR, discussion-log, TDD, green, worklog, PR, cross-issue audit |
| `#109` | 9 | 2872 | 설계/워크로그/코드 해설/커밋 흐름 설명까지 포함 |
| `#99` | 4 | 652 | issue, design, TDD, PR draft |

여기서 중요한 건 문서 개수 자체보다 문서의 역할이 세분화되어 있었다는 점이다.

예를 들어 `#97`에는 아래가 동시에 존재했다.

- `00-intake`
- `02-analysis`
- `03-adr`
- `04-tdd/plan.md`
- `04-tdd/go-logs/*`
- `05-green-design/*`
- `07-pr/*`

### 2-4. `plan.md` 자체도 긴 checklist/phase 문서였다

TDD plan 길이:

| 문서 | 줄 수 | 특징 |
|------|------:|------|
| `docs/issues/#97/04-tdd/plan.md` | 215 | Phase 0~2 중심 |
| `docs/issues/#99/04-tdd/plan.md` | 389 | 메뉴/태그/입력값 전체 시나리오 체크리스트 |
| `docs/issues/#101/04-tdd/plan.md` | 596 | 엔티티/리더/서비스/API/캘린더까지 장문 체크리스트 |
| `docs/issues/#105/04-tdd/plan.md` | 82 | 상대적으로 작음 |
| `docs/issues/#109/04-tdd/plan.md` | 44 | 이후의 경량화 버전 |

특히 `#97`, `#99`, `#101`은 "다음 테스트 1건"이 아니라 큰 phase와 세부 체크리스트를 한 문서 안에 오래 유지하는 구조였다.

### 2-5. RED/GREEN/REFACTOR 외에 보조 문서가 더 붙었다

`#97`만 해도 아래 보조 문서가 붙었다.

- `go-logs` 6개
- `green-design` 4개

즉 실제 loop는 단순히

- 테스트 추가
- 실패 확인
- 최소 구현
- 리팩터링

이 아니라, 종종 아래처럼 확장됐다.

1. 이슈 번호 확인
2. 문서 루트 생성
3. analysis / ADR / TDD plan 작성
4. `go(RED)` 문서
5. 테스트 추가
6. 실패 확인
7. `green-design` 문서
8. 구현
9. worklog
10. PR 초안

이 구조가 저장소 회고 문서에서 "문서/TDD 상태 머신 강제"라고 불린 이유다.

---

## 3. 절차 과잉이 AI 구현에 만든 패턴

이 섹션은 "AI가 직접 작성했다"는 뜻이 아니라,
AI-assisted workflow 하에서 실제 산출물에 반복적으로 나타난 패턴을 정리한다.

### 3-1. RED를 위해 프로덕션에 임시 구현을 먼저 넣는 패턴

문서 규칙은 `RED 단계 임시 구현 허용`을 명시했다.

대표 근거:

- `docs/issues/#97/04-tdd/plan.md`
- `docs/issues/#99/04-tdd/plan.md`
- `docs/issues/#97/04-tdd/go-logs/2026-02-07-go-red-1-e-1.md`

`go-red` 문서는 아예 아래 접근을 예시로 든다.

- 테스트가 컴파일되도록 엔티티/리포지토리/Writer를 먼저 만든다
- Writer는 실제 저장하지 않게 두어 RED를 만든다

이 패턴은 실제 커밋에도 나타난다.

#### 사례 A. `b8113b8`

- 커밋명: `test(staff): add failing test for default staff lookup`
- 실제 변경:
  - 테스트 추가
  - 동시에 `src/main/java/com/example/easybooking/staff/StaffReader.java`에
    `getDefaultStaffByShopId()`를 추가하고 `UnsupportedOperationException("TODO")`를 넣음

즉 `test(...)` 커밋이지만 프로덕션 TODO stub을 같이 도입했다.

#### 사례 B. `7c13516`

- 커밋명: `test(reservation): add failing JPA mapping test for startAt`
- 실제 변경:
  - 테스트 추가
  - 동시에 `Reservation`에 `startAt` 필드와 `setStartAt()`를 넣고,
    setter 본문은 `// TODO: go(GREEN)` 상태로 둠

이건 "테스트를 위해 프로덕션 구조를 먼저 열어두는" 전형적인 임시 구현 패턴이다.

### 3-2. Reflection 금지 문서와 실제 테스트 작성이 어긋났다

`#97`, `#99` TDD plan은 모두 `Reflection 금지`를 명시했다.
하지만 현재 `develop`에는 아래 테스트가 여전히 리플렉션을 사용한다.

- `src/test/java/com/example/easybooking/availability/HolidayCheckerTest.java`
- `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
- `src/test/java/com/example/easybooking/availability/presentation/AvailabilityControllerIntegrationTest.java`
- `src/test/java/com/example/easybooking/reservation/service/ReservationServiceCreateReservationNewFieldsUnitTest.java`
- `src/test/java/com/example/easybooking/reservation/service/ReservationServiceDualWriteTest.java`
- `src/test/java/com/example/easybooking/user/service/UserCleanupServiceTest.java`

현재 기준 집계:

- `ReflectionTestUtils` 참조: `7`
- `setAccessible(true)` 참조: `3`
- `getDeclaredField(...)` 참조: `3`

즉 workflow 문서는 "public API만 테스트, reflection 금지"를 강하게 말했지만,
실제 테스트 구현은 내부 필드 조작으로 자주 돌아갔다.

### 3-3. 저가치 또는 신호가 약한 테스트가 남았다

`docs/refactor/testing/2026-03-22-test-suite-hardening-plan.md`는 아래 테스트들을 재평가 대상으로 직접 적었다.

- `ReservationConfirmRequestValidationTest`
- `EasybookingApplicationTests`
- `ReservationCreateStartAtDualWriteRedTest`

현재 파일 상태:

- `ReservationConfirmRequestValidationTest`
  - JSON 역직렬화만 수행
  - assertion 없음
- `EasybookingApplicationTests`
  - 빈 `contextLoads()`만 보유
- `ReservationCreateStartAtDualWriteRedTest`
  - 이름 자체가 `RedTest`

이건 저장소 스스로도 일부 테스트를 "보호 가치가 낮거나 stale할 수 있음"으로 인식했음을 보여준다.

### 3-4. "가장 작은 다음 변경" 문구와 실제 커밋 크기가 자주 어긋났다

작은 단위 커밋도 있었지만, 특히 `#99`, `#101`에서는 아래처럼 넓은 커밋이 반복됐다.

#### 사례 A. `404ffd3`

- `feat(shop): add ShopMenu CRUD service and controller`
- 7 files, 269 insertions
- 엔티티/DTO/컨트롤러/서비스/테스트를 한 번에 도입

#### 사례 B. `3117017`

- `feat(shop): add ShopMenuInputField CRUD service and controller`
- 8 files, 354 insertions

#### 사례 C. `1f11199`

- `feat(availability): add customer daily/monthly availability APIs`
- 8 files, 568 insertions
- 서비스/DTO/컨트롤러/설정/대형 integration test를 한 번에 포함

반면 `#97` 초기에는 작은 test-first 커밋도 꽤 존재했다.
따라서 정확한 표현은 "항상 넓었다"가 아니라
"작게 끊으려는 규칙은 있었지만, 실제로는 넓은 커밋이 자주 발생했다"다.

### 3-5. 문서 기준 workflow와 실제 구현 흐름이 종종 어긋났다

대표 사례:

- `docs/issues/#97/03-adr/0002-adr-confirm-reschedule-at-confirm.md`
  - 사용자 요청으로 `TDD(새 테스트 추가) 없이 구현만 진행`했다고 명시
- `docs/issues/#101/08-analysis/0002-cross-issue-user-flow-audit.md`
  - `#97/#99` TDD plan의 진행 상태 표와 본문 체크 불일치를 지적

즉 문서는 강한 상태 머신을 전제했지만, 실제 구현은 중간중간 우회하거나 다른 흐름으로 진행됐다.

---

## 4. 왜 이것이 문제였는가

### 4-1. 테스트보다 절차 관리 비용이 커졌다

문제의 핵심은 "문서가 많다"가 아니라,
"테스트 하나를 추가하기 전에 거쳐야 하는 메타 작업이 많아졌다"는 점이다.

이 구조에서는 AI가 다음을 자주 수행하게 된다.

- 현재 phase/step 복원
- 관련 문서 경로 탐색
- `go` 상태 확인
- `green-design` 또는 worklog 작성
- PR 문서까지 맞추기

그 결과 실제 테스트 설계에 쓰여야 할 집중력이 문서 상태 관리로 분산된다.

### 4-2. RED를 만들기 위한 임시 구현이 프로덕션 설계를 오염시킬 수 있다

`UnsupportedOperationException("TODO")`, 빈 setter, 저장 안 하는 Writer처럼
"컴파일을 위한 임시 구조"를 프로덕션에 먼저 넣는 패턴은 다음 위험이 있다.

- 실제 도메인 경계가 아닌 "RED를 만들기 쉬운 형태"로 구조가 열릴 수 있다
- setter, TODO stub 같은 임시 API가 그대로 남을 수 있다
- 최소 도메인 모델링보다 "테스트를 실행시키는 구조"가 먼저 굳을 수 있다

### 4-3. 내부 필드 조작 테스트는 리팩터링 저항을 키운다

`ReflectionTestUtils`, `setAccessible(true)` 기반 테스트는 다음 문제를 만든다.

- private/internal field 이름에 테스트가 묶인다
- public contract가 아니라 내부 상태 조작으로 테스트를 통과시킨다
- 리팩터링 시 테스트가 실제 행위 회귀보다 구조 변화에 민감하게 깨진다

저장소가 나중에 `리플렉션 제거`를 정비 목표로 삼은 이유도 여기에 있다.

### 4-4. 저가치 테스트가 쌓이면 보호 신호가 약해진다

assertion 없는 역직렬화 테스트, 빈 context load, 이름이 `RedTest`인 잔존 테스트는
개별적으로는 큰 해가 없어 보일 수 있지만, 합치면 다음 문제가 생긴다.

- 실패했을 때 의미가 약하다
- 전체 스위트가 녹색이어도 실제 보호 범위를 과대평가하게 만든다
- 새로운 RED/GREEN 사이클에서 기존 잡음을 섞는다

`docs/refactor/testing/2026-03-22-test-suite-hardening-plan.md`가
"기존 실패와 신규 실패가 섞인다"고 적은 이유가 이 맥락이다.

### 4-5. 문서-코드 드리프트가 생기면 SSOT가 약해진다

강한 `plan.md SSOT` 구조는 plan이 정확할 때만 의미가 있다.
하지만 저장소에는 아래 드리프트가 남았다.

- `#97/#99` TDD plan의 상태 표와 본문 불일치
- `#97` 문서 내 시간 단위 정책 충돌
- 테스트 정비 문서상 제거 완료라고 적힌 항목이 현재 코드에는 일부 남아 있음

즉 문서량이 많아질수록 문서 정확성 유지 비용도 커졌다.

---

## 5. 저장소는 이후 어떻게 대응했는가

### 5-1. 규칙 재구성: workflow를 기본 강제에서 선택형으로 이동

`#999` 문서 묶음은 문제를 명시적으로 진단하고 대응 방향을 정리한다.

- 문제 인식:
  - 현재 규칙이 간단한 구현까지 지나치게 무겁게 만든다
  - 문서 게이트, 승인 키워드, `go` 기반 TDD, 학습 문서, PR 문서화가 repo-wide로 묶여 있다
- 추천 대응:
  - repo-wide 최소화
  - workflow는 skill/prompt file로 분리
  - `issue-first`, `go`, `plan.md`, study 문서화, PR 초안은 필요 시 호출

핵심 문서:

- `docs/issues/#999/0001-rules-restructure-discussion-log.md`
- `docs/issues/#999/0002-agent-rules-restructure-options.md`
- `docs/issues/#999/0002-current-agent-rules-summary.md`

### 5-2. 테스트 스위트 자체를 정비 대상으로 삼았다

`docs/refactor/testing/2026-03-22-test-suite-hardening-plan.md`는
새 테스트 추가보다 먼저 기준선 복구를 목표로 삼는다.

핵심 진단:

- `./gradlew test`가 `compileTestJava` 단계에서 실패하던 시점이 있었다
- brittle/저가치 테스트가 있다
- `ReflectionTestUtils` 사용 테스트가 있다
- 긴 controller integration test, interaction coupling이 과하다

이 문서는 "TDD를 더 밀어붙이기 전에 테스트 스위트 품질을 복구해야 한다"는 저장소의 자가 진단이다.

### 5-3. TDD 문서도 "다음 최소 RED 1건" 중심으로 줄였다

후기 구조에서는 `#109` plan과 현재 `doc-tdd-plan` skill이 같은 방향을 취한다.

- `docs/issues/#109/04-tdd/plan.md`
  - `Next Smallest RED (exactly one)`
- `.cursor/skills/doc-tdd-plan/SKILL.md`
  - 체크리스트 완성이 아니라 "다음 최소 테스트 1건" 추천
  - reflection, broad assertion, stale test를 명시적 리스크로 취급

즉 저장소는 TDD를 포기한 것이 아니라,
"장문 phase/checklist 기반 TDD"에서 "작은 다음 스텝 기반 TDD"로 이동했다.

### 5-4. 현재 기본 규칙은 절차보다 변경 단위에 초점을 둔다

현재 `AGENTS.md`와 구현 규칙은 아래로 재정렬돼 있다.

- `지금 필요한 가장 작은 다음 변경`
- `Structural` / `Behavioral` 분리
- 작은 논리 단위로 커밋
- 날짜는 런타임 확인
- 반복 CLI 오류 시 사용자 조치 요청

즉 대응의 핵심은 "문서를 없애기"가 아니라,
"절차 상태 머신을 repo-wide 기본 규칙에서 내리고, 품질과 변경 단위에 집중하는 것"이었다.

---

## 6. 현재 상태 평가

### 좋아진 점

- `./gradlew test` 전체 스위트가 현재 통과한다
- 기본 규칙이 훨씬 짧고 명확해졌다
- 도메인 정책 정리용 문서 workflow는 여전히 남아 있지만 기본 강제가 아니다
- TDD skill이 "다음 최소 RED 1건" 중심으로 바뀌었다

### 아직 남아 있는 흔적

- 리플렉션 기반 테스트가 아직 존재한다
- 저장소 스스로 저가치라 지목한 테스트가 일부 남아 있다
- 과거 문서와 현재 코드 사이의 잔존 드리프트가 완전히 청소되지는 않았다

즉 대응은 진행됐지만, 완전한 cleanup까지 끝난 상태는 아니다.

---

## 종합 결론

이 저장소에서 AI는 도메인 정책 정리에는 실제로 강했다.

- `#101`의 availability 정책
- `#97`의 occupancy / confirm / default staff 정책
- `#109`의 전역 태그 -> 매장 로컬 태그 전제 전환

같은 문제는 문서 기반 논의와 ADR을 통해 선명하게 정리됐다.

반면 초기 TDD workflow는 다음 문제가 있었다.

- workflow가 repo-wide 기본 규칙으로 올라가 있었다
- 문서 묶음과 상태 관리가 과도했다
- RED를 위해 임시 구현을 먼저 넣는 패턴이 생겼다
- reflection 금지 규칙과 실제 테스트 구현이 어긋났다
- 일부 저가치 테스트와 plan drift가 누적됐다
- "가장 작은 다음 변경" 문구에 비해 실제 구현 단위는 자주 넓었다

저장소의 대응은 일관적이다.

- 문제를 `#999`에서 규칙 구조 문제로 진단했고
- 테스트 스위트를 별도 정비 대상으로 분리했으며
- 현재는 "가벼운 기본 규칙 + 선택형 workflow + 다음 최소 RED 1건" 구조로 이동했다

따라서 이 회고의 가장 정확한 문장은 다음이다.

> AI는 이 저장소에서 도메인 정책을 정리하고 설계 전제를 고정하는 데는 높은 효용을 보였지만,
> 초기의 과도한 문서/TDD 상태 머신과 결합되었을 때는 실제 테스트-구현 루프를 무겁게 만들었고,
> 그 결과 일부 임시 구현, 내부 결합 테스트, 저가치 테스트, 문서-코드 드리프트가 발생했다.

---

## 관련 문서

- `docs/issues/#97/04-tdd/plan.md`
- `docs/issues/#97/04-tdd/go-logs/`
- `docs/issues/#97/05-green-design/`
- `docs/issues/#97/03-adr/`
- `docs/issues/#99/04-tdd/plan.md`
- `docs/issues/#99/07-pr/0001-pr-draft.md`
- `docs/issues/#101/03-adr/discussion-log.md`
- `docs/issues/#101/04-tdd/plan.md`
- `docs/issues/#101/06-worklog/worklog.md`
- `docs/issues/#101/08-analysis/0002-cross-issue-user-flow-audit.md`
- `docs/issues/#109/04-tdd/plan.md`
- `docs/issues/#109/work-log.md`
- `docs/issues/#109/commit-unit-flow.md`
- `docs/issues/#999/0001-rules-restructure-discussion-log.md`
- `docs/issues/#999/0002-agent-rules-restructure-options.md`
- `docs/issues/#999/0002-current-agent-rules-summary.md`
- `docs/refactor/testing/2026-03-22-test-suite-hardening-plan.md`
- `.cursor/skills/doc-tdd-plan/SKILL.md`
- `AGENTS.md`
