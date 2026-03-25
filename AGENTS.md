<!-- Manually synced from `.cursor/rules/*.mdc` and `.cursor/skills/*/SKILL.md`. -->

# Project Instructions

- 이 파일은 현재 저장소의 핵심 규칙과 로컬 스킬을 간단히 요약한다.

## Source: `.cursor/rules/00-language-and-tone.mdc`

# 언어/톤/표현 규칙

- 항상 **한국어**로 답변한다.
- 사용자가 요구하지 않는 한 이모지는 사용하지 않는다.
- 설명은 간결하게 하되, “왜/대안/결정”이 필요한 문서 작업에서는 근거를 생략하지 않는다.
- 파일/디렉터리/클래스/함수/URL은 백틱(``)으로 감싼다.

## Source: `.cursor/rules/30-tidy-first-commit-discipline.mdc`

# Tidy First + Commit Discipline

- 변경은 `구조적 변경(Structural)`과 `동작 변경(Behavioral)`로 나눠서 다룬다.
- 구조 변경과 동작 변경을 한 단계에 섞지 않는다.
- 둘 다 필요하면 구조 변경을 먼저 하고, 그 다음 동작 변경을 한다.
- 이 규칙은 커밋 분해의 유일한 기준이 아니라 가드레일이다.
- 사용자가 특정 이슈의 리뷰 흐름이나 커밋 순서를 요구하면, 그 흐름을 우선한다.
- 커밋은 가능한 한 이슈 진행 순서와 리뷰 가능한 단계를 따라 자르고, 그 안에서 구조/동작 혼합을 피한다.
- 커밋은 작고 자주 하되, 단일 논리 단위여야 한다.
- 커밋은 테스트 통과와 가능한 범위의 경고 해결을 전제로 한다.
- 커밋 메시지에는 `Structural` / `Behavioral` 성격이 드러나야 한다.
- 사용자가 별도 커밋 플로우 문서나 커밋 로그 예시를 준 경우, 그 문서를 기본 분해 축으로 삼는다.

## Source: `.cursor/rules/50-runtime-date-and-study-docs.mdc`

# 런타임 날짜 / 기록 규칙

- 날짜/시간이 필요하면 대화 컨텍스트 대신 런타임에서 확인한다.
- 반복되는 CLI 오류가 같은 원인으로 이어지면 중단하고 사용자 조치를 요청한다.
- `study/...` 문서화는 기본 동작이 아니며, 사용자가 기록을 원할 때만 수행한다.

## Source: `.cursor/rules/my-custom-rules.mdc`

# 코드 수정 기본 규칙

- 단순 변경은 바로 구현한다.
- 다음처럼 위험한 변경은 수정 전에 접근 방식과 리스크를 짧게 설명한다.
  - DB schema 변경
  - public API 변경
  - 여러 파일에 걸친 리팩터링
  - 설계 대안 비교가 필요한 변경
- 리팩터링 요청 시에는 우선순위, 장단점, 적용 기준을 함께 제시한다.

## Source: `.cursor/rules/40-issue-doc-driven-implementation.mdc`

# 이슈 기반 계획/구현/기록 규칙

- 여러 파일 변경, API 변경, 리팩터링, 테스트 추가가 필요한 작업, 하루 이상 이어질 수 있는 작업 같은 이슈 기반 중간 이상 작업에는 이 규칙을 적용한다.
- 문서 기준 위치는 항상 루트 작업 디렉터리의 `docs/issues/#<issue-number>/...` 이다.
- 구현 전 `docs/issues/#<issue-number>/02-analysis/0001-implementation-plan.md` 를 작성하거나 갱신한다.
- 구현은 커밋 가능한 논리 단위로 나누고, 가능하면 `Structural` / `Behavioral` 을 분리한다.
- 구현 중 `docs/issues/#<issue-number>/03-implementation/0001-worklog.md` 를 작성하거나 갱신한다.
- worklog에는 날짜/시간, 작업 단위 또는 커밋 해시, 변경 내용, 이유, 검증, 다음 작업을 남긴다.
- 단순하고 국소적인 변경에는 이 규칙을 강제하지 않는다.

## Project Skills

- 로컬 재사용 스킬은 `.cursor/skills` 아래에 있다.
- `clarify-feature-specs`: 모호한 요구사항을 질문 라운드로 구체화해 `PRD`, `Feature Spec`, `Tech/API Spec`, `Acceptance Criteria` 4개 문서로 정리한다. 기본 경로는 `docs/specs/<feature-name>/`이다.
- `design-doc-before-change`: 코드 수정 전에 변경 방식, 근거, 대안, 예시, 예상 결과를 먼저 문서화한다.
- `design-plan`: 설계안 A/B, 롤아웃 단계, 리스크/롤백, 테스트 전략을 포함한 설계 계획 문서를 작성한다.
- `doc-adr`: 결정/대안/결과를 담는 ADR 문서를 작성한다. 기본 경로는 `docs/issues/#<issue-number>/03-decisions/`이다.
- `doc-github-issue`: 배경, 목표, 요구사항, AC, 리스크, 체크리스트를 담은 GitHub 이슈 초안을 문서로 작성한다.
- `doc-pr-draft`: 변경 요약, 테스트 계획, 리스크/롤백, 관련 문서를 담은 PR 본문 초안을 작성한다.
- `doc-problem-scan`: 문제 증상, 영향 범위, 리팩터링 후보, 우선순위를 정리한 스캔 문서를 작성한다.
- `doc-tdd-plan`: 현재 테스트 상태를 읽고 다음 최소 `RED` 1건 중심의 `plan.md`를 생성/갱신한다.
- `pr-docs-workflow`: 큰 변경 마무리 시 `PR` 초안, 리뷰 체크리스트, 리스크/롤백 문서 패키지를 작성한다.
- `study-note-on-demand`: 요청이 있을 때만 `study/<YYYY-MM-DD>/` 아래에 학습 메모와 Q&A를 기록한다.
