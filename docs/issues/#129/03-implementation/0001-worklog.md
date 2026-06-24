# `#129 예약 상세 응답 총액 추가` 작업 로그

작성일: `2026-06-18`  
기준 이슈: `#129`  
관련 브랜치: `feature/#129`

## Entries

### Entry 001

- Date: `2026-06-18 00:46`
- Unit: `pre-commit`
- Type: `Structural`
- Scope:
  - `docs/issues/#129/02-analysis/0001-implementation-plan.md`
- What:
  - 예약 상세 응답 총액 추가 이슈의 범위와 완료 기준을 정리했다.
- Why:
  - 예약 상세 바텀시트 보강은 캘린더 조회와 분리해서 이슈 단위로 진행해야 한다.
- Verification:
  - 문서 작성 후 계획 범위를 확인했다.
- Next:
  - `ReservationDetailResponse`와 `ReservationService`를 수정해 총액 필드를 추가한다.

### Entry 002

- Date: `2026-06-18 00:46`
- Unit: `pre-commit`
- Type: `Behavioral`
- Scope:
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationDetailResponse.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `docs/issues/#129/frontend-api-handoff.md`
- What:
  - 예약 상세 응답에 `totalPrice` 필드를 추가하고, 상세 조회에서 `priceSnapshot` 합계를 계산해 내려가도록 했다.
- Why:
  - 상세 바텀시트에서 메뉴별 가격과 총액을 함께 보여주려면 백엔드가 합계를 내려주는 편이 일관된다.
- Verification:
  - 코드 수정 후 컴파일 검증을 준비했다.
- Next:
  - 컴파일 확인 후 `#129`를 마무리하고 다음 후속 이슈로 넘긴다.
