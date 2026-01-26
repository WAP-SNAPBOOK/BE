---
name: doc-tdd-plan
description: "Kent Beck 스타일 TDD `plan.md`(테스트 체크리스트/시나리오)를 생성한다."
disable-model-invocation: true
---

# doc-tdd-plan

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- 테스트/프로덕션 코드 변경, 커밋/푸시/PR 생성은 하지 않는다.

## Output
- 기본 저장 위치: `docs/<type>/<owner>/<branch-name>/04-tdd/plan.md`

## Must follow
- Always follow the instructions in plan.md.
- 사용자가 “go”라고 말하면 `plan.md`의 **다음 미체크 테스트 1개**부터 진행한다.

## Template
- Ground rules
  - Red → Green → Refactor
  - Tidy First(구조/동작 분리)
  - Defect protocol(API-level failing test → 최소 재현 테스트 → 둘 다 통과)
- Scope
  - Goals / Non-goals
- Test list (체크박스)
  - `[ ]` 가장 작은 증가를 표현하는 테스트(짧게)
  - 각 항목에 포함: 목적/입력/출력/엣지케이스/관측 포인트
- Notes
  - long-running 테스트 제외 기준
  - 모호한 요구사항/질문 목록(추가 논의 링크)

