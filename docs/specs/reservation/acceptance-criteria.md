## Acceptance Criteria

### 확정된 정보
- Given 예약 가능한 슬롯이 존재하고 고객이 필수 정보를 입력했을 때
  When 예약 생성 요청을 보내면
  Then 예약은 `PENDING` 상태로 생성된다.

- Given 동일 `staffId + date + time` 슬롯에 기존 `PENDING` 예약이 있을 때
  When 고객이 같은 슬롯으로 새 예약 요청을 보내면
  Then 새 예약 요청도 생성된다.

- Given 고객이 과거 날짜/시간, 휴무일, 운영시간 밖, 예약 가능 기간 밖, 최소 리드타임 미달, 예약 불가 슬롯으로 요청할 때
  When 예약 생성을 시도하면
  Then 예약 생성은 실패한다.

- Given 고객이 예약 생성 시 `requirements`, 이미지, 메뉴, 입력값을 함께 제출했을 때
  When 예약이 생성되면
  Then 해당 정보는 저장된다.

- Given 점주가 `PENDING` 예약을 처리할 권한을 가지고 있을 때
  When 예약을 확정하면
  Then 예약은 `CONFIRMED`가 되고 점유 블록이 생성된다.

- Given 확정 대상 예약과 시간 블록이 충돌할 때
  When 점주가 예약을 확정하거나 시간 관련 정보를 수정하면
  Then 요청은 실패하고 기존 상태와 기존 점유는 유지된다.

- Given 점주가 `PENDING` 예약을 거절할 권한을 가지고 있을 때
  When 예약을 거절하면
  Then 예약은 `REJECTED`가 된다.

- Given 고객이 예약일 전날 `23:59:59` 이전에 자신의 예약을 취소할 때
  When 취소 요청을 보내면
  Then 예약은 `CANCELED`가 된다.

- Given 고객이 예약일 `00:00` 이후 자신의 예약을 셀프 취소할 때
  When 취소 요청을 보내면
  Then 요청은 `CUSTOMER_CANCELLATION_DEADLINE_PASSED`로 실패한다.

- Given 점주가 운영상 예약을 취소할 때
  When 점주가 취소 요청을 보내면
  Then 예약은 `CANCELED`가 되고, `CONFIRMED`였다면 점유 블록은 해제된다.

- Given 예약이 `CANCELED`로 변경될 때
  When 취소 처리가 완료되면
  Then `canceledByType`, `canceledByUserId`, `canceledAt`, `cancelReason`, `cancelTiming`, `refundEligible`가 저장된다.

- Given 점주가 `PENDING` 또는 `CONFIRMED` 예약을 수정할 권한을 가지고 있을 때
  When `date`, `time`, `staffId`, `requirements`, 이미지, 메뉴, 입력값 중 일부를 수정하면
  Then 요청한 항목이 반영된다.

- Given 고객 또는 점주가 예약 목록을 조회할 때
  When 목록 응답이 반환되면
  Then 목록에는 `requirements`가 포함되지 않는다.

- Given 고객 또는 점주가 예약 상세를 조회할 때
  When 상세 응답이 반환되면
  Then `requirements`, 메뉴 스냅샷, 입력값 스냅샷이 포함된다.

- Given 상태 전이가 허용되지 않는 예약에 대해 확정, 거절, 취소를 시도할 때
  When 요청을 보내면
  Then 액션별 상태 전이 에러 코드로 실패한다.

- Given 메뉴 또는 입력값이 정의된 규칙을 위반할 때
  When 예약 생성 또는 수정을 시도하면
  Then 표준 비즈니스 에러 코드로 실패한다.

### 열린 질문
- 상세 조회 응답에 `staffId`, `startAt`, `durationMinutes`를 포함할지 여부

### 가정
- 테스트 시나리오 세분화는 이 AC를 기준으로 후속 문서에서 파생한다.
