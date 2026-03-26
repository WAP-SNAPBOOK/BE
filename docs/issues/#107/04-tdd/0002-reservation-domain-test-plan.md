# `#107` `reservation/domain` 테스트 시나리오 계획

작성일: `2026-03-09`

## Ground Rules

- Red -> Green -> Refactor 순서를 지킨다.
- 구조 변경과 동작 변경을 섞지 않는다.
- 먼저 순수 도메인 단위 테스트를 추가하고, 그 다음 JPA 테스트를 정리한다.
- 한 번에 테스트 1개씩 추가한다.
- 기존 JPA 테스트를 지우기 전에 더 강한 계약 테스트가 먼저 있어야 한다.

## Scope

### Goals

- `Reservation`의 상태 전이와 예외 규칙을 순수 단위 테스트로 보호한다.
- `reservation/domain`의 기존 JPA 테스트를 유지 가치 기준으로 재분류한다.
- snapshot / unique constraint 성격의 persistence 테스트는 필요한 수준으로 남긴다.

### Non-goals

- 서비스 레이어 검증
- API 계약 검증
- DB 스키마 변경
- `ReservationTimeBlock`까지 포함한 전체 예약 흐름 검증

## Test List

- [x] `createReservation`이 초기 상태와 파생 필드를 올바르게 만든다
  - 목적: 생성 규칙의 기본 계약 보호
  - 입력: `shopId`, `ownerUserId`, `customerId`, `date`, `time`, `designImageURLs`
  - 출력: `status=PENDING`, `startAt=date+time`, 나머지 입력값 그대로 보존
  - 엣지케이스: 빈 `designImageURLs`
  - 관측 포인트: getter 값

- [ ] `confirm`은 `PENDING` 예약을 `CONFIRMED`로 바꾸고 메시지와 소요시간을 저장한다
  - 목적: 확정 상태 전이 검증
  - 입력: `message`, `durationMinutes`
  - 출력: `status=CONFIRMED`, `confirmationMessage`, `durationMinutes` 반영
  - 엣지케이스: `message=null`, `durationMinutes=null`
  - 관측 포인트: 상태와 필드 값

- [ ] `confirm`은 `PENDING`이 아닌 예약에서 예외를 던진다
  - 목적: 잘못된 상태 전이 차단
  - 입력: 이미 `CONFIRMED`, `REJECTED`, `CANCELED` 상태의 예약
  - 출력: `IllegalStateException`
  - 엣지케이스: 상태별 예외 메시지 동일 여부
  - 관측 포인트: 예외 타입과 상태 유지

- [ ] `reschedule`은 `PENDING` 예약의 `time`과 `startAt`을 함께 바꾼다
  - 목적: 시간 변경 시 파생 필드 동기화 보장
  - 입력: 새 `LocalTime`
  - 출력: `time` 변경, `startAt=date+newTime`
  - 엣지케이스: 같은 시간으로 재조정
  - 관측 포인트: `time`, `startAt`

- [ ] `reschedule`은 `PENDING`이 아닌 예약에서 예외를 던진다
  - 목적: 확정/거절/취소 후 시간 변경 방지
  - 입력: 비정상 상태 예약과 새 시간
  - 출력: `IllegalStateException`
  - 엣지케이스: 상태별 동일 정책 유지
  - 관측 포인트: 예외 타입과 기존 시간 유지

- [ ] `reject`는 `PENDING` 예약을 `REJECTED`로 바꾸고 거절 사유를 저장한다
  - 목적: 거절 상태 전이 검증
  - 입력: 거절 사유 문자열
  - 출력: `status=REJECTED`, `rejectionReason` 저장
  - 엣지케이스: 빈 문자열 또는 `null` 사유 정책 현행 유지
  - 관측 포인트: 상태와 필드 값

- [ ] `reject`는 `PENDING`이 아닌 예약에서 예외를 던진다
  - 목적: 잘못된 거절 전이 방지
  - 입력: 이미 처리된 예약
  - 출력: `IllegalStateException`
  - 엣지케이스: `CONFIRMED`, `CANCELED`, `REJECTED`
  - 관측 포인트: 예외 타입과 상태 유지

- [ ] `cancel`은 `PENDING` 예약을 `CANCELED`로 바꾼다
  - 목적: 취소 가능 상태 검증
  - 입력: `PENDING` 예약
  - 출력: `status=CANCELED`
  - 엣지케이스: 추가 필드 변화 없음
  - 관측 포인트: 상태 값

