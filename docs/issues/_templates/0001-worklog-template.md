# `#<issue-number> <short-title>` 작업 로그

작성일: `<YYYY-MM-DD>`  
기준 이슈: `#<issue-number>`  
관련 브랜치: `<type>/<owner>/#<issue-number>`

## 사용 방법

- 각 항목은 **커밋 가능한 단위** 기준으로 추가한다.
- 실제 커밋을 했다면 해시를 적고, 아직 전이면 `pre-commit` 같은 상태를 적는다.
- `What`에는 변경 내용, `Why`에는 선택 이유를 짧고 명확하게 적는다.

## Entries

### Entry 001

- Date: `<YYYY-MM-DD HH:mm>`
- Unit: `pre-commit` 또는 `<commit-sha>`
- Type: `Structural` | `Behavioral`
- Scope:
  - 변경한 모듈/파일/계층
- What:
  - 무엇을 바꿨는지
- Why:
  - 왜 이렇게 나눴는지 / 왜 이 방식이 필요한지
- Verification:
  - 실행한 테스트 / 수동 확인 / 미실행 사유
- Next:
  - 다음 작업 단위
