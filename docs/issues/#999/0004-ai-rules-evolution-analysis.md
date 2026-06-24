# `#999` AI Agent Rules 변경 분석

작성일: `2026-04-11`

## 문서 목적

`2026-01-26` 이후 이 저장소에서 AI가 따라야 하는 repo rules가 왜 바뀌었는지, 어떤 식으로 바뀌었는지, 그 결과 무엇이 개선됐는지를 커밋과 저장소 문서 기준으로 정리한다.

이 문서는 `좋다/나쁘다` 식의 인상평보다 아래 세 가지를 분리해서 본다.

1. 어떤 규칙이 언제 들어왔는가
2. 왜 그 규칙이 문제로 인식됐는가
3. 무엇을 삭제/이동/축소해서 현재 구조가 되었는가

## 한 줄 결론

핵심 변화는 `문서/TDD 상태 머신을 alwaysApply에서 빼고`, `중간 이상 작업에만 이슈 기반 문서 규칙을 남기고`, `TDD plan을 전체 체크리스트에서 다음 최소 RED 1건 추천으로 바꾼 것`이다.

즉, 규칙을 없앤 것이 아니라 `repo-wide 강제 workflow`를 `작업 종류별 선택형 또는 조건부 규칙`으로 재배치했다.

## 1. 초기 구조: 강한 repo-wide workflow 도입

### `2026-01-26` `f04cc7f` `#97 [CHORE] apply Cursor DocOps settings`

이 커밋에서 아래가 한 번에 추가됐다.

- `.cursor/rules/00-language-and-tone.mdc`
- `.cursor/rules/10-docops-workflow.mdc`
- `.cursor/rules/20-tdd-kentbeck-plan-go.mdc`
- `.cursor/rules/30-tidy-first-commit-discipline.mdc`
- `.cursor/rules/40-git-pr-docs.mdc`
- `.cursor/rules/50-runtime-date-and-study-docs.mdc`
- 문서/분석/TDD/PR용 skill 10개

의미:

- 언어/표현 규칙만 추가된 것이 아니었다.
- `문서 게이트`, `승인 키워드`, `plan.md`, `go`, `PR 문서`, `study 문서`까지 모두 repo 기본 규칙 레벨로 들어왔다.

당시 `10-docops-workflow.mdc`는 다음을 항상 요구했다.

- 이슈 번호 게이트
- 문서 루트 강제
- `동의` 전 코드/테스트 수정 금지
- `go` 승인 기반 TDD 실행

당시 `20-tdd-kentbeck-plan-go.mdc`는 다음을 항상 요구했다.

- `plan.md`를 개발 순서의 SSOT로 사용
- 사용자가 `go`라고 말하면 다음 미체크 테스트 1개를 RED -> GREEN -> REFACTOR로 수행
- 결함 수정 시 API 레벨 실패 테스트 -> 최소 재현 테스트 -> 둘 다 통과

즉, 이 시점의 기본 구조는 `바로 구현`이 아니라 `이슈 번호 -> 문서 -> 동의 -> plan.md -> go -> TDD`였다.

### `2026-01-26` `4522ad6` `#97 [CHORE] DocOps: issue-only doc root gate`

같은 날 후속 커밋에서 강제가 더 선명해졌다.

- 문서/스킬 산출물 경로를 `docs/issues/#<issue-number>/...`로 통일
- `plan.md` 기본 위치를 `docs/issues/#<issue-number>/04-tdd/plan.md`로 고정
- PR 문서 경로도 브랜치 기준이 아니라 이슈 기준으로 고정

의미:

- workflow가 더 단순해진 것이 아니라, 경로와 절차가 더 강하게 결합됐다.
- AI는 코드를 고치기 전에 먼저 이슈 번호와 문서 위치를 맞추도록 유도되었다.

## 2. 중간 조정: 강한 gate는 유지한 채 일부 정리

### `2026-02-14` `0cb4438` `chore(repo): update doc workflow and line-ending settings`

이 커밋은 `10-docops-workflow.mdc`에서 아래를 제거했다.

- 고정된 7단계 문서 게이트 순서
  - 문제 스캔
  - 분석
  - ADR
  - TDD `plan.md`
  - Green 설계
  - 구현 로그
  - PR 초안

하지만 아래는 그대로 남겼다.

- 이슈 번호가 없으면 중단
- 문서 루트는 `docs/issues/#<issue-number>/...`
- `동의` 전 코드 변경 금지
- `go` 승인 기반 실행

의미:

- 과밀했던 “문서 종류의 고정 순서”는 약간 줄였지만,
- `이슈 번호 + 승인 키워드 + plan/go`라는 핵심 gate는 여전히 repo-wide였다.

