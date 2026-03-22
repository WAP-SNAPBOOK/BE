---
name: design-plan
description: "설계/계획 문서 작성(A/B안, 리스크, 마이그레이션 단계, 테스트 전략)."
disable-model-invocation: true
---

# design-plan

## Safety
- 이 Skill은 **문서 파일 생성/수정까지만** 수행한다.
- `git commit / push / gh` 명령은 실행하지 않는다.
- 실제 반영은 사용자 승인 후에만 가능하다.

## Output
- 기본 저장 위치: `docs/<type>/<owner>/<branch-name>/issue/0002-design-plan.md`

## Must include
- 설계안 A/B(트레이드오프)
- 단계별 롤아웃(ADD → backfill → switch → cleanup)
- 리스크/롤백 전략
- 테스트 전략(TDD, 체크리스트(엣지 케이스까지 확실하게게))

