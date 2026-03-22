---
name: study-note-on-demand
description: "요청 시 `study/<YYYY-MM-DD>/`에 학습/질문 답변/특이사항을 기록한다."
disable-model-invocation: true
---

# study-note-on-demand

## Use when
- 사용자가 학습 메모, 질의응답 정리, 특이사항 기록을 원할 때
- 나중에 재사용할 지식으로 정리해 달라고 할 때

## Safety
- 기본 동작이 아니라 요청 시에만 사용한다.

## Output
- 기본 경로: `study/<YYYY-MM-DD>/`
- 주제가 다르면 새 파일을 만든다.

## Template
- 요약
- 배경 / 질문
- 핵심 내용
- 주의점 / 교훈
- 관련 파일 / 링크
