# `#125` 프론트 API Handoff - 점주 메뉴/카테고리 관리

작성일: `2026-05-01`
대상 브랜치: `feature/jiseob/#125`
관련 PR: `https://github.com/WAP-SNAPBOOK/BE/pull/126`

## 목적

점주용 메뉴/카테고리 관리 화면에서 사용할 백엔드 API 계약을 정리한다.

이번 변경의 핵심은 카테고리 관리용 API다. 기존 고객 예약 화면용 태그 조회 API는 활성 메뉴에 연결된 태그만 반환하므로, 점주 관리 화면에서는 새로 추가된 관리용 조회 API를 사용해야 한다.

## 인증

- 모든 API는 JWT 인증이 필요하다.
- 점주 소유 매장이 아닌 `shopId`로 호출하면 `SHOP_OWNER_MISMATCH`가 반환된다.
- 프론트는 기존 인증 헤더 정책을 그대로 사용하면 된다.

## 1. 내 매장 `shopId` 확인

점주 마이페이지에서 메뉴/카테고리 관리 화면으로 이동하기 전에 `/shop/link` 응답의 `shopId`를 사용한다.

```http
GET /shop/link
```

### Response `200`

```json
{
  "shopId": 1,
  "fullUrl": "https://domain.com/s/my-shop",
  "canonicalUrl": "/s/my-shop",
  "slug": "my-shop",
  "publicCode": "ABC12345"
}
```

### 프론트 사용

- `shopId`를 `/mypage/menus` 화면의 필수 상태로 사용한다.
- `shopId`가 없거나 API가 실패하면 메뉴/카테고리 관리 화면 진입 전에 매장 등록 또는 오류 상태를 보여준다.

## 2. 카테고리 전체 조회

점주 관리 화면의 카테고리 칩 목록은 이 API를 사용한다.

```http
GET /api/shops/{shopId}/tags/manage
```

### Response `200`

```json
[
  {
    "id": 101,
    "name": "컷"
  },
  {
    "id": 102,
    "name": "펌"
  }
]
```

### 중요

- 이 API는 메뉴 연결 여부와 관계없이 해당 매장의 모든 카테고리를 반환한다.
- 고객 예약 화면용 `GET /api/shops/{shopId}/tags`와 다르다.
- 관리 화면에서는 반드시 `/tags/manage`를 기본 조회 API로 사용한다.

## 3. 카테고리 생성

```http
POST /api/shops/{shopId}/tags
```

### Request

```json
{
  "name": "컬러"
}
```

### Response `201`

```json
{
  "id": 103,
  "name": "컬러"
}
```

### 주요 에러

- `409 Conflict` / `SHOP_TAG_ALREADY_EXISTS`

### 프론트 처리

- 생성 성공 후 `GET /api/shops/{shopId}/tags/manage`를 다시 조회한다.
- 중복 이름이면 입력창 아래에 "이미 존재하는 카테고리입니다." 같은 메시지를 표시한다.

## 4. 카테고리 이름 수정

```http
PATCH /api/shops/{shopId}/tags/{tagId}
```

### Request

```json
{
  "name": "클리닉"
}
```

### Response `200`

```json
{
  "id": 103,
  "name": "클리닉"
}
```

### 주요 에러

- `400 Bad Request` / `SHOP_TAG_MISMATCH`
- `403 Forbidden` / `SHOP_OWNER_MISMATCH`
- `409 Conflict` / `SHOP_TAG_ALREADY_EXISTS`

### 프론트 처리

- 수정 성공 후 카테고리 목록과 현재 선택된 카테고리 라벨을 갱신한다.
- 같은 이름으로 수정하는 요청은 프론트에서 먼저 막아도 된다.

## 5. 카테고리 삭제

```http
DELETE /api/shops/{shopId}/tags/{tagId}
```

### Response `200`

본문 없음.

### 동작

- 해당 카테고리와 메뉴의 연결을 제거한 뒤 카테고리를 삭제한다.
- 현재 백엔드 스키마에는 태그용 `deleted_at` 또는 `is_visible`이 없어 물리 삭제로 처리한다.

### 주요 에러

- `400 Bad Request` / `SHOP_TAG_MISMATCH`
- `403 Forbidden` / `SHOP_OWNER_MISMATCH`

### 프론트 처리

- 삭제 전 확인 모달을 권장한다.
- 삭제 성공 후 카테고리 목록을 다시 조회한다.
- 삭제한 카테고리가 선택 중이었다면 첫 번째 카테고리 또는 전체 메뉴 상태로 이동한다.

## 6. 카테고리 정렬

```http
PUT /api/shops/{shopId}/tags/order
```

### Request

```json
{
  "tagIds": [102, 101, 103]
}
```

### Response `200`

본문 없음.

### 주의

