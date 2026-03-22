---
name: doc-tdd-plan
description: "현재 테스트 상태를 기준으로 다음 최소 TDD 시나리오(RED 1건)를 추천하는 `plan.md`를 생성/갱신한다."
disable-model-invocation: true
---

# doc-tdd-plan

## Purpose

- 이 Skill의 목표는 체크리스트를 한 번에 완성하는 것이 아니다.
- 현재 테스트/코드 상황을 읽고, 다음으로 작성할 최소 단위 테스트 시나리오 1건을 추천한다.
- 사용자와 함께 Red -> Green -> Refactor를 한 스텝씩 진행하도록 돕는다.

## Safety

- 이 Skill은 문서 파일 생성/수정까지만 수행한다.
- 테스트/프로덕션 코드 변경, 커밋/푸시/PR 생성은 하지 않는다.

## Required Context Check

- 최근 테스트 실행 결과(실패/통과/컴파일 실패)를 확인한다.
- 대상 레이어를 명시한다: `Domain`, `Service`, `Persistence`, `API`.
- 현재 테스트 품질 리스크를 요약한다.
  - reflection(`ReflectionTestUtils`, 강제 ID 주입)
  - broad assertion(`is4xxClientError`, `RuntimeException.class`)
  - 구현 결합 검증(호출 횟수/순서 중심)
  - stale/학습용/빈 테스트

## Output

- 기본 저장 위치: `docs/issues/#<issue-number>/04-tdd/plan.md`
- 핵심 산출물: 다음 최소 시나리오(RED) 1건 + 바로 이어지는 GREEN/REFACTOR 가이드

## Plan Template

```md
# TDD Next Step Plan

## 1) Current Test Snapshot
- 사실 기반 요약만 기록:
- 현재 깨지는 테스트/컴파일 이슈:
- 계약이 약한 테스트:
- 이번 스텝에서 건드리지 않을 범위(Non-goal):

## 2) Next Smallest RED (exactly one)
- Test name:
- Layer:
- Why this is the smallest next step:
- Given / When / Then:
- Expected failure (error code/exception/value):
- Observable contract (결과/상태/저장 결과):

## 3) GREEN Hint (minimum)
- 통과를 위한 최소 구현 방향(2~4줄):
- 이번 스텝에서 하지 않을 것(리팩터링/확장):

## 4) Refactor Candidates (after green)
- 중복 제거:
- 이름/구조 정리:
- 테스트 fixture 정리:

## 5) Open Questions
- 요구사항/계약 미확정 사항:
```

## Recommendation Rules

- 항상 테스트 1건 단위로 제안한다(한 번에 여러 체크박스 작성 금지).
- 구현 디테일보다 계약 검증을 우선한다.
- 우선순위는 `Domain -> Service -> Persistence -> API`를 기본으로 하되, 컴파일/치명 실패가 있으면 먼저 처리한다.
- `@SpringBootTest`는 경계 계약 검증이 필요한 경우에만 선택한다.
- broad assertion 대신 구체 assertion을 제안한다.

## Response Style

- "지금 당장 쓸 테스트 1개 + 선택 이유 + 바로 다음 액션" 중심으로 작성한다.
- 사용자가 실패 로그/수정 결과를 주면 Snapshot을 갱신하고 다음 최소 RED 1건을 다시 제안한다.

