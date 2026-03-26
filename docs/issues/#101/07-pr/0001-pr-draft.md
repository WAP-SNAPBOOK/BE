# PR Draft - #101 Availability

## Summary
- 점주가 운영 설정(간격/운영시간/휴무일)과 직원 오버라이드를 관리하고, 고객이 일별/월별 예약 가능일을 조회할 수 있도록 예약 가능성 도메인을 완성한다.
- 대안으로 월별 응답을 일별 API 반복 호출로 조합하는 방식도 가능했지만, 서버에서 `availableDates/holidayDates/closedDates`를 한 번에 계산해 캘린더 렌더링 비용과 클라이언트 복잡도를 낮추는 방향을 선택했다.

## Changes
- 점주용 스케줄 API 추가: 설정 조회/수정, 운영시간 조회/수정, 휴무일 조회/추가/삭제.
- 직원 오버라이드 API 추가: 직원별 운영시간 오버라이드 조회/설정 및 매장 운영범위 초과 검증.
- 고객용 가용 조회 API 추가: 일별 슬롯 조회 + 월별 캘린더 조회(`availableDates`, `holidayDates`, `closedDates`).

## Test plan
- TDD 기준 문서: `docs/issues/#101/04-tdd/plan.md`
- 확인한 테스트
  - [x] `com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest`
  - [x] `com.example.easybooking.availability.presentation.StaffOperatingTimesControllerIntegrationTest`
  - [x] `com.example.easybooking.availability.presentation.AvailabilityControllerIntegrationTest`
  - [x] `com.example.easybooking.availability.AvailabilityServiceTest`
- 엣지 케이스/회귀 포인트
  - booking window 경계(월 단위 조회 시 빈 구간 처리)
  - 당일 min booking lead 반영
  - 휴무일 우선순위(정기휴무/공휴일)
  - 직원 `isOff`/오버라이드와 매장 운영시간 충돌 검증
  - 예약 확정 후 `reservation_time_blocks` 점유 반영

## Risks & Rollback
- 위험 요소
  - 월별 분류 로직(available/holiday/closed) 우선순위 변경 시 캘린더 표시 회귀 가능
  - 휴무일 패턴(BIWEEKLY/MONTHLY) 계산 규칙이 정책 변경에 민감
- 롤백/완화 전략
  - 기능 단위 커밋 기준으로 선택 롤백 가능
    - 점주 스케줄 API
    - 휴무일 관리 API
    - 직원 오버라이드 API
    - 고객 가용 조회 API
  - 회귀 발생 시 API 응답 스펙 테스트(`AvailabilityControllerIntegrationTest`)를 우선 실행해 분류 로직부터 복구

## Docs
- `docs/issues/#101/01-scan/problem-scan.md`
- `docs/issues/#101/02-analysis/analysis-options.md`
- `docs/issues/#101/03-adr/adr-101-availability.md`
- `docs/issues/#101/03-adr/discussion-log.md`
- `docs/issues/#101/04-tdd/plan.md`
- `docs/issues/#101/05-green/green-design.md`
- `docs/issues/#101/06-worklog/worklog.md`
- `docs/issues/#101/08-analysis/0001-current-app-feature-flow-analysis.md`
- `docs/issues/#101/08-analysis/0002-cross-issue-user-flow-audit.md`
