# `#115` Booking Entry Design

작성일: `2026-03-25`

## 문제 정의

예약 시작 전에 `staffId`가 필요하지만, 현재 공개 링크와 채팅방 응답은 직원 선택 정보를 제공하지 않는다.

## 선택한 방향

- 예약 전용 `booking entry` 응답을 별도로 만든다.
- 링크 진입과 채팅 예약 버튼 진입은 같은 응답 계약을 사용한다.
- `slugOrCode` 해석은 공통 조회 메서드로 모은다.

## 응답 초안

```json
{
  "shopId": 1,
  "shopName": "네일샵",
  "defaultStaffId": 10,
  "staffs": [
    { "staffId": 10, "name": "원장" },
    { "staffId": 11, "name": "직원A" }
  ]
}
```

## 엔드포인트

- `GET /api/public/shops/{slugOrCode}/booking-entry`
- `GET /api/v1/shops/{shopId}/booking-entry`

## 정렬 정책

- 현재는 `displayOrder`가 없으므로 `id ASC`를 기본 정렬로 사용한다.
- `defaultStaffId`는 첫 번째 직원의 `staffId`로 설정한다.

## 제외 범위

- availability 계산 로직 변경
- 예약 생성 로직 변경
- 직원 공개 정책 추가