- [ ] `cancel`은 `CONFIRMED` 예약도 취소할 수 있다
  - 목적: 확정 후 취소 허용 규칙 고정
  - 입력: `CONFIRMED` 예약
  - 출력: `status=CANCELED`
  - 엣지케이스: 메시지/소요시간 값 유지
  - 관측 포인트: 상태와 기존 필드 유지

- [ ] `cancel`은 이미 `CANCELED` 또는 `REJECTED` 상태에서 예외를 던진다
  - 목적: 중복 취소/거절 후 취소 방지
  - 입력: `CANCELED`, `REJECTED` 예약
  - 출력: `IllegalStateException`
  - 엣지케이스: 상태별 동일 정책 유지
  - 관측 포인트: 예외 타입과 상태 유지

- [ ] `ReservationMenuItem`은 메뉴 이름 snapshot을 저장 후에도 유지한다
  - 목적: 원본 메뉴 변경과 예약 스냅샷 분리
  - 입력: 예약 생성 후 `ReservationMenuItem.create(...)`, 이후 원본 메뉴명 변경
  - 출력: 저장된 `menuNameSnapshot`은 원래 값 유지
  - 엣지케이스: 다른 필드 변경과 무관
  - 관측 포인트: DB 재조회 후 `menuNameSnapshot`

- [ ] `ReservationMenuItem`은 동일 예약에서 같은 `shopMenuId`를 중복 저장할 수 없다
  - 목적: unique constraint 계약 보호
  - 입력: 같은 `reservationId`, 같은 `shopMenuId`
  - 출력: 두 번째 저장 시 `DataIntegrityViolationException`
  - 엣지케이스: `sortOrder`가 달라도 중복 불가
  - 관측 포인트: 예외 타입과 flush 시점

- [ ] `ReservationMenuItem`은 다른 예약이면 같은 `shopMenuId`를 저장할 수 있다
  - 목적: 허용 케이스 계약을 명시
  - 입력: 다른 `reservationId`, 같은 `shopMenuId`
  - 출력: 두 row 모두 저장 성공
  - 엣지케이스: 동일 이름/정렬값 여부와 무관
  - 관측 포인트: 저장 개수와 핵심 필드

- [ ] `ReservationMenuInputValue`는 숫자형 snapshot 값을 저장/조회할 수 있다
  - 목적: 숫자 입력 snapshot 매핑 보호
  - 입력: `valueNumber=5`, `valueText=null`
  - 출력: 숫자 값 보존
  - 엣지케이스: 소수점 포함 값
  - 관측 포인트: DB 재조회 후 `valueNumber`, `valueText`

- [ ] `ReservationMenuInputValue`는 텍스트형 snapshot 값을 저장/조회할 수 있다
  - 목적: 텍스트 입력 snapshot 매핑 보호
  - 입력: `valueNumber=null`, `valueText="프렌치"`
  - 출력: 텍스트 값 보존
  - 엣지케이스: 빈 문자열 정책 현행 유지
  - 관측 포인트: DB 재조회 후 `valueText`, `valueNumber`

- [ ] `ReservationMenuInputValue`는 같은 `reservationMenuItemId`와 `fieldId` 조합을 중복 저장할 수 없다
  - 목적: unique constraint 계약 보호
  - 입력: 같은 `reservationMenuItemId`, 같은 `shopMenuInputFieldId`
  - 출력: 두 번째 저장 시 `DataIntegrityViolationException`
  - 엣지케이스: label/value가 달라도 중복 불가
  - 관측 포인트: 예외 타입과 flush 시점

- [ ] `ReservationMenuInputValue`는 다른 `reservationMenuItemId`이면 같은 `fieldId`를 저장할 수 있다
  - 목적: 허용 케이스 계약을 명시
  - 입력: 다른 `reservationMenuItemId`, 같은 `shopMenuInputFieldId`
  - 출력: 두 row 모두 저장 성공
  - 엣지케이스: 값 종류가 달라도 허용
  - 관측 포인트: 저장 개수와 핵심 필드

## Notes

- 첫 구현 단위는 `ReservationUnitTest`다.
- 기존 `ReservationCreateStartAtDualWriteRedTest`는 단위 테스트가 자리 잡은 후 rename 또는 흡수한다.
- `ReservationStartAtJpaMappingTest`, `ReservationStaffIdJpaMappingTest`, `ReservationDurationMinutesJpaMappingTest`는 마지막 단계에서 유지 가치 재평가 대상이다.
- `go`를 사용할 경우 위 체크리스트의 다음 미체크 1개만 진행한다.
