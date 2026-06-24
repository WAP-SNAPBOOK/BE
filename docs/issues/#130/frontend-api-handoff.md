# `#130` 예약 취소 프론트 전달 문서

작성일: `2026-06-18`

## 범위

점주용 예약 상세 바텀시트에서 예약 취소를 호출할 수 있게 한다.  
이번 이슈는 취소 상태 전환과 `CONFIRMED` 예약 점유 블록 해제까지만 포함한다.

## API

```http
PUT /api/reservations/{reservationId}/cancel
Authorization: Bearer {accessToken}
```

### 요청

- 요청 바디 없음

### 응답

기존 `ReservationStatusResponse`를 그대로 사용한다.

## 화면 해석

- 성공 시 예약 상태는 `CANCELED`다.
- 취소된 예약은 캘린더 기본 화면에서 숨긴다.
- 상세 바텀시트에서는 취소 상태만 표시한다.
