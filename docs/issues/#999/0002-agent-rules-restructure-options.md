# `#107` AI Agent Rules 재구성 옵션 분석

작성일: `2026-03-07`

## 문제 재정의

현재 규칙셋은 문서화와 절차 통제에는 강하지만, `alwaysApply`되는 repo-wide 규칙에 워크플로우 상태와 문서 산출물 강제가 과도하게 포함되어 간단한 작업까지 비싸게 만든다.

## 원인 분석

### 가설

1. 품질 보장을 위해 넣은 규칙이 `항상 적용` 레이어에 몰리면서 작업 종류별 구분이 사라졌다.
2. `이슈 번호`, `동의`, `go`, `study 문서`, `PR 초안`, `설계 문서`가 모두 repo-wide 규칙으로 승격되었다.
3. 그 결과, 작업 복잡도와 무관하게 최대 절차가 기본 경로가 되었다.

### 검증

- 현재 repo-wide 규칙 파일
  - `.cursor/rules/00-language-and-tone.mdc`
  - `.cursor/rules/10-docops-workflow.mdc`
  - `.cursor/rules/20-tdd-kentbeck-plan-go.mdc`
  - `.cursor/rules/30-tidy-first-commit-discipline.mdc`
  - `.cursor/rules/40-git-pr-docs.mdc`
  - `.cursor/rules/50-runtime-date-and-study-docs.mdc`
  - `.cursor/rules/my-custom-rules.mdc`
- 중복/겹침 예시
  - `동의` / `go` / `plan.md` 프로토콜이 `10-docops-workflow.mdc`, `20-tdd-kentbeck-plan-go.mdc`, `my-custom-rules.mdc`, `AGENTS.md`에 반복됨
  - 설계 문서/PR 문서/학습 문서가 기본 경로로 강제됨
- 외부 공식 가이드와 비교한 기준
  - GitHub Copilot: repo-wide custom instructions는 대부분 요청에 넓게 적용되는 짧은 규칙 위주
  - Anthropic Claude Code: persistent instruction 파일은 자주 필요한 정보만 간결하게 유지, task-specific 지식은 skills/subagents 쪽으로 분리

### 결론

핵심 문제는 규칙의 양 자체보다 `스코프 분리 실패`다. 좋은 규칙도 잘못된 레이어에 놓이면 agent 효율이 급격히 떨어진다.

## 대안

### 대안 A. 현 구조 유지 + 문구만 완화

- 장점
  - 변경 비용이 가장 작다.
  - 기존 팀 습관을 거의 건드리지 않는다.
- 단점
  - 중복과 과한 기본 절차가 그대로 남는다.
  - 단순 작업 비용이 크게 줄지 않는다.
- 비용
  - 낮음
- 리스크
  - 체감 개선이 약하다.
- 운영 난이도
  - 낮음

### 대안 B. repo-wide 규칙 최소화 + workflow를 skills/prompt file로 분리

- 장점
  - 최신 agent 가이드와 가장 잘 맞는다.
  - 간단한 작업은 빠르게, 큰 작업은 문서 중심으로 처리할 수 있다.
  - 규칙 충돌과 중복을 줄일 수 있다.
- 단점
  - 기존 습관을 일부 다시 학습해야 한다.
  - `issue-first`, `go` 같은 절차가 항상 자동으로 걸리지는 않는다.
- 비용
  - 중간
- 리스크
  - 전환 초기에 “어떤 workflow를 언제 호출할지” 기준이 필요하다.
- 운영 난이도
  - 중간

### 대안 C. 전면 단순화하여 거의 모든 workflow 제거

- 장점
  - agent 자율성과 속도가 최대화된다.
  - 문서 오버헤드가 가장 작다.
- 단점
  - 추적성과 팀 내 일관성이 급감할 수 있다.
  - 도메인/DB 변경 같은 고위험 작업에서 품질 장치가 약해진다.
- 비용
  - 중간
- 리스크
  - 문서 기반 협업 문화가 사실상 붕괴할 수 있다.
- 운영 난이도
  - 낮음

## 추천안

대안 B를 추천한다.

### 추천 이유

- 좋은 엔지니어링 습관은 유지하면서, 그 습관을 `항상 적용`에서 `필요할 때 호출`로 내릴 수 있다.
- 현재 규칙 중 유지 가치가 높은 `Tidy First`, `런타임 날짜`, `반복 CLI 오류 중단`, `핵심 테스트 선호`는 repo-wide에 남길 수 있다.
- 반면 `이슈 번호 게이트`, `동의`, `go`, `study 문서화`, `PR 초안`, `설계 문서 강제`는 workflow skill 또는 prompt file로 이동하는 것이 적절하다.

## 목표 구조

### 1. repo-wide(`alwaysApply`)에 남길 것

- 언어/톤의 최소 규칙
- 위험 명령/안전 규칙
- 날짜는 런타임 확인
- 반복 CLI 오류 시 중단
- 구조 변경과 동작 변경 분리
- 테스트는 위험한 변경에서 우선 고려

### 2. skill 또는 prompt file로 내릴 것

- 이슈 번호 게이트
- `동의` / `go` / `plan.md`
- 설계 문서 선작성
- PR 초안/작업 로그/학습 문서 생성
- 브랜치 경로 기반 문서 저장 규칙

### 3. personal/org 레벨로 올릴 후보

- `항상 한국어`
- 커밋 메시지 추천 스타일 같은 개인 선호

## 위험 / 롤백 / 관측 계획

### 위험

- 전환 직후 일부 작업에서 문서 생성 습관이 누락될 수 있다.
- 팀원이 기존 `동의/go` 흐름을 기대하면 혼란이 생길 수 있다.

### 롤백

- 기존 `.mdc` 파일을 백업하고, 단계적으로 파일을 분리한 뒤 문제가 있으면 즉시 되돌린다.

### 관측

- 단순 작업에서 “이슈 번호 요청 → 문서 작성 → 승인 대기” 빈도가 줄어드는지 확인
- 복잡한 작업에서는 여전히 설계/TDD 문서가 필요할 때만 호출되는지 확인