### `2026-03-01` `f6c9593` `chore(cursor): enforce test naming with DisplayName in Korean`

이 커밋은 TDD 규칙을 줄이지 않고 오히려 테스트 표기 규칙을 더 추가했다.

- 테스트 클래스/메서드 식별자는 영문
- 각 테스트는 `@DisplayName` 필수
- 설명은 한국어

의미:

- 이 시점까지의 방향은 `절차 축소`보다 `기존 절차 위에 품질 규칙 추가`에 가까웠다.

## 3. 문제 인식: “규칙이 나쁜 것”보다 “레이어가 잘못된 것”

### `2026-03-07` `docs/issues/#999/0001-rules-restructure-discussion-log.md`

저장소 문서는 아래를 직접 적고 있다.

- 사용자는 현재 규칙이 간단한 구현까지 지나치게 무겁게 만든다고 느끼고 있다.
- 현재 규칙은 문서 게이트, 승인 키워드, `go` 기반 TDD, 학습 문서 생성, PR 문서화까지 repo-wide로 묶여 있다.
- 기본 방향은 `repo-wide 최소화`, `workflow는 skill/prompt file로 분리`다.

### `2026-03-07` `docs/issues/#999/0002-agent-rules-restructure-options.md`

이 문서는 원인을 더 정확히 짚는다.

- 좋은 규칙도 잘못된 레이어에 놓이면 agent 효율이 급격히 떨어진다.
- 핵심 문제는 규칙의 양 자체보다 `스코프 분리 실패`다.
- `이슈 번호`, `동의`, `go`, `study 문서`, `PR 초안`, `설계 문서`가 모두 `alwaysApply` 레이어에 올라가 있었다.

이 문서의 추천안은 명확하다.

- repo-wide에는 짧고 넓게 적용되는 규칙만 남긴다.
- 반복 workflow는 skill 또는 prompt file로 내린다.
- 고위험 작업에서만 이슈/문서/TDD workflow를 호출한다.

## 4. 실제 재구성: alwaysApply에서 workflow를 걷어냄

### `2026-03-22` `f511ac4` `chore(repo): sync cursor rules and skills [Structural]`

이 커밋이 가장 큰 전환점이다.

삭제된 alwaysApply 규칙:

- `.cursor/rules/10-docops-workflow.mdc`
- `.cursor/rules/20-tdd-kentbeck-plan-go.mdc`
- `.cursor/rules/40-git-pr-docs.mdc`

삭제된 문서 중심 skill:

- `.cursor/skills/doc-analysis-options/SKILL.md`
- `.cursor/skills/doc-discussion-log/SKILL.md`
- `.cursor/skills/doc-green-design/SKILL.md`
- `.cursor/skills/doc-learning-note/SKILL.md`
- `.cursor/skills/doc-work-log/SKILL.md`

추가된 규칙/skill:

- `.cursor/rules/my-custom-rules.mdc`
- `.cursor/skills/clarify-feature-specs/SKILL.md`
- `.cursor/skills/design-doc-before-change/SKILL.md`
- `.cursor/skills/design-plan/SKILL.md`
- `.cursor/skills/pr-docs-workflow/SKILL.md`
- `.cursor/skills/study-note-on-demand/SKILL.md`

가장 중요한 변화는 `doc-tdd-plan`이었다.

기존:

- `Kent Beck 스타일 TDD plan.md(테스트 체크리스트/시나리오)` 생성
- `plan.md`를 따르고, `go`가 오면 다음 미체크 테스트 1개를 진행

변경 후:

- 현재 테스트 상태를 보고 `다음 최소 RED 1건`만 추천
- reflection, broad assertion, stale test 같은 테스트 품질 리스크를 먼저 점검
- `Current Test Snapshot -> Next Smallest RED (exactly one) -> GREEN Hint -> Refactor Candidates` 구조 사용

의미:

- `plan.md`가 “전체 체크리스트를 미리 채워두는 문서”에서
- “현재 상태를 읽고 다음 한 걸음을 정하는 문서”로 역할이 바뀌었다.

이 변화는 이전에 나타난 문제와 정확히 연결된다.

- 체크리스트가 길수록 AI는 다음 테스트 1개보다 전체 phase를 따라가려 했다.
- baseline이 깨져 있거나 정책이 바뀌면 `plan.md` 전체가 쉽게 stale해졌다.
- 그래서 TDD plan을 고정 장문 문서가 아니라 `현재 상태 기반 다음 한 걸음`으로 줄였다.

## 5. 두 번째 재구성: “완전 자유”가 아니라 조건부 문서 규칙으로 복귀

### `2026-03-25` `cd70ef2` `chore(repo): track rules, AGENTS, and issue docs [Structural]`

