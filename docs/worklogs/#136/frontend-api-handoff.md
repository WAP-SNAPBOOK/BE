# `#136` 예약 취소 메타데이터 프론트 전달 문서

작성일: `2026-06-18`

## 이번 변경

- 예약 취소 요청은 사유를 포함해야 한다.
- 취소 시 예약 레코드에 취소 메타데이터를 저장한다.
- 취소 응답에는 취소 메타데이터를 함께 내려준다.

## API 계약

### 예약 취소

- `PUT /api/reservations/{id}/cancel`
- Request body:
  - `reason`: `string`, 필수
- Response:
  - 기존 예약 상태 응답 필드
  - `canceledByType`
  - `canceledByUserId`
  - `canceledAt`
  - `cancelReason`
  - `cancelTiming`
  - `refundEligible`

## 저장 규칙

- `canceledByType`
  - 현재 구현은 점주 취소 흐름을 기준으로 `OWNER`를 저장한다.
- `canceledByUserId`
  - 취소를 실행한 사용자 ID를 저장한다.
- `canceledAt`
  - 서버 시간 기준 취소 시각을 저장한다.
- `cancelTiming`
  - 예약일 이전이면 `BEFORE_CUTOFF`
  - 예약일 당일이면 `AFTER_CUTOFF`
- `refundEligible`
  - `cancelTiming=BEFORE_CUTOFF`이면 `true`
  - 그 외에는 `false`

## 프론트 처리

- 취소 모달에서 사유를 입력받아 전송한다.
- 취소 완료 후 상세/목록 화면은 서버 응답 기준으로 다시 그린다.
- 취소 메타데이터는 상세 바텀시트 또는 상태 영역에서 노출할 수 있다.
