# `link 진입`과 `booking entry` 흐름 정리 백로그

작성일: `2026-03-25`
관련 작업: `#115 링크/채팅 예약 진입에 직원 선택 컨텍스트 추가`
상태: `backlog`

## 배경

기존 공유 링크 진입은 `slugOrCode`를 이용해 매장을 식별한 뒤, 바로 채팅방으로 진입하는 흐름을 전제로 설계돼 있었다.

하지만 예약 시작 전에 `staffId`가 필요해지면서, 링크 진입 직후에는 더 이상 채팅방보다 `예약 진입 컨텍스트`가 먼저 필요해졌다.

이 변화로 인해 아래 세 요소의 책임이 겹쳐 보이기 시작했다.

- `ShortLinkController`
- `LinkController`
- `BookingEntryController`

## 현재 책임 정리

### `ShortLinkController`

- 경로: `GET /s/{slugOrCode}`
- 역할: 브라우저가 백엔드 짧은 링크로 들어왔을 때 프론트 화면 URL로 `302 redirect` 한다.
- 이 컨트롤러는 데이터를 주는 API가 아니라, 사용자의 브라우저를 프론트 엔드 화면으로 보내는 진입점이다.

예시:

```http
GET https://api.example.com/s/abc123
302 Location: https://app.example.com/s/abc123
```

즉 이후 실제 데이터 조회는 프론트 화면이 뜬 뒤 별도 API 호출로 진행된다.

### `LinkController`

- 경로: `GET /link/chat/{slugOrCode}`
- 역할: `slugOrCode -> shopId`를 해석한 뒤, 로그인 사용자의 채팅방을 조회하거나 생성한다.
- 본질적으로는 `slugOrCode`를 `shopId`로 바꿔서 기존 채팅방 API를 대신 호출하는 thin wrapper 에 가깝다.

### `BookingEntryController`

- 경로:
  - `GET /api/public/shops/{slugOrCode}/booking-entry`
  - `GET /api/v1/shops/{shopId}/booking-entry`
- 역할: 예약 시작 전에 필요한 `shopId`, `shopName`, `staffs`, `defaultStaffId`를 제공한다.
- 링크 진입과 채팅방 예약 버튼 진입이 같은 응답 계약을 쓰도록 만드는 API다.

## 현재와 목표 흐름

### 기존 링크 중심 흐름

1. 사용자가 공유 링크 `.../s/{slugOrCode}` 로 진입
2. 프론트 또는 별도 API가 `slugOrCode`를 이용해 채팅방 진입 시도
3. `LinkController`가 `shopId`를 해석하고 채팅방을 생성/조회
4. 채팅 화면에서 예약 버튼 진입

이 구조에서는 예약 전 `staffId` 확보가 늦다.

### 변경 후 목표 흐름

1. 사용자가 공유 링크 `.../s/{slugOrCode}` 로 진입
2. `ShortLinkController`가 프론트 `.../s/{slugOrCode}` 화면으로 리다이렉트
3. 프론트가 `GET /api/public/shops/{slugOrCode}/booking-entry` 호출
4. `shopId`, `staffs`, `defaultStaffId` 확보
5. 예약이면 `staffId` 선택 후 availability 호출
6. 채팅이면 `shopId`로 기존 채팅방 API 호출

이 흐름에서는 `slugOrCode`를 `shopId`로 바꾸는 기준 API가 `LinkController`가 아니라 `BookingEntryController`가 된다.

## 왜 `ShortLinkController`는 대체되지 않는가

`BookingEntryController`는 JSON 응답 API이고, `ShortLinkController`는 브라우저 리다이렉트 엔드포인트다.

둘은 응답 방식과 책임이 다르다.

- `ShortLinkController`는 브라우저 이동
- `BookingEntryController`는 프론트 데이터 조회

따라서 공유 링크를 계속 백엔드 도메인 `.../s/{slugOrCode}` 로 발급하는 한, `ShortLinkController`는 여전히 필요하다.

단, 공유 링크를 아예 프론트 도메인 `https://frontend/s/{slugOrCode}` 로 직접 발급하도록 바꾸면 그때는 제거 후보가 될 수 있다.

## 왜 `LinkController`는 제거 후보인가

`LinkController`의 현재 가치은 사실상 하나다.

- `slugOrCode`를 받아 `shopId`를 찾아 채팅방을 열어준다.

하지만 `BookingEntryController`가 이미 공개 경로에서 `slugOrCode -> shopId`를 해석하고, 예약/채팅 모두에 필요한 진입 컨텍스트를 제공하게 되면 프론트는 더 이상 `LinkController`를 거칠 이유가 없다.

프론트는 아래처럼 직접 처리할 수 있다.

- 링크 진입 후 `booking-entry` 호출
- 예약이면 `staffId` 기반 캘린더 진입
- 채팅이면 `shopId`로 `GET /chat/rooms/shop/{shopId}` 호출

즉 `LinkController`는 새 플로우에서 중복 책임을 가진 래퍼 API가 된다.

## 정리 방향 제안

- `ShortLinkController`는 유지
- `BookingEntryController`를 링크/예약 진입의 표준 API로 사용
- `LinkController`는 프론트 전환 완료 후 deprecated 또는 제거 검토
- 문서와 프론트 플로우에서도 `slugOrCode -> booking-entry -> shopId/staffId 확보`를 기본 시나리오로 통일

## 확인이 필요한 항목

- 프론트가 현재도 `LinkController`를 직접 호출하는지
- 링크 진입 후 무조건 채팅으로 보내는 UX가 아직 남아 있는지
- 공유 링크 발급 주소를 장기적으로 백엔드 `/s/...` 로 유지할지, 프론트 `/s/...` 로 바꿀지

## 완료 기준 초안

- 링크 진입 후 프론트 표준 흐름이 `booking-entry` 기반으로 정리된다.
- 채팅 진입은 `shopId` 기반 채팅방 API를 사용한다.
- `LinkController`가 더 이상 초기 진입 표준 경로가 아니라고 문서화된다.
- 필요 시 `LinkController` deprecate 또는 제거 계획이 후속 이슈로 분리된다.
