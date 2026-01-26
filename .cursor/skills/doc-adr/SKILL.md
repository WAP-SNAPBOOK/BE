---
name: doc-adr
description: "ADR(Architecture Decision Record) 문서를 생성한다(결정/대안/결과)."
disable-model-invocation: true
---

# doc-adr

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- 코드 변경/커밋/푸시/PR 생성은 하지 않는다.

## Output
- 기본 저장 위치: `docs/<type>/<owner>/<branch-name>/03-decisions/0001-adr-<slug>.md`

## Template
- Decision(결정)
- Status(제안/승인/폐기)
- Context(맥락/배경/문제)
- Options considered(대안 목록)
- Decision drivers(결정 기준/우선순위)
- Consequences(트레이드오프/리스크/운영 영향)
- Links(관련 이슈/문서/PR)