이 커밋은 문서 규칙을 완전히 없애지 않고, 더 좁은 형태로 다시 도입했다.

추가된 파일:

- `.cursor/rules/40-issue-doc-driven-implementation.mdc`
- `AGENTS.md`

핵심 변화:

- 모든 작업이 아니라 `여러 파일 변경`, `API/계약 변경`, `리팩터링`, `테스트 추가`, `하루 이상 이어질 가능성이 있는 작업`에만 적용
- 구현 전 `docs/issues/#<issue-number>/02-analysis/0001-implementation-plan.md`
- 구현 중 `docs/issues/#<issue-number>/03-implementation/0001-worklog.md`

의미:

- 초기 구조의 문제를 보고 “문서를 없애자”로 간 것이 아니다.
- 추적성과 리뷰 품질이 필요한 중간 이상 작업에만 문서 규칙을 남긴 것이다.

### `2026-03-25` `9cacf26` `chore(repo): refresh AGENTS and issue-doc rule [Structural]`

이 커밋은 문서 위치 규칙을 다시 줄였다.

변경:

- 문서 위치를 “루트 `develop` 고정”에서 “현재 작업 디렉터리/워크트리 기준”으로 바꿈

의미:

- worktree에서 작업하면서도 문서가 자연스럽게 같은 작업 공간에 남게 됐다.
- `문서가 develop 루트에만 있어야 한다`는 마찰을 줄였다.

### `2026-03-25` `0e88f57` `chore(repo): align implementation rules and AGENTS [Structural]`

이 커밋은 `30-tidy-first-commit-discipline.mdc`를 삭제하고 `30-implementation-discipline.mdc`로 바꿨다.

핵심 변화:

- 큰 원칙 이름을 `Tidy First + Commit Discipline`에서 `Implementation Discipline`으로 재정의
- `아주 작은 논리 단위`
- `사람이 한눈에 리뷰할 수 있는 크기`
- `Structural / Behavioral` 분리

의미:

- 구조/동작 분리 원칙은 유지했지만,
- “거대한 workflow의 일부”가 아니라 “작은 구현 단위 규율”로 의미를 더 좁혔다.

### `2026-03-25` `d6286da` `chore(repo): require PR rationale in docs [Structural]`

이 커밋은 일시적으로 이슈 기반 규칙에 `PR에는 왜 이 이슈를 처리하는지까지 써야 한다`를 다시 추가했다.

이유 자체는 납득 가능하다.

- `무엇을 바꿨는지`만 적힌 PR은 추적성이 약하다.
- 배경, 문제, 이유를 적으면 리뷰 맥락이 좋아진다.

하지만 이 규칙은 오래 남지 않았다.

### `2026-03-26` `e21abd4` `chore(repo): sync AGENTS and implementation rules [Structural]`

이 커밋에서 현재 구조가 거의 완성됐다.

`30-implementation-discipline.mdc`는 다음을 명시했다.

- 항상 `지금 필요한 가장 작은 다음 변경`을 먼저 고른다.
- 모든 구현을 한꺼번에 하지 않는다.
- 그 단위 구현이 끝나면 worklog를 쓰고 커밋한다.
- 계획은 phase가 아니라 최소 변경 단위 목록으로 작성한다.

`40-issue-doc-driven-implementation.mdc`는 다음으로 정리됐다.

- 단계별 구현 순서는 가장 작은 변경 단위여야 한다.
- 구현 중에는 `30-implementation-discipline`을 참조한다.
- PR rationale 강제 섹션은 제거됐다.

의미:

- repo-wide 기본 경로는 다시 짧아졌고,
- 문서 규칙이 남는 부분도 `작은 다음 변경` 중심으로 재설계됐다.

## 6. 현재 구조가 어떻게 달라졌는가

현재 repo-wide 규칙 파일은 아래 5개다.

- `.cursor/rules/00-language-and-tone.mdc`
- `.cursor/rules/30-implementation-discipline.mdc`
- `.cursor/rules/40-issue-doc-driven-implementation.mdc`
- `.cursor/rules/50-runtime-date-and-study-docs.mdc`
- `.cursor/rules/my-custom-rules.mdc`

현재 로컬 skill은 아래 7개다.

- `.cursor/skills/clarify-feature-specs/SKILL.md`
- `.cursor/skills/design-doc-before-change/SKILL.md`
- `.cursor/skills/design-plan/SKILL.md`
- `.cursor/skills/doc-adr/SKILL.md`
- `.cursor/skills/doc-problem-scan/SKILL.md`
- `.cursor/skills/doc-tdd-plan/SKILL.md`
- `.cursor/skills/study-note-on-demand/SKILL.md`

초기와 비교하면 사라진 것:

