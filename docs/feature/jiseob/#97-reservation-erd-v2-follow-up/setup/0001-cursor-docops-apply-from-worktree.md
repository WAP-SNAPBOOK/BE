# Cursor DocOps 세팅 적용(Worktree → 대상 워크트리) 계획서

- 작성일(런타임): 2026-01-26
- 대상 워크트리(적용 대상): `C:/Users/seobe/desktop/wjs/project/snapbook/BE`
- 소스 워크트리(가져올 세팅): `C:/Users/seobe/.cursor/worktrees/BE/hlj`
- 대상 브랜치: `feature/jiseob/#97-reservation-erd-v2-follow-up`
- 적용 방식: **파일 복사(히스토리 없이)**
  - 대상 `.cursor/`를 **초기화(삭제)** 한 뒤, 소스 `.cursor/` 내용을 그대로 생성
  - 대상 `study/`는 소스의 `study/2026-01-26/0001-cursor-docops-zero-base-plan.md`를 그대로 반영

## 1) 어떻게 변경할지(설계/접근 방식)
### 1.1 적용 범위
- **삭제/초기화 대상(대상 워크트리)**:
  - `.cursor/**` (rules/skills 전체)
  - `study/2026-01-26/0001-cursor-docops-zero-base-plan.md`가 기존에 있으면 교체
- **생성/적용 대상(대상 워크트리)**:
  - 소스 워크트리의 `.cursor/rules/*` 6개
  - 소스 워크트리의 `.cursor/skills/*/SKILL.md` 10개
  - 소스 워크트리의 `study/2026-01-26/0001-cursor-docops-zero-base-plan.md`

### 1.2 실제 적용 방식(히스토리 없이)
- git merge/cherry-pick을 사용하지 않고, **파일을 삭제 후 동일 내용으로 재생성**한다.
- 장점: 대상 브랜치가 worktree 충돌에 걸려도 적용 가능(현재 상황에 적합)
- 단점: 변경 이력이 커밋 하나로 정리되며, 소스 브랜치의 커밋 히스토리는 직접 연결되지 않음

### 1.3 커밋 계획(B 방식)
- `.cursor/` + `study/`를 함께 스테이징/커밋한다.
- 커밋 메시지는 프로젝트 스타일에 맞춰 `#97 [CHORE] ...` 형태로 작성한다.

## 2) 변경 근거(이유, 장단점)
### 2.1 왜 파일 복사 방식인가
- 동일 브랜치를 두 worktree에서 동시에 체크아웃할 수 없어서(worktree lock),
  대상 워크트리에서 바로 “소스 브랜치로 전환” 없이도 적용하려면 파일 복사가 가장 단순하다.

### 2.2 장점
- worktree 제약과 무관하게 즉시 적용 가능
- 대상 브랜치에 결과물이 “한 번에” 들어가서 운영이 단순

### 2.3 단점/주의
- 기존 `.cursor` 설정은 초기화되므로, 필요하면 백업이 필요
- `study/`는 팀 공용으로 반영될 수 있으니(원하는 정책이라면 OK) 범위를 의식적으로 포함한다

## 3) 변경 예시(파일 목록)
### 3.1 생성될 rules(6)
- `.cursor/rules/00-language-and-tone.mdc`
- `.cursor/rules/10-docops-workflow.mdc`
- `.cursor/rules/20-tdd-kentbeck-plan-go.mdc`
- `.cursor/rules/30-tidy-first-commit-discipline.mdc`
- `.cursor/rules/40-git-pr-docs.mdc`
- `.cursor/rules/50-runtime-date-and-study-docs.mdc`

### 3.2 생성될 skills(10)
- `.cursor/skills/doc-problem-scan/SKILL.md`
- `.cursor/skills/doc-github-issue/SKILL.md`
- `.cursor/skills/doc-analysis-options/SKILL.md`
- `.cursor/skills/doc-adr/SKILL.md`
- `.cursor/skills/doc-discussion-log/SKILL.md`
- `.cursor/skills/doc-tdd-plan/SKILL.md`
- `.cursor/skills/doc-green-design/SKILL.md`
- `.cursor/skills/doc-work-log/SKILL.md`
- `.cursor/skills/doc-pr-draft/SKILL.md`
- `.cursor/skills/doc-learning-note/SKILL.md`

### 3.3 생성/교체될 study(1)
- `study/2026-01-26/0001-cursor-docops-zero-base-plan.md`

## 4) 변경 후 예상 결과
- 대상 워크트리에서 Cursor가 문서 게이트/plan.md-go/Tidy First 규율을 기본으로 강제한다.
- `.cursor`와 `study`가 브랜치에 포함되어, 동일 브랜치를 쓰는 다른 환경에서도 동일한 워크플로우를 재현할 수 있다.

## 5) 실행 절차(동의 후 수행)
1) 대상 워크트리의 `.cursor/**` 파일 전부 삭제(초기화)
2) 소스 워크트리의 `.cursor/**`를 동일 내용으로 대상에 생성
3) `study/2026-01-26/...`도 동일 내용으로 대상에 생성/교체
4) `git add .cursor study` → 커밋

---

## 승인
이 계획대로 진행하려면: **“동의”**라고 답해줘.

