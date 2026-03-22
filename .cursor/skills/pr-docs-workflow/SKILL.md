---
name: pr-docs-workflow
description: "PR 초안, 리뷰 체크리스트, 리스크/롤백 문서를 생성하는 선택형 workflow."
disable-model-invocation: true
---

# pr-docs-workflow

## Use when
- 사용자가 PR 초안이나 리뷰 체크리스트를 요청할 때
- 큰 변경을 마무리하며 문서 패키지가 필요할 때

## Safety
- 이 Skill은 문서 파일 생성/수정까지만 수행한다.
- PR 생성/발행, 커밋/푸시는 하지 않는다.

## Output
- 기본 경로: `docs/issues/#<issue-number>/07-pr/`

## Must include
- Summary
- Changes
- Test plan
- Risks & Rollback
- Docs
