---
name: design-doc-before-change
description: "코드 수정 전 설계/근거/예시/예상 결과를 문서화하는 선택형 workflow."
disable-model-invocation: true
---

# design-doc-before-change

## Use when
- 사용자가 설계 문서를 먼저 원할 때
- DB schema, public API, 대규모 리팩터링처럼 사전 합의가 중요한 변경일 때

## Safety
- 이 Skill은 문서 작업까지만 수행한다.
- 코드 변경은 별도 승인이나 후속 요청 후 진행한다.

## Must include
- 어떻게 변경할지
- 변경 근거(이유, 장단점, 대안)
- 코드 수정 예시(diff 또는 예시 코드)
- 변경 후 예상 결과(동작, 성능, 영향 범위)

## Output
- 이슈 기반이면 `docs/issues/#<issue-number>/...`
- 브랜치 기반이면 `docs/<type>/<owner>/<branch-name>/...`
