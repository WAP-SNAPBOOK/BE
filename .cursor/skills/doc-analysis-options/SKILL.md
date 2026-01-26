---
name: doc-analysis-options
description: "문제 원인 분석 + 대안(A/B/C) 비교 + 추천안을 문서화한다."
disable-model-invocation: true
---

# doc-analysis-options

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- 코드 변경/커밋/푸시/PR 생성은 하지 않는다.

## Output
- 기본 저장 위치: `docs/issues/#<issue-number>/02-analysis/0001-root-cause-and-options.md`

## Template
- 문제 재정의(한 문장)
- 원인 분석
  - 가설
  - 검증(증거/재현/로그/지표)
  - 결론(원인)
- 대안
  - 대안 A: 장점/단점/비용/리스크/운영 난이도
  - 대안 B: 장점/단점/비용/리스크/운영 난이도
  - 대안 C: 장점/단점/비용/리스크/운영 난이도
- 추천안
  - 왜 이게 최선인지(트레이드오프 포함)
- 위험/롤백/관측(Observability) 계획

