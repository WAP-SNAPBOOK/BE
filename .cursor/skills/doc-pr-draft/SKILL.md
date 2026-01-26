---
name: doc-pr-draft
description: "PR 본문 초안과 테스트/리스크/롤백 정보를 문서로 생성한다."
disable-model-invocation: true
---

# doc-pr-draft

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- PR 생성/발행(gh 등), 커밋/푸시는 하지 않는다.

## Output
- 기본 저장 위치: `docs/issues/#<issue-number>/07-pr/0001-pr-draft.md`

## Template
## Summary
- 왜 이 변경이 필요한지(문제/목표)
- 어떤 대안을 고려했고 왜 이 방식을 선택했는지

## Changes
- 핵심 변경 1~3개(세부 나열 금지)

## Test plan
- TDD(plan.md) 링크
- 확인한 테스트(체크리스트)
- 엣지 케이스/회귀 포인트

## Risks & Rollback
- 위험 요소
- 롤백/완화 전략

## Docs
- 관련 문서 링크(이슈/분석/ADR/논의/plan.md/학습)

