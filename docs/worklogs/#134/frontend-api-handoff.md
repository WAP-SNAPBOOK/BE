# `#134` 예약 변경 이력 프론트 전달 문서

작성일: `2026-06-18`

## 범위

예약 수정 시 before/after JSON을 저장한다.  
이번 이슈는 저장 구조만 포함하며, 이력 조회 화면은 후속 이슈로 분리한다.

## API 영향

- 예약 수정 API `PATCH /api/reservations/{reservationId}`는 동작은 유지한다.
- 수정 시 내부적으로 `reservation_change_histories`에 before/after JSON이 쌓인다.
- 프론트 응답 계약은 바뀌지 않는다.

## 화면 해석

- 예약 수정 후에도 상세 바텀시트는 기존 `ReservationDetailResponse`를 그대로 사용한다.
- 변경 이력은 나중에 별도 조회 화면이 붙을 때 사용한다.
