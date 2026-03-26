# `#109` 프론트 전달용 API 변경 안내

작성일: `2026-03-23`

## 목적

이 문서는 프론트에서 `#109` 태그 기능 연동 시 알아야 할
현재 백엔드 API 계약만 빠르게 확인할 수 있도록 정리한 문서다.

핵심 방향은 아래와 같다.

- 태그는 이제 전역 `Tag`가 아니라 **매장 로컬 `ShopTag`** 기준으로 본다.
- 프론트의 기본 사용 경로는 `/api/shops/{shopId}/tags` 계열이다.
- 구 전역 `/api/tags` 경로는 아직 살아 있지만 **deprecated** 상태다.
- 메뉴 태그 연결 API는 과도기 동안 legacy `tagId`와 new `shopTagId`를 모두 받아준다.

---

## 가장 먼저 알아야 할 점

### 1. 태그 생성/조회/정렬은 `shopId` 스코프다

이제 프론트가 태그를 직접 다룰 때 기본 경로는 아래다.

- `POST /api/shops/{shopId}/tags`
- `GET /api/shops/{shopId}/tags`
- `PUT /api/shops/{shopId}/tags/order`

응답 태그 형태는 공통으로 아래다.

```json
{
  "id": 123,
  "name": "손관리"
}
```

여기서 `id`는 **`shopTagId`**라고 이해하면 된다.

### 2. 메뉴 태그 연결 API의 `tagId`는 과도기 상태다

현재 요청 필드 이름은 아직 `tagId`지만,
실제로는 아래 둘 다 받아준다.

- legacy 전역 `tags.id`
- new 매장 로컬 `shop_tags.id`

즉 프론트가 새 구조로 붙을 때는
`GET /api/shops/{shopId}/tags`에서 받은 `id`를 그대로 보내면 된다.

### 3. `/api/tags`는 새 구현 기준이 아니다

아래 경로는 아직 제거되지는 않았지만 deprecated 상태다.

- `POST /api/tags`
- `GET /api/tags`

새 화면/새 연동에서는 쓰지 않는 것을 권장한다.

---

## 권장 연동 흐름

### 운영자 화면

1. `POST /api/shops/{shopId}/tags`로 태그 생성
2. `GET /api/shops/{shopId}/tags`로 현재 visible 태그 목록 조회
3. 드래그 정렬 후 `PUT /api/shops/{shopId}/tags/order`
4. 메뉴에 태그 연결 시 `POST /api/shops/{shopId}/menus/{menuId}/tags`

### 사용자 화면

1. `GET /api/shops/{shopId}/tags`로 visible 태그 조회
2. 필요 시 이 `id`를 메뉴 필터/연결 요청에 사용

---

## API 상세

## 1. `POST /api/shops/{shopId}/tags`

### 용도

점주가 자기 매장에 태그를 생성한다.

### 요청

```json
{
  "name": "손관리"
}
```

### 성공 응답

`201 Created`

```json
{
  "id": 101,
  "name": "손관리"
}
```

### 에러

- `403 Forbidden`
  - 코드: `SHOP_OWNER_MISMATCH`
  - 의미: 로그인 사용자가 해당 매장 점주가 아님

- `409 Conflict`
  - 코드: `SHOP_TAG_ALREADY_EXISTS`
  - 의미: 같은 매장에 같은 이름 태그가 이미 존재함

### 프론트 주의사항

- 같은 이름 중복 생성은 이제 재사용이 아니라 **에러**다.
- 생성 성공 후 응답의 `id`를 이후 `shopTagId`처럼 사용하면 된다.

---

## 2. `GET /api/shops/{shopId}/tags`

### 용도

사용자에게 보여줄 visible 태그 목록을 조회한다.

### 응답

`200 OK`

```json
[
  { "id": 101, "name": "손관리" },
  { "id": 102, "name": "발관리" }
]
```

### 현재 visible 기준

- 활성 메뉴에 연결된 태그만 내려간다.
- 같은 태그가 여러 메뉴에 붙어 있어도 한 번만 내려간다.
- `shop_tags.sort_order` 순서대로 내려간다.

### 프론트 주의사항

