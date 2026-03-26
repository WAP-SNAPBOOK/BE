# AI Agent Rules 재구성 설계안

작성일: `2026-03-07`
관련 이슈: `#107`
현재 브랜치: `refactor/jiseob/#105`

## 요약

현재 규칙셋의 문제는 “좋은 규칙이 많다”가 아니라 “너무 많은 workflow가 repo-wide 기본값으로 강제된다”는 점이다.

재구성 목표:

- 단순 작업은 바로 실행 가능하게 만든다.
- 고위험 작업은 여전히 문서/TDD workflow를 탈 수 있게 한다.
- 중복 규칙을 제거하고, 규칙의 위치를 목적에 맞게 나눈다.

## 제안하는 새 구조

### A. repo-wide 규칙으로 유지

#### `.cursor/rules/00-language-and-tone.mdc`

- 유지 범위
  - 한국어 응답 여부
  - 간결한 톤
  - 파일/함수/경로 표기 방식
- 비고
  - 팀 공통 규칙이 아니면 personal 레벨 이동 검토

#### `.cursor/rules/10-core-engineering.mdc` 신규

- 포함
  - 위험 명령 보수성
  - 기존 변경사항 임의 revert 금지
  - 구조 변경 / 동작 변경 분리
  - 테스트는 위험 변경에서 우선
  - 반복 CLI 오류 시 중단

#### `.cursor/rules/20-runtime-and-validation.mdc` 신규

- 포함
  - 날짜는 런타임 기준
  - 변경 후 가능한 범위 테스트/검증 실행
  - 결과는 간단 요약

### B. skill / prompt file로 이동

#### `issue-docops`

- 기존 내용 출처
  - `10-docops-workflow.mdc`
- 이동 내용
  - 이슈 번호 게이트
  - 이슈 루트 문서 저장
  - `동의`
  - `go`

#### `tdd-go-workflow`

- 기존 내용 출처
  - `20-tdd-kentbeck-plan-go.mdc`
  - `my-custom-rules.mdc` 일부
- 이동 내용
  - `plan.md` SSOT
  - `go(RED/GREEN/REFACTOR)`
  - 테스트 이름/DisplayName 규칙

#### `design-doc-before-change`

- 기존 내용 출처
  - `my-custom-rules.mdc`
- 이동 내용
  - 설계/근거/예시/예상결과 작성
  - md 저장 후 승인 대기

#### `study-note-on-demand`

- 기존 내용 출처
  - `50-runtime-date-and-study-docs.mdc`
- 이동 내용
  - `study/<date>/...` 저장
  - 학습/특이사항/질문 답변 기록

#### `pr-docs-workflow`

- 기존 내용 출처
  - `40-git-pr-docs.mdc`
- 이동 내용
  - PR 초안
  - 리뷰 체크리스트
  - 롤백/리스크 문서

### C. 축소 또는 제거

#### 제거 후보

- “모든 작업은 이슈 번호 기준”
- “동의 전에는 코드 수정 금지”
- “질문 답변은 항상 study 문서로 저장”
- “간단한 구현도 무조건 plan.md + go”

#### 완화 후 유지 후보

- “DB / public API / cross-cutting 변경에서는 설계 문서 권장”
- “버그 수정 / 복잡한 변경에서는 테스트 우선”

## 단계별 마이그레이션

### 1단계: 중복 제거

- `10-docops-workflow.mdc`, `20-tdd-kentbeck-plan-go.mdc`, `my-custom-rules.mdc`에서 겹치는 프로토콜을 식별
- 동일 의미 규칙을 한 곳으로 모은다

### 2단계: repo-wide 최소화

- `alwaysApply` 파일에는 핵심 엔지니어링 규칙만 남긴다
- workflow 강제 문구는 제거한다

### 3단계: workflow 문서화

- 이슈/TDD/PR/study/설계 문서 흐름을 각각 skill 또는 prompt file로 분리한다
- 호출 조건을 명시한다

### 4단계: `AGENTS.md` 동기화

- `.cursor/rules`와 `.cursor/skills` 변경 후 `scripts/sync-cursor-rules-to-agents.ps1` 실행

### 5단계: 검증

- 시나리오 A: 간단한 DTO 필드 추가
  - 기대: 바로 구현 가능
- 시나리오 B: DB migration 포함 기능
  - 기대: 설계/TDD workflow 권장 또는 호출
- 시나리오 C: 단순 질의응답
  - 기대: `study` 문서 자동 생성 없음

## 리스크 / 롤백

### 리스크

- 팀이 기존 절차에 익숙하다면 초기엔 “문서가 덜 남는 것 아닌가”라는 불안이 생길 수 있다.
- workflow가 자동 적용되지 않아 놓치는 경우가 생길 수 있다.

### 완화

- repo-wide 규칙에 “고위험 작업에서 설계/TDD workflow 사용” 한 줄을 남긴다.
- skill 이름을 명확하게 해 필요 시 쉽게 호출되게 한다.

### 롤백

- 기존 `.mdc` 내용을 백업해 두고, 새 구조가 불안정하면 바로 되돌린다.

## 테스트 전략

- 규칙 패치 후 다음 샘플 프롬프트로 동작 점검
  - “`DTO` 필드 하나 추가”
  - “`DB migration` 포함 기능 추가”
  - “이 규칙 설명해줘”
- 기대 결과
  - 단순 작업: 즉시 실행
  - 복잡 작업: 필요한 경우에만 문서/TDD workflow 사용
  - 설명 요청: 과도한 문서 생성 없음
