# `#107` 현재 AI Agent Rules 변경 요약

작성일: `2026-03-08`

## 문서 목적

최근 규칙 정리 이후, 현재 저장소에서 AI agent가 따르게 되는 규칙 구조가 어떻게 바뀌었는지 한 문서로 정리한다.

이 문서는 "예전에 무엇이 있었나"보다 "지금 무엇이 기본 규칙이고, 무엇이 선택형 workflow로 내려갔나"를 기준으로 본다.

---

## 한 줄 요약

현재 규칙셋은 `문서/TDD 상태 머신 강제` 중심에서 `가벼운 기본 규칙 + 필요 시 선택형 workflow 호출` 구조로 바뀌었다.

---

## 현재 기본 규칙(`alwaysApply`) 요약

### 1. 언어 / 표현

파일:

* `.cursor/rules/00-language-and-tone.mdc`

현재 의미:

* 기본 응답은 한국어
* 이모지는 요청 시에만 사용
* 설명은 간결하게 작성
* 파일/경로/클래스/함수/URL은 백틱으로 표기

평가:

* 작업 절차를 무겁게 만들지 않는 최소 표현 규칙만 남아 있다.

### 2. DocOps는 기본 강제가 아니라 선택형

파일:

* `.cursor/rules/10-docops-workflow.mdc`

현재 의미:

* 이슈 번호, 문서 루트, `동의`, `go`는 기본 강제가 아니다.
* 사용자가 명시적으로 원하거나, 큰 변경에서만 적용을 검토한다.
* 단순 구현/소규모 수정/명백한 버그 수정은 바로 진행 가능하다.

평가:

* 가장 큰 변화다.
* 예전의 "무조건 이슈 번호 → 문서 → 승인" 흐름이 기본 경로에서 빠졌다.

### 3. TDD는 강제 상태 머신이 아니라 기본 품질 원칙

파일:

* `.cursor/rules/20-tdd-kentbeck-plan-go.mdc`

현재 의미:

* 테스트 이름, 식별자, `@DisplayName` 같은 최소 테스트 품질 규칙만 남았다.
* `plan.md`, `go(RED/GREEN/REFACTOR)`는 기본 규칙이 아니다.
* 엄격한 TDD step은 필요 시 skill로 사용한다.

평가:

* 테스트 품질 규칙은 유지하면서, 절차 오버헤드는 줄였다.

### 4. 구조/동작 분리와 커밋 규율 유지

파일:

* `.cursor/rules/30-tidy-first-commit-discipline.mdc`

현재 의미:

* 구조 변경과 동작 변경을 섞지 않는다.
* 커밋은 작은 논리 단위로 나눈다.
* 테스트가 통과한 상태에서 커밋하는 원칙을 유지한다.

평가:

* 절차보다 품질에 직접 연결되는 규칙이라 유지 가치가 높다.

### 5. PR / 문서화는 on-demand

파일:

* `.cursor/rules/40-git-pr-docs.mdc`

현재 의미:

* PR 초안, 리뷰 체크리스트, 이슈 문서는 사용자가 원하거나 큰 변경 마무리 시에만 작성한다.
* 자동 `git commit/push`, PR 생성은 하지 않는다.

평가:

* 문서 강제는 줄이고, 통제는 유지하는 방향이다.

### 6. 날짜 / study 기록 규칙 경량화

파일:

* `.cursor/rules/50-runtime-date-and-study-docs.mdc`

현재 의미:

* 날짜/시간은 런타임에서 확인
* 반복 CLI 오류는 중단 후 사용자 조치 요청
* `study/...` 기록은 기본 동작이 아니라 요청 시 수행

평가:

* 유용한 신뢰성 규칙만 남기고, 자동 문서 생성은 제거했다.

### 7. 코드 수정 기본 휴리스틱 단순화

파일:

* `.cursor/rules/my-custom-rules.mdc`

현재 의미:

* 단순 변경은 바로 구현
* 다음처럼 위험한 변경만 사전 설명
  - DB schema 변경
  - public API 변경
  - 여러 파일에 걸친 리팩터링
  - 설계 대안 비교 필요
* 리팩터링 요청 시 우선순위/장단점/적용 기준 제시

평가:

* "즉시 코드 수정 금지" 계열 강한 게이트가 사라졌다.

---

## 현재 선택형 workflow(skill) 구조

현재 `.cursor/skills`에는 기존 문서 스킬 외에, 선택형 workflow 성격의 스킬이 추가되어 있다.

### workflow 성격이 강한 스킬

* `design-doc-before-change`
  * 코드 수정 전 설계/근거/예시/예상 결과 문서화
* `tdd-go-workflow`
  * `plan.md` + `go(RED/GREEN/REFACTOR)`를 엄격히 적용
* `study-note-on-demand`
  * 요청 시에만 `study/<YYYY-MM-DD>/...` 기록
* `pr-docs-workflow`
  * PR 초안 / 리뷰 체크리스트 / 리스크 문서 생성

### 기존 문서화 스킬

* `design-plan`
* `doc-adr`
* `doc-analysis-options`
* `doc-discussion-log`
* `doc-github-issue`
* `doc-green-design`
* `doc-learning-note`
* `doc-pr-draft`
* `doc-problem-scan`
* `doc-tdd-plan`
* `doc-work-log`
* `tdd-go-commit-recommender`

의미:

* 문서/TDD workflow가 사라진 것이 아니라, 기본 강제에서 선택형 도구로 위치가 바뀌었다.

---

## 작업 체감이 어떻게 달라졌는가

### 예전 체감

간단한 수정도 다음 흐름으로 끌려갈 가능성이 컸다.

* 이슈 번호 확인
* 문서 생성
* `동의` 대기
* `plan.md`
* `go`
* 단계별 TDD

### 현재 체감

단순 작업은 기본적으로 다음 흐름을 탄다.

* 코드 위치 파악
* 바로 수정
* 필요한 테스트 실행
* 결과 요약

복잡한 작업만 선택적으로 다음 흐름을 탈 수 있다.

* 설계 문서
* 이슈 기반 문서화
* 엄격한 `go` 기반 TDD
* PR 문서화

---

## 종합 평가

현재 구조의 핵심 변화는 다음 두 가지다.

1. 기본 규칙은 `짧고 넓게 적용되는 것`만 남겼다.
2. 무거운 workflow는 `삭제`가 아니라 `선택형 skill`로 분리했다.

이 구조의 장점:

* 단순 작업의 진입 비용이 낮다.
* 큰 변경에서는 여전히 문서와 TDD workflow를 사용할 수 있다.
* 규칙 충돌과 중복이 줄었다.

남아 있는 특징:

* 여전히 한국어 응답, Tidy First, 런타임 날짜 확인 같은 팀 스타일은 유지된다.
* 커밋/PR 자동 실행 금지 같은 안전 규칙도 유지된다.

---

## 관련 파일

* `AGENTS.md`
* `.cursor/rules/00-language-and-tone.mdc`
* `.cursor/rules/10-docops-workflow.mdc`
* `.cursor/rules/20-tdd-kentbeck-plan-go.mdc`
* `.cursor/rules/30-tidy-first-commit-discipline.mdc`
* `.cursor/rules/40-git-pr-docs.mdc`
* `.cursor/rules/50-runtime-date-and-study-docs.mdc`
* `.cursor/rules/my-custom-rules.mdc`
* `.cursor/skills/`
