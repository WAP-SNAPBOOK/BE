# `reservation` 도메인 논의 로그

작성일: `2026-03-10`

논의 주제:

- `reservation` 도메인의 테스트 전 요구사항 확정
- 취소 정책 및 상태 모델 정리
- 에러 계약과 점주 수정 범위 정리

공유된 전제/컨텍스트:

- 현재 구현 코드를 기준으로 도메인을 파악했다.
- 목적은 테스트 개선과 객체지향 관점 리팩터링 전에 요구사항을 닫는 것이다.
- `reservation` 도메인은 기본 예약 생성/확정/거절/조회는 있으나, 취소/수정/availability 연계/요구사항 저장은 불완전했다.

합의 사항:

- 예약 생성 시 availability 정책을 통과한 경우에만 `PENDING`을 생성한다.
- availability 정책 적용 대상은 영업시간, 휴무일, 예약 가능 기간, 최소 리드타임, 과거 날짜/시간이다.
- 같은 `staffId + date + time`의 `PENDING` 중복 접수는 허용한다.
- `requirements`는 저장 대상이며, 목록 응답에는 제외하고 상세 조회에는 포함한다.
- 고객과 점주 모두 취소할 수 있다.
- 취소 상태는 `CANCELED` 하나로 통일한다.
- 고객은 예약일 전날 `23:59:59`까지 취소 가능하다.
- 예약일 `00:00`부터 고객 셀프 취소 및 환불은 불가다.
- 당일 고객이 연락했고 점주가 슬롯을 비우는 경우는 `NO_SHOW`가 아니라 `CANCELED`로 본다.
- `NO_SHOW`는 "연락 없이 오지 않음"의 후속 확장 상태로만 언급한다.
- `COMPLETED`, `NO_SHOW`는 현재 구현 범위에는 넣지 않는다.
- 예약금은 우리 서비스가 직접 관리하지 않는다.
- 환불 판단과 실제 환불은 점주가 별도로 수행한다.
- `CANCELED`에는 취소 메타데이터를 둔다.
- 최소 취소 메타데이터는 `canceledByType`, `canceledByUserId`, `canceledAt`, `cancelReason`, `cancelTiming`, `refundEligible`다.
- 상태 전이 실패는 액션별 비즈니스 에러 코드로 `409`를 사용한다.
- 고객 취소 마감 위반은 `CUSTOMER_CANCELLATION_DEADLINE_PASSED`로 `409`를 사용한다.
- 예약 생성 availability 위반은 `400`, 시간 충돌은 `409`로 처리한다.
- 메뉴/입력값 검증 실패도 표준 비즈니스 에러로 내린다.
- 점주는 `date`, `time`, `staffId`, `menuSelections`, 입력값, `requirements`, 이미지까지 모두 수정할 수 있다.
- `CONFIRMED` 상태에서 `date/time/staffId` 변경 시 기존 점유를 해제하고 재생성한다.
- 재생성 충돌 시 전체 변경은 실패하고 기존 상태/점유를 유지한다.
- `GET /api/reservations/shop/{shopId}/availability`는 삭제한다.
- 예약 가능 시간 판단 책임은 `availability` 도메인으로 단일화한다.

미합의/보류 사항:

- 상세 조회 응답에 `staffId`, `startAt`, `durationMinutes`를 포함할지 여부
- `COMPLETED`, `NO_SHOW`를 실제 구현 범위로 끌어올릴 시점
- 취소 메타데이터를 값 객체로 둘지, 엔티티 필드로 펼칠지의 구체 설계

액션 아이템:

- `reservation` 도메인 확정 명세를 기준으로 테스트 시나리오를 작성한다.
- 현재 구현과 확정 명세의 갭을 기준으로 리팩터링/기능 보완 작업을 나눈다.
- 취소 API, 수정 API, availability 연계, 에러 코드 정리를 후속 구현 범위로 계획한다.

관련 링크:

- [분석 초안](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#107/02-analysis/0005-reservation-current-prd-spec-ac-draft.md)
- [확정 명세](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/docs/issues/#107/03-decisions/0001-reservation-domain-spec-final.md)
