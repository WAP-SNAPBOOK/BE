# `#135` 예약 자동 메시지 본문 프론트 전달 문서

작성일: `2026-06-18`

## 범위

예약 이벤트가 고객 채팅방에 실제 본문을 남기도록 한다.
예약 수정 메시지는 기존 문자열 본문과 구조화된 변경 스냅샷을 함께 제공한다.

## 결정사항

- `ReservationEvent`는 본문 `content`를 포함한다.
- 시스템 메시지 writer는 본문이 비어 있으면 메시지 타입에 맞는 기본 문구를 저장한다.
- 예약 수정의 `message`는 `예약 정보가 변경되었습니다.`로 유지한다.
- `reservationChange`에는 실제로 값이 달라진 필드만 포함한다.
- 점주 전달사항의 전·후 값은 `reservationChange.ownerMessage`에 포함한다.
- 최상위 `ownerMessage` 문자열은 하위 호환을 위해 유지한다.
- 조회 API와 WebSocket은 모두 `MessageResponse`를 사용한다.

## 예약 수정 응답 계약

요청은 변경할 필드만 보내면 된다.

```json
{
  "date": "2026-06-24",
  "startAt": "14:00",
  "durationMinutes": 90,
  "staffId": 2,
  "menuSelections": [
    {
      "menuId": 10,
      "tagId": 3,
      "inputValues": [
        {
          "fieldId": 1,
          "valueNumber": null,
          "valueText": "화이트"
        }
      ]
    }
  ],
  "message": "고객 요청으로 예약 날짜와 시간을 변경했습니다."
}
```

- `date`만 보내면 기존 시간을 유지한 채 날짜만 변경한다.
- `startAt`만 보내면 기존 날짜를 유지한 채 시간만 변경한다.
- `date`와 `startAt`을 함께 보내면 둘을 결합한 새 시작 시점으로 변경한다.
- `menuSelections`를 생략하면 기존 예약 메뉴를 유지한다.
- `menuSelections`를 포함하면 기존 예약 메뉴를 전체 교체한다.
- `menuSelections: []`는 예약 메뉴 전체 제거로 처리한다.

## 예약 확정 요청 계약

확정 시에도 날짜와 시간을 조정할 수 있다.

```json
{
  "date": "2026-06-24",
  "startAt": "14:00",
  "durationMinutes": 90,
  "message": "예약 가능 시간에 맞춰 날짜와 시간을 조정했습니다."
}
```

- `date`를 생략하면 기존 예약 날짜를 유지한다.
- `startAt`을 생략하면 기존 예약 시간을 유지한다.
- `date`만 보내면 기존 시간을 유지한 채 날짜만 변경한다.
- `startAt`만 보내면 기존 날짜를 유지한 채 시간만 변경한다.
- `date`와 `startAt`을 함께 보내면 둘을 결합한 새 시작 시점으로 확정한다.
- 확정 점유 블록은 최종 확정된 날짜와 시간 기준으로 생성된다.

```json
{
  "messageType": "RESERVATION_UPDATED",
  "reservationId": 123,
  "message": "예약 정보가 변경되었습니다.",
  "reservationChange": {
    "date": {
      "before": "2026-06-23",
      "after": "2026-06-24"
    },
    "startAt": {
      "before": "13:00",
      "after": "14:00"
    },
    "durationMinutes": {
      "before": 60,
      "after": 90
    },
    "staff": {
      "before": {
        "staffId": 1,
        "staffName": "담당자 A"
      },
      "after": {
        "staffId": 2,
        "staffName": "담당자 B"
      }
    },
    "ownerMessage": {
      "before": "기존 점주 전달사항",
      "after": "고객 요청으로 예약 시간을 변경했습니다."
    },
    "menus": {
      "before": [
        {
          "menuId": 1,
          "menuName": "기본 네일",
          "tagName": "손",
          "price": 30000,
          "sortOrder": 0,
          "inputValues": []
        }
      ],
      "after": [
        {
          "menuId": 10,
          "menuName": "프렌치 네일",
          "tagName": "손",
          "price": 45000,
          "sortOrder": 0,
          "inputValues": [
            {
              "inputFieldId": 1,
              "fieldLabel": "요청사항",
              "inputType": "TEXT",
              "valueNumber": null,
              "valueText": "화이트"
            }
          ]
        }
      ]
    }
  },
  "ownerMessage": "고객 요청으로 예약 시간을 변경했습니다."
}
```

## 필드 규칙

- `reservationChange.date`: 날짜가 실제로 변경된 경우에만 존재하며 값은 `yyyy-MM-dd` 형식이다.
- `reservationChange.startAt`: 시간이 실제로 변경된 경우에만 존재하며 값은 `HH:mm` 형식이다.
- `reservationChange.durationMinutes`: 소요시간이 실제로 변경된 경우에만 존재한다.
- `reservationChange.staff`: 담당자가 실제로 변경된 경우에만 존재하며 전·후 `staffId`, `staffName`을 제공한다.
- `reservationChange.menus`: 메뉴가 실제로 변경된 경우에만 존재하며 전·후 메뉴 스냅샷 배열을 제공한다.
- 메뉴 스냅샷의 `menuId`는 예약 당시 선택한 `shopMenuId`이다.
- 메뉴 입력값 스냅샷은 `inputFieldId`, `fieldLabel`, `inputType`, `valueNumber`, `valueText`를 포함한다.
- 구조화할 변경이 없으면 `reservationChange`는 `null`이다.
- 전달사항이 없거나 공백이면 `ownerMessage`는 `null`이다.
- 점주 전달사항이 실제로 변경되면 `reservationChange.ownerMessage`에 전·후 값이 제공된다.
- 최상위 `ownerMessage`는 하위 호환을 위해 `reservationChange.ownerMessage.after`와 같은 문자열을 유지한다.
- 기존 `message`는 채팅 목록 미리보기와 기존 클라이언트 호환을 위해 계속 제공한다.

## 화면 해석

- 채팅방 목록과 메시지 히스토리에서 예약 상태 변화를 문장으로 읽을 수 있다.
- 예약 수정 메시지는 `reservationChange`를 변경 전 → 변경 후 비교 UI로 표시한다.
- `reservationChange.ownerMessage`도 다른 변경 필드와 동일한 비교 UI 규칙으로 표시한다.
- 기존 `ownerMessage`만 사용하는 화면은 변경 없이 유지할 수 있다.
