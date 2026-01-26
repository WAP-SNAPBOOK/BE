---
name: doc-problem-scan
description: "프로젝트 내 문제/리팩토링 후보 스캔 문서를 생성한다(증거/영향/우선순위)."
disable-model-invocation: true
---

# doc-problem-scan

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- 코드 변경/커밋/푸시/PR 생성은 하지 않는다.

## Output
- 기본 저장 위치: `docs/<type>/<owner>/<branch-name>/00-intake/0001-problem-scan.md`

## Template
- Context(현재 상황)
- 증상/징후
  - 재현 절차
  - 로그/지표/스크린샷(가능한 경우 링크/발췌)
- 영향 범위
  - 사용자/도메인/성능/비용/보안
- 리팩토링 후보 목록(우선순위 + 근거)
- 지금 당장 안 하면 생기는 비용

