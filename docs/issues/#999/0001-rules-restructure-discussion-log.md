# `#107` Rules 재구성 논의 로그

작성일: `2026-03-07`

## 논의 주제

현재 `.cursor` / `AGENTS` 기반 규칙이 과도하게 복잡한지 평가하고, 최신 AI agent 가이드라인에 맞는 재구성 방향을 정한다.

## 공유된 전제 / 컨텍스트

- 사용자는 현재 규칙이 간단한 구현까지 지나치게 무겁게 만든다고 느끼고 있다.
- 현재 규칙은 문서 게이트, 승인 키워드, `go` 기반 TDD, 학습 문서 생성, PR 문서화까지 repo-wide로 묶여 있다.
- 공식 가이드 비교 결과, repo-wide 규칙은 더 짧고 넓게 적용되는 내용으로 줄이고, 반복 workflow는 별도 도구화하는 방향이 주류다.

## 합의 사항

- 현재 규칙은 과도하게 복잡하다는 문제 인식 자체는 타당하다.
- 규칙을 단순 삭제하기보다 `스코프 재배치`가 우선이다.
- 재구성 기본 방향은 다음과 같다.
  - repo-wide 최소화
  - workflow는 skill / prompt file로 분리
  - 언어 선호 같은 항목은 personal/org 레벨 검토

## 미합의 / 보류 사항

- `항상 한국어`를 repo 정책으로 유지할지, personal 규칙으로 옮길지
- `issue-first`를 완전 제거할지, “고위험 작업에서만” 적용할지
- `go` 기반 TDD를 skill로 남길지, 더 완화된 체크리스트 방식으로 바꿀지

## 액션 아이템

- `rules` 재구성 설계 문서 작성
- 새 규칙 파일 구조 제안
- 이후 실제 `.cursor/rules` 패치 여부 결정

## 관련 링크

- 기존 분석: `docs/issues/#107/02-analysis/0002-agent-rules-restructure-options.md`
- 기존 가격 이슈 문서: `docs/issues/#107/02-analysis/0001-root-cause-and-options.md`