- 현재 구현은 visible 태그 전체를 전달해야 하는 정책이다.
- 관리 화면에서 아직 메뉴와 연결되지 않은 카테고리까지 드래그 정렬하려면 백엔드 정책 조정이 추가로 필요할 수 있다.
- 이번 화면의 1차 연동에서는 정렬 UI를 생략하거나, 연결된 카테고리 정렬로 제한하는 편이 안전하다.

## 7. 메뉴 조회/필터

카테고리 칩 선택 시 기존 메뉴 조회 API를 사용한다.

```http
GET /api/shops/{shopId}/menus?tagIds={tagId}
```

### 예시

```http
GET /api/shops/1/menus?tagIds=101
```

### Response `200`

```json
[
  {
    "id": 10,
    "shopId": 1,
    "name": "남성 컷",
    "description": "기본 남성 커트",
    "isActive": true,
    "sortOrder": 0
  }
]
```

### 프론트 처리

- 카테고리 미선택 상태에서는 `GET /api/shops/{shopId}/menus`로 전체 활성 메뉴를 조회한다.
- 카테고리 선택 상태에서는 `tagIds`에 선택한 카테고리 `id`를 전달한다.
- 여러 카테고리를 동시에 선택하는 UI라면 `tagIds=101&tagIds=102` 형식의 배열 쿼리를 사용한다.

## 8. 메뉴 생성/수정/숨김

기존 API를 사용한다.

```http
POST /api/shops/{shopId}/menus
PATCH /api/shops/{shopId}/menus/{menuId}
DELETE /api/shops/{shopId}/menus/{menuId}
```

### 메뉴 생성 Request

```json
{
  "name": "남성 컷",
  "description": "기본 남성 커트",
  "sortOrder": 0
}
```

### 메뉴 수정 Request

```json
{
  "name": "여성 컷",
  "description": "기본 여성 커트",
  "sortOrder": 1
}
```

### 메뉴 숨김

- `DELETE /api/shops/{shopId}/menus/{menuId}`는 물리 삭제가 아니라 비활성화 처리다.
- 프론트 문구는 "삭제"보다 "숨기기" 또는 "비활성화"가 더 정확하다.

## 9. 메뉴와 카테고리 연결/해제

```http
POST /api/shops/{shopId}/menus/{menuId}/tags
DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}
```

### 연결 Request

```json
{
  "tagId": 101
}
```

### 프론트 처리

- 새 연동에서는 `/tags/manage` 응답의 `id`를 `tagId`로 사용한다.
- 메뉴 추가 모달에서 카테고리를 선택하게 한 뒤 메뉴 생성 성공 후 연결 API를 호출한다.
- 메뉴 수정에서 카테고리를 바꾸는 경우:
  - 새 카테고리 연결 API 호출
  - 기존 카테고리 해제 API 호출
  - 이후 메뉴 목록과 카테고리 목록을 다시 조회

## 10. 권장 화면 초기 로딩 순서

1. `GET /shop/link`로 `shopId` 확인
2. `GET /api/shops/{shopId}/tags/manage`로 카테고리 전체 조회
3. `GET /api/shops/{shopId}/menus`로 전체 활성 메뉴 조회
4. 첫 카테고리를 기본 선택한다면 `GET /api/shops/{shopId}/menus?tagIds={tagId}` 재조회

## 11. Mutation 후 재조회 기준

- 카테고리 생성: `/tags/manage` 재조회
- 카테고리 이름 수정: `/tags/manage`, 현재 메뉴 목록 재조회
- 카테고리 삭제: `/tags/manage`, 현재 메뉴 목록 재조회
- 메뉴 생성: 메뉴 목록 재조회, 필요한 경우 태그 연결 후 재조회
- 메뉴 수정: 메뉴 목록 재조회
- 메뉴 숨김: 메뉴 목록 재조회, visible 태그 변화 가능성이 있으면 고객용 태그 조회도 재조회
- 메뉴-카테고리 연결/해제: 메뉴 목록, `/tags/manage` 재조회

## 12. 에러 응답 형태

에러 응답은 기존 공통 포맷을 따른다.

```json
{
  "code": "SHOP_TAG_ALREADY_EXISTS",
  "message": "이미 존재하는 매장 태그입니다.",
  "path": "/api/shops/1/tags",
  "traceId": "trace-id",
  "timestamp": "2026-05-01T12:00:00Z",
  "details": null
}
```

## 13. 현재 제한사항

- 카테고리 삭제는 소프트 삭제가 아니다.
- 카테고리 정렬 API는 현재 visible 태그 기준 정책이라, 관리 화면 전체 카테고리 정렬에는 바로 맞지 않을 수 있다.
- 메뉴 응답에는 연결된 카테고리 목록이 포함되지 않는다. 메뉴 수정 모달에서 기존 카테고리 값을 정확히 보여줘야 한다면 메뉴 상세 또는 메뉴별 태그 응답 확장이 추가로 필요하다.