- 이 API 응답의 `id`가 새 구조 기준 태그 식별자다.
- 이후 메뉴 연결/삭제/정렬에서 이 값을 기준으로 쓰는 쪽이 맞다.

---

## 3. `PUT /api/shops/{shopId}/tags/order`

### 용도

운영자가 visible 태그 순서를 변경한다.

### 요청

```json
{
  "tagIds": [102, 101, 103]
}
```

### 성공 응답

`200 OK`

바디 없음

### 중요한 규칙

- partial update가 아니다.
- **현재 visible 태그 전체를 순서대로** 보내야 한다.
- hidden 태그는 요청에 포함하지 않는다.
- hidden 태그는 백엔드가 기존 상대 순서를 유지한 채 뒤에 둔다.

### 에러

- `403 Forbidden`
  - 코드: `SHOP_OWNER_MISMATCH`

- `400 Bad Request`
  - 코드: `INVALID_SHOP_TAG_ORDER`
  - 예시 원인:
    - visible 태그 일부만 보냄
    - 중복 ID를 보냄
    - 현재 visible 집합과 다른 ID를 보냄

### 프론트 주의사항

- 드래그 정렬 후 visible 리스트 전체를 그대로 보내야 한다.
- hidden 태그를 프론트가 직접 다룰 필요는 없다.

---

## 4. `POST /api/shops/{shopId}/menus/{menuId}/tags`

### 용도

메뉴에 태그를 연결한다.

### 요청

```json
{
  "tagId": 101
}
```

### 성공 응답

`200 OK`

바디 없음

### 현재 과도기 계약

이 요청의 `tagId`는 현재 아래 둘 다 허용된다.

- legacy 전역 `tags.id`
- new `shop_tags.id`

하지만 프론트는 새 구조 기준으로 아래처럼 쓰는 것을 권장한다.

- `GET /api/shops/{shopId}/tags`에서 받은 `id` 전달

### 에러

- `400 Bad Request`
  - 코드: `SHOP_MENU_MISMATCH`
  - 의미: `shopId`와 `menuId` 소속이 맞지 않음

- `400 Bad Request`
  - 코드: `SHOP_TAG_MISMATCH`
  - 의미: 다른 매장 태그를 연결하려고 함

### 프론트 주의사항

- 필드 이름은 아직 `tagId`지만 의미는 점진적으로 `shopTagId`로 가는 중이다.
- 새 연동에서는 `GET /api/shops/{shopId}/tags` 응답의 `id`를 그대로 사용하면 된다.

---

## 5. `DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}`

### 용도

메뉴에서 태그 연결을 제거한다.

### 성공 응답

`200 OK`

바디 없음

### 현재 과도기 계약

삭제 경로도 현재는 아래 둘 다 받아준다.

- legacy 전역 `tags.id`
- new `shop_tags.id`

프론트는 새 구조 기준으로 `shopTagId`를 쓰는 쪽을 권장한다.

### 에러

- `400 Bad Request`
  - 코드: `SHOP_MENU_MISMATCH`

- `400 Bad Request`
  - 코드: `SHOP_TAG_MISMATCH`

---

## Deprecated API

## 6. `POST /api/tags`

전역 태그 생성 API다.  
현재는 deprecated 상태이며 새 화면에서는 사용하지 않는 것을 권장한다.

## 7. `GET /api/tags`

전역 태그 목록 API다.  
현재는 deprecated 상태이며 새 화면에서는 사용하지 않는 것을 권장한다.

---

## 프론트 체크리스트

- 새 태그 생성은 반드시 `POST /api/shops/{shopId}/tags`를 사용한다.
- 태그 목록은 `GET /api/shops/{shopId}/tags`를 기준으로 렌더링한다.
- 메뉴 태그 연결/삭제 시 `GET /api/shops/{shopId}/tags`에서 받은 `id`를 사용한다.
- 정렬 저장 시 visible 태그 전체 배열을 `PUT /api/shops/{shopId}/tags/order`로 보낸다.
- 새 기능에서는 `/api/tags`를 사용하지 않는다.

---

## 한 줄 정리

프론트는 이제 **매장 태그 목록을 `GET /api/shops/{shopId}/tags`로 받고,
그 응답의 `id`를 기준으로 생성/연결/삭제/정렬을 처리한다**고 이해하면 된다.
