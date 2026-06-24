# `#129` 예약 상세 총액 프론트 전달 문서

작성일: `2026-06-18`

## 범위

예약 상세 응답에 총액 필드가 추가된다.  
이번 이슈는 예약 상세 바텀시트에서 메뉴별 가격과 함께 합계 금액을 보여주기 위한 최소 보강이다.

## 결정사항

- 예약 상세 API는 기존 경로를 유지한다.
- `menus[]`는 그대로 사용한다.
- `totalPrice`는 메뉴별 `priceSnapshot` 합계다.
- `priceSnapshot`이 `null`인 메뉴는 합산에서 제외한다.
- 모든 메뉴가 `null`이면 `totalPrice`도 `null`이다.

## 응답 예시

```json
{
  "id": 10,
  "menus": [
    {
      "menuNameSnapshot": "젤네일",
      "priceSnapshot": 50000
    },
    {
      "menuNameSnapshot": "파츠추가",
      "priceSnapshot": 10000
    }
  ],
  "totalPrice": 60000
}
```

## 화면 해석

- `totalPrice`가 있으면 그대로 총액으로 표시한다.
- `totalPrice`가 `null`이면 상세 바텀시트에서 `총액 미정`으로 표시한다.
- 메뉴별 가격 표시는 기존 `menus[].priceSnapshot`을 그대로 사용한다.
