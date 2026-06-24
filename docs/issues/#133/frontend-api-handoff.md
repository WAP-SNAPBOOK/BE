# `#133` 예약 수정 프론트 전달 문서

작성일: `2026-06-18`

## 범위

점주가 확정된 예약을 수정할 수 있게 한다.  
이번 이슈는 수정 동작 자체만 포함하고, 수정 자동 메시지와 변경 이력은 후속 이슈에서 다룬다.

## API

```http
PATCH /api/reservations/{reservationId}
Authorization: Bearer {accessToken}
```

### Request

```json
{
  "startAt": "10:30",
  "durationMinutes": 60,
  "staffId": 1,
  "message": "변경된 시간으로 확정합니다"
}
```

- `startAt`: 선택, `HH:mm`
- `durationMinutes`: 선택, 10분 단위
- `staffId`: 선택, 담당 직원 변경 시 사용
- `message`: 선택, 점주 전달 메시지 갱신용

## Response

- 성공 시 최신 `ReservationDetailResponse`를 그대로 반환한다.
- 프론트는 이 응답으로 상세 바텀시트와 캘린더 블록을 다시 그릴 수 있다.

## 화면 해석

- 수정 대상은 `CONFIRMED` 예약만 가능하다.
- 수정 후에는 시작 시간, 소요시간, 담당 직원이 최신 값으로 바뀐다.
- 예약 블록과 상세 바텀시트는 새 응답 기준으로 갱신한다.
