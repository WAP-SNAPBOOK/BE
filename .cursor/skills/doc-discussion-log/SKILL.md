---
name: doc-discussion-log
description: "사용자와 논의/합의/미합의 사항을 논의 로그로 문서화한다."
disable-model-invocation: true
---

# doc-discussion-log

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- 코드 변경/커밋/푸시/PR 생성은 하지 않는다.

## Output
- 기본 저장 위치: `docs/<type>/<owner>/<branch-name>/03-decisions/0002-discussion-log.md`

## Template
- 날짜(런타임 기준)
- 논의 주제
- 공유된 전제/컨텍스트
- 합의 사항
- 미합의/보류 사항(결정 필요)
- 액션 아이템(담당/기한)
- 관련 링크(이슈/ADR/plan.md)

