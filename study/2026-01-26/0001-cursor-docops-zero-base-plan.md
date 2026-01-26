# Cursor 문서화 기반 개발 워크플로우 (제로베이스 세팅 계획)

- 작성일(런타임): 2026-01-26
- 대상 레포(worktree): `C:\Users\seobe\.cursor\worktrees\BE\hlj`
- 현재 git 상태: detached HEAD(브랜치 없음) → 브랜치 기반 경로 자동화를 위해 “브랜치 명확화”가 선행되면 가장 좋음

## 0) 목표(내가 원하는 운영 방식 한 문장)
“기능 구현/리팩토링을 왜 하는지(문제·대안·결정)가 문서로 남고, plan.md가 개발 순서를 고정하며, go 시 plan.md의 다음 체크 항목을 TDD(RED→GREEN→REFACTOR)로 진행한다.”

## 1) 어떻게 변경할지(설계/접근 방식)
### 1.1 프로젝트에 추가/초기화할 구성요소(제로베이스)
- `.cursor/rules/`: 문서 우선/경로 규칙/TDD(go·plan.md)/Tidy First/PR·커밋 규율을 강제
- `.cursor/skills/`: 각 “문서 산출물”을 빠르게 만들기 위한 템플릿 기반 스킬 모음
- `docs/...` 및 `study/...`: 브랜치 기반(팀 산출물) + 날짜 기반(학습/메모) 저장

### 1.2 문서 저장 “기본 규칙”
- **브랜치 기반 산출물(팀 작업물)**: `docs/<type>/<owner>/<branch-name>/...`
  - 예) `docs/feature/jiseob/feature/jiseob/#97-reservation-erd-v2-follow-up/` 처럼 중복이 생기면,
    - 권장 단순화: `docs/<owner>/<branch-name>/...` (운영 편의성↑)
- **학습/메모(질문/특이사항/회고)**: `study/<YYYY-MM-DD>/...` (YYYY-MM-DD는 런타임에서 산출)

### 1.3 “문서 게이트” (코드 변경 전 필수 흐름)
1) 문제/리팩토링 후보 발견 문서
2) 분석(원인/대안/추천안/리스크)
3) 결정(ADR) + 논의 로그
4) **TDD `plan.md`**: 체크리스트가 곧 개발 순서(SSOT)
5) Green 설계(최소 구현 전략, 구조/동작 변경 분리 계획)
6) 구현(커밋 단위 로그) + 학습 정리
7) PR 문서로 마무리

## 2) 변경 근거(이유, 장단점)
### 2.1 왜 이 방식인가
- 문서(why/alternatives/decision)가 남아야 “팀 수준”의 리뷰/회고가 가능
- `plan.md`를 SSOT로 두면 “다음 할 일”이 자동으로 결정되고, 컨텍스트 스위칭 비용이 줄어듦
- Kent Beck 스타일 TDD + Tidy First는 “작은 단위/최소 변경/구조-동작 분리”로 리스크를 줄임

### 2.2 장점
- 작업의 이유/대안/결정이 명확(미래의 나/동료가 이해 가능)
- 테스트가 개발 순서를 고정(의사결정 지연/우왕좌왕 감소)
- 작은 커밋/명확한 커밋 타입(구조 vs 동작)로 PR 리뷰 품질↑

### 2.3 단점/트레이드오프
- 초기 문서 비용이 발생(대신 재작업/리스크 비용을 줄임)
- 규율(go/plan/커밋 분리)을 지키지 않으면 체계가 무너짐 → rules/skills로 강제

## 3) 코드 수정 예시(diff 또는 예시 코드)
### 3.1 앞으로 생성될 파일(예시)
```text
.cursor/
  rules/
    00-language-and-tone.mdc
    10-docops-workflow.mdc
    20-tdd-kentbeck-plan-go.mdc
    30-tidy-first-commit-discipline.mdc
    40-git-pr-docs.mdc
    50-runtime-date-and-study-docs.mdc
  skills/
    doc-problem-scan/SKILL.md
    doc-github-issue/SKILL.md
    doc-analysis-options/SKILL.md
    doc-adr/SKILL.md
    doc-discussion-log/SKILL.md
    doc-tdd-plan/SKILL.md
    doc-green-design/SKILL.md
    doc-work-log/SKILL.md
    doc-pr-draft/SKILL.md
    doc-learning-note/SKILL.md
```

