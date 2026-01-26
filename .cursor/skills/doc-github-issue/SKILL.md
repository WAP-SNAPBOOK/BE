---
name: doc-github-issue
description: "GitHub 이슈 문서 초안을 생성한다(배경/목표/AC/리스크/체크리스트)."
disable-model-invocation: true
---

# doc-github-issue

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- `git commit/push`, `gh` 등 발행 동작은 하지 않는다.

## Output
- 기본 저장 위치: `docs/issues/#<issue-number>/01-issue/0001-github-issue-draft.md`

## Template
- 배경/문제 정의
- 목표(Goals)
- 비목표(Non-goals)
- 요구사항
  - 기능
  - 비기능(성능/보안/운영)
- 수용 기준(AC)
- 범위/의존성
- 리스크/운영 메모
- 작업 체크리스트(관련 문서 링크 포함)

