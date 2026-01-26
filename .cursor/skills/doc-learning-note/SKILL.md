---
name: doc-learning-note
description: "학습/특이사항/실수/교훈을 재사용 가능한 형태로 문서화한다."
disable-model-invocation: true
---

# doc-learning-note

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- 코드 변경/커밋/푸시/PR 생성은 하지 않는다.

## Output
- 기본 저장 위치(브랜치): `docs/<type>/<owner>/<branch-name>/99-learning/0001-learnings.md`
- 보조 저장 위치(날짜): `study/<YYYY-MM-DD>/xxxx-<topic>.md`

## Template
- 날짜(런타임 기준)
- 오늘 배운 점(핵심 1~3개)
- 의외의 사실/실수/교훈
- 다음에 적용할 규칙/체크리스트(재사용 가능하게)
- 관련 링크(이슈/ADR/plan.md/PR)