### 3.2 `plan.md` 프로토콜(요지)
- “Always follow the instructions in plan.md.”
- “go”를 받으면 `plan.md`의 다음 미체크 테스트 1개를:
  - 테스트 작성(RED) → 최소 구현(GREEN) → 리팩터(REFACTOR)
  - Tidy First: 구조 변경과 동작 변경을 섞지 않음

## 4) 변경 후 예상 결과(동작, 성능, 영향 범위)
- Cursor에서 어떤 작업을 시작하든:
  - “문서 생성 → plan.md 확정 → go로 한 단계씩 실행” 흐름이 일관되게 유지됨
- 문서가 브랜치/날짜 기준으로 자동 정리되어, 작업 컨텍스트가 장기적으로 보존됨
- 구현/리팩토링의 “why/alternatives/decision”이 항상 남음

## 5) 실제로 만들 문서 템플릿(목차 고정)
> 아래 템플릿은 skills가 그대로 생성하도록 한다.

### A. 문제 발견/리팩토링 후보 스캔
- 경로: `docs/.../00-intake/0001-problem-scan.md`
- 필수: 증거(재현/로그/지표), 영향 범위, 우선순위, 지금 안 하면 생기는 비용

### B. GitHub 이슈 초안
- 경로: `docs/.../01-issue/0001-github-issue-draft.md`
- 필수: 배경, 목표/비목표, AC, 리스크, 작업 체크리스트(문서 링크)

### C. 분석 + 대안 비교
- 경로: `docs/.../02-analysis/0001-root-cause-and-options.md`
- 필수: 원인(가설→검증), 대안 A/B/C(트레이드오프), 추천안(왜), 리스크/롤백

### D. ADR + 논의 로그
- ADR 경로: `docs/.../03-decisions/0001-adr-<slug>.md`
- 논의 경로: `docs/.../03-decisions/0002-discussion-log.md`

### E. TDD plan.md (SSOT)
- 경로: `docs/.../04-tdd/plan.md`
- 필수: Ground rules(go/plan), 테스트 체크리스트(짧은 단위), 버그 프로토콜(API-level test → 최소 재현 테스트)

### F. Green 설계
- 경로: `docs/.../05-green-design/0001-green-implementation-plan.md`
- 필수: 테스트→코드 매핑, 최소 구현 전략, 구조/동작 변경 분리 계획(Tidy First), 리스크/디버깅 포인트

### G. 구현 로그 / PR 초안 / 학습
- 구현 로그: `docs/.../06-implementation/0001-work-log.md`
- PR 초안: `docs/.../07-pr/0001-pr-draft.md`
- 학습: `docs/.../99-learning/0001-learnings.md` + 필요 시 `study/<YYYY-MM-DD>/...`

## 6) 브랜치(detached HEAD) 대응 정책
- 원칙: 브랜치 기반 저장이 목적이므로, 작업 시작 전에 **브랜치를 명확히** 하는 것을 권장
- 부득이하게 detached 상태면:
  - 임시 브랜치명으로 `detached-<short-sha>`를 사용하거나
  - setup 전용 브랜치(`chore/cursor-docops-reset`)를 생성해서 진행

## 7) 다음 액션(승인 후 수행)
- 사용자가 “동의”라고 답하면:
  - 이 계획서에 명시된 파일/폴더 구조대로 `.cursor/rules`, `.cursor/skills`를 새로 생성
  - rules에 “문서 게이트 + plan/go + Tidy First” 강제 문구를 반영
  - skills에 각 문서 템플릿을 반영(문서 생성/수정만 수행하도록 safety 포함)

