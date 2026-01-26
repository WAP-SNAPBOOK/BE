---
name: doc-green-design
description: "Green phase에서 최소 구현으로 테스트를 통과시키는 설계/계획 문서를 작성한다."
disable-model-invocation: true
---

# doc-green-design

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- 코드 변경/커밋/푸시/PR 생성은 하지 않는다.

## Output
- 기본 저장 위치: `docs/issues/#<issue-number>/05-green-design/0001-green-implementation-plan.md`

## Must include
- plan.md(테스트 체크리스트)에서 “다음 미체크 1개”를 어떻게 통과시킬지
- 최소 구현 전략(이번 Green에서 하지 않을 것 포함)
- 구조 변경이 필요하면 Tidy First(구조/동작 분리) 계획

## Template
- 입력: 관련 문서 링크
  - 이슈/분석/ADR/plan.md
- 목표: 이번 Green에서 통과시킬 테스트(1개)
- 설계(최소)
  - 책임/경계(클래스/모듈/함수 수준)
  - 데이터 흐름/의존성
- 구현 계획
  - 단계(최소) + 각 단계에서 확인할 테스트
- 리스크/디버깅 포인트
- 완료 정의(DoD)

