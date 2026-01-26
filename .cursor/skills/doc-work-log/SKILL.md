---
name: doc-work-log
description: "구현/리팩토링 진행을 커밋 단위로 기록하는 작업 로그 문서를 생성한다."
disable-model-invocation: true
---

# doc-work-log

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- 코드 변경/커밋/푸시/PR 생성은 하지 않는다.

## Output
- 기본 저장 위치: `docs/<type>/<owner>/<branch-name>/06-implementation/0001-work-log.md`

## Template
- 목적(이 작업이 왜 필요한가)
- 진행 로그(커밋 단위)
  - 날짜/시간(런타임 기준)
  - Structural vs Behavioral (Tidy First)
  - 변경 요약(핵심 3줄)
  - 테스트 결과(무엇을 어떻게 확인했는지)
  - 리스크/메모
- 다음 할 일(다음 plan.md 체크 항목 링크)