- repo-wide `이슈 번호 게이트`
- repo-wide `동의` / `go`
- repo-wide `plan.md` SSOT 강제
- repo-wide `PR 문서화` 강제
- repo-wide `study 문서화` 강제

남긴 것:

- 한국어/표현 규칙
- 위험 변경 사전 설명
- 날짜는 런타임 기준
- 반복 CLI 오류 중단
- 작은 단위 구현
- `Structural / Behavioral` 분리
- 중간 이상 작업에서의 issue 기반 plan/worklog

## 7. 왜 이 변경이 개선으로 이어졌는가

### 1) 기본 경로가 짧아졌다

예전 기본 경로:

- 이슈 번호 요청
- 문서 생성
- `동의`
- `plan.md`
- `go`
- TDD 단계 실행

현재 기본 경로:

- 코드 위치 파악
- 바로 수정
- 필요한 테스트 실행
- 결과 요약

이 차이는 단순 작업에서 바로 체감된다.

### 2) 복잡한 작업에만 문서 비용을 지불하게 됐다

이전에는 단순 구현과 큰 변경이 비슷한 절차를 탔다.

지금은 다음 같은 경우에만 issue-doc 규칙이 붙는다.

- 여러 파일 변경
- API/계약 변경
- 리팩터링
- 테스트 추가
- 하루 이상 이어질 작업

즉, 문서화는 유지하되 적용 범위가 현실적으로 좁아졌다.

### 3) TDD가 “전체 시나리오 작성”에서 “다음 한 걸음”으로 바뀌었다

이전 `doc-tdd-plan`은 장문 체크리스트를 만들기 쉬운 구조였다.

지금 `doc-tdd-plan`은 다음을 강제한다.

- 현재 테스트 상태 확인
- 다음 최소 RED 1건만 제안
- broad assertion, reflection, stale test 같은 리스크 먼저 점검

그래서 `plan.md`가 구현을 끌고 가는 문서가 아니라, 현재 상태를 반영해 다음 한 걸음을 정하는 도구로 바뀌었다.

### 4) worktree/문서 경로 마찰이 줄었다

루트 `develop` 기준 고정은 문서 추적에는 유리했지만, 실제 작업 흐름과는 자주 어긋났다.

작업 디렉터리 기준으로 바꾸면서:

- worktree에서 바로 문서 작성 가능
- 코드와 문서가 같은 작업 공간에 존재
- 문서 위치 설명 비용 감소

### 5) “왜”는 남기되, 모든 단계에 강제하지 않게 됐다

PR rationale 강제는 합리적이지만, alwaysApply에 계속 두면 다시 workflow가 비대해진다.

현재 구조는 다음 쪽으로 균형을 잡는다.

- 위험 변경은 사전 설명
- 이슈 기반 큰 작업은 plan/worklog 유지
- 그러나 모든 단계에서 `왜` 문서를 강제하지는 않음

## 8. 남아 있는 한계

현재 구조가 완전히 자유로운 것은 아니다.

- 중간 이상 작업에는 여전히 이슈 기반 plan/worklog가 필요하다.
- 한국어 응답, 작은 단위 구현, `Structural / Behavioral` 분리는 계속 강하다.
- 즉, `문서 문화 자체를 버린 것`이 아니라 `기본 강제 범위를 현실화한 것`에 가깝다.

또 하나의 한계는 측정 방식이다.

- 저장소에는 규칙 변경의 의도와 구조는 잘 남아 있다.
- 하지만 “변경 후 작업 시간이 몇 % 줄었는가” 같은 정량 지표는 남아 있지 않다.

따라서 현재 확인 가능한 개선은 `구조적 개선`과 `문서/규칙 상의 마찰 감소` 수준까지다.

## 9. 최종 정리

이 저장소의 규칙 변경은 `문서화를 포기한 것`도 아니고, `TDD를 포기한 것`도 아니다.

정확한 표현은 아래에 가깝다.

1. `2026-01-26`에는 문서/TDD/PR/study workflow가 repo-wide 기본 경로로 들어왔다.
2. `2026-03-07` 전후에는 그 구조가 간단한 작업까지 너무 비싸게 만든다는 문제 인식이 생겼다.
3. `2026-03-22`에는 alwaysApply에서 무거운 workflow 규칙을 제거하고 skill로 내렸다.
4. `2026-03-25`~`2026-03-26`에는 중간 이상 작업에만 문서 규칙을 다시 좁게 도입하고, `가장 작은 다음 변경` 중심으로 재정의했다.

한 줄로 요약하면:

`AI가 따라야 할 규칙은 “항상 문서/TDD 상태 머신을 강제하는 구조”에서 “기본 규칙은 짧게, 무거운 workflow는 조건부로 호출하는 구조”로 바뀌었다.`
