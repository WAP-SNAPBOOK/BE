# API 설계 규칙

이 문서는 `SNAPBOOK_BE`의 신규 API 설계와 기존 API 정리 기준을 정의한다.

목표는 REST 원칙을 기계적으로 강제하는 것이 아니라, 클라이언트가 예측 가능한 API를 쓰고 서버가 장기적으로 유지보수 가능한 URI/메서드/상태코드 체계를 갖도록 하는 것이다.

## 1. 기본 원칙

- 신규 비즈니스 API는 기본적으로 `/api/v1` 아래에 둔다.
- URI는 행위가 아니라 리소스를 표현한다.
- HTTP method로 행위를 표현한다.
- 리소스명은 복수형을 기본으로 한다.
- 화면 이름, 사용자 역할, 버튼 이름을 URI에 직접 넣지 않는다.
- 조회 `GET`은 서버 상태를 변경하지 않는다.
- 생성/수정/삭제의 성공 상태코드를 일관되게 사용한다.
- 레거시 API는 즉시 제거하지 않고 대체 API를 먼저 제공한 뒤 deprecated로 표시한다.

## 2. Base Path

### 2.1 신규 API

신규 비즈니스 API는 아래 형식을 기본으로 한다.

```http
/api/v1/{resources}
```

예:

```http
GET /api/v1/shops/{shopId}
GET /api/v1/shops/{shopId}/menus
POST /api/v1/reservations
```

### 2.2 예외 허용 경로

아래는 REST 리소스 API와 성격이 달라 예외적으로 `/api/v1` 밖에 둘 수 있다.

- 인증/OAuth: `/auth/**`, `/oauth/**`
- 공개 짧은 링크: `/s/{slugOrCode}`
- 공개 진입 API: `/api/public/**`
- WebSocket handshake/STOMP destination
- 개발 전용 API: `/dev/**`

단, 예외 경로도 문서에 공개/인증/운영 가능 여부를 명시해야 한다.

## 3. URI Naming

### 3.1 리소스는 복수형을 사용한다

권장:

```http
GET /api/v1/shops
GET /api/v1/shops/{shopId}
GET /api/v1/reservations
```

비권장:

```http
GET /shop
GET /user
GET /api/reservation
```

### 3.2 계층 관계는 실제 소유/포함 관계일 때만 중첩한다

권장:

```http
GET /api/v1/shops/{shopId}/menus
GET /api/v1/shops/{shopId}/menus/{menuId}/input-fields
GET /api/v1/shops/{shopId}/staff/{staffId}/availability
```

주의:

```http
GET /api/v1/users/{userId}/shops/{shopId}/menus
```

사용자가 소유자일 수는 있지만 메뉴의 직접 부모는 `shop`이다. 사용자 권한은 인증/인가에서 처리하고 URI 계층에 과하게 넣지 않는다.

### 3.3 화면/역할 이름을 URI에 넣지 않는다

비권장:

```http
GET /api/reservations/my
GET /api/reservations/shop
GET /api/reservations/chat/customer
GET /api/shops/{shopId}/tags/manage
GET /api/owner/shops/{shopId}/calendar
```

권장:

```http
GET /api/v1/me/reservations
GET /api/v1/shops/{shopId}/reservations
GET /api/v1/shops/{shopId}/reservations?customerId={customerId}
GET /api/v1/shops/{shopId}/tags?scope=all
GET /api/v1/shops/{shopId}/calendar
```

역할 기반 접근 제어는 URI가 아니라 인증 사용자와 서비스 인가 로직으로 판단한다.

### 3.4 행위 동사를 URI에 넣지 않는다

비권장:

```http
PUT /api/reservations/{reservationId}/confirm
PUT /api/reservations/{reservationId}/reject
PUT /api/reservations/{reservationId}/cancel
POST /api/files/upload
```

권장 후보:

```http
PATCH /api/v1/reservations/{reservationId}
POST /api/v1/reservations/{reservationId}/status-transitions
POST /api/v1/reservations/{reservationId}/cancellation
POST /api/v1/files
```

단, 상태 전이가 복잡하고 도메인 이벤트 성격이 강하면 `status-transitions`, `cancellation` 같은 하위 리소스로 표현할 수 있다.

## 4. HTTP Method

| Method | 의미 | 사용 기준 |
| --- | --- | --- |
| `GET` | 조회 | 서버 상태를 변경하지 않는다 |
| `POST` | 생성 또는 명령 | 컬렉션에 새 리소스를 만들거나 명령성 작업을 수행한다 |
| `PUT` | 전체 대체 | 리소스 전체 표현을 교체한다 |
| `PATCH` | 부분 수정 | 일부 필드만 변경한다 |
| `DELETE` | 삭제 또는 비활성화 | 리소스를 제거하거나 사용 불가 상태로 만든다 |

### 4.1 GET은 생성하면 안 된다

비권장:

```http
GET /chat/rooms/shop/{shopId}
```

이 API가 채팅방이 없을 때 생성한다면 `GET`이 아니다.

권장:

```http
GET /api/v1/shops/{shopId}/chat-room
POST /api/v1/shops/{shopId}/chat-rooms
```

제품 정책상 “열기 또는 생성”이 필요하면 `POST`를 사용한다.

```http
POST /api/v1/chat-rooms
```

요청 예:

```json
{
  "shopId": 1
}
```

## 5. 상태코드

| 상황 | 상태코드 |
| --- | --- |
| 조회 성공 | `200 OK` |
| 생성 성공 | `201 Created` |
| 수정 성공, 응답 body 있음 | `200 OK` |
| 수정 성공, 응답 body 없음 | `204 No Content` |
| 삭제 성공 | `204 No Content` |
| 잘못된 요청 | `400 Bad Request` |
| 인증 없음/토큰 없음 | `401 Unauthorized` |
| 권한 없음/소유자 아님 | `403 Forbidden` |
| 리소스 없음 | `404 Not Found` |
| 중복/상태 충돌 | `409 Conflict` |

### 5.1 생성 API

생성 API는 `201 Created`를 기본으로 한다.

권장:

```http
POST /api/v1/shops
201 Created
```

현재 `POST /shop`처럼 생성 후 `200 OK`를 반환하는 API는 신규 v1 대체 경로를 만들 때 `201 Created`로 정리한다.

### 5.2 삭제 API

삭제 또는 비활성화 API는 body가 필요 없으면 `204 No Content`를 기본으로 한다.

권장:

```http
DELETE /api/v1/shops/{shopId}/menus/{menuId}
204 No Content
```

비활성화라 하더라도 클라이언트 관점에서 “목록에서 사라짐”이면 `DELETE`를 사용할 수 있다. 단, 문서에 soft delete인지 hard delete인지 명시한다.

## 6. 컬렉션 조회

### 6.1 필터는 query parameter를 사용한다

권장:

```http
GET /api/v1/shops/{shopId}/reservations?customerId=1&status=CONFIRMED
GET /api/v1/shops/{shopId}/menus?tagIds=1,2
GET /api/v1/shops/{shopId}/tags?scope=all
```

비권장:

```http
GET /api/reservations/chat/customer
GET /api/reservations/chat/owner
GET /api/shops/{shopId}/tags/manage
```

### 6.2 현재 사용자 기준 리소스는 `/me`를 사용한다

권장:

```http
GET /api/v1/me
GET /api/v1/me/reservations
GET /api/v1/me/chat-rooms
GET /api/v1/me/shop-link
```

비권장:

```http
GET /user/me
GET /api/reservations/my
GET /chat/rooms/
GET /shop/link
```

## 7. 상태 전이 API

예약처럼 상태 전이가 중요한 도메인은 두 방식 중 하나를 선택한다.

### 7.1 단순 변경은 PATCH

상태 변경이 단순한 필드 수정이면 `PATCH`를 사용한다.

```http
PATCH /api/v1/reservations/{reservationId}
```

요청 예:

```json
{
  "status": "CONFIRMED",
  "message": "내일 방문해주세요",
  "durationMinutes": 60,
  "startAt": "10:30"
}
```

### 7.2 상태 전이 자체가 의미 있으면 하위 리소스

상태 전이에 이력, 사유, 메시지, 부수 효과가 있으면 전이를 리소스로 본다.

```http
POST /api/v1/reservations/{reservationId}/status-transitions
```

요청 예:

```json
{
  "toStatus": "CONFIRMED",
  "message": "내일 방문해주세요",
  "durationMinutes": 60,
  "startAt": "10:30"
}
```

취소가 독립된 의미를 가지면 별도 리소스도 가능하다.

```http
POST /api/v1/reservations/{reservationId}/cancellation
```

요청 예:

```json
{
  "reason": "고객 요청",
  "refundEligible": true
}
```

현재 예약 도메인은 상태 이력과 변경 이력이 있으므로, 장기적으로는 `status-transitions` 또는 `cancellation` 모델을 우선 검토한다.

## 8. 인증과 인가

### 8.1 인증 필요 여부를 문서화한다

각 API 문서는 아래 중 하나를 명시한다.

| 구분 | 의미 |
| --- | --- |
| 공개 | 토큰 없이 호출 가능 |
| 임시 사용자 | 회원가입 전 임시 토큰 필요 |
| 인증 사용자 | 정식 access token 필요 |
| 점주 | 해당 매장의 owner 권한 필요 |
| 관리자 | admin 권한 필요 |

### 8.2 권한은 URI가 아니라 서버에서 검증한다

비권장:

```http
GET /api/owner/shops/{shopId}/calendar
```

권장:

```http
GET /api/v1/shops/{shopId}/calendar
```

서버는 인증 사용자와 `shopId`의 소유자 관계를 검증한다.

### 8.3 컨트롤러 시그니처에 인증 의존성을 드러낸다

인증 사용자 정보가 필요한 API는 가능하면 `@RequireAuthenticatedUser`를 명시한다.

```java
public ResponseEntity<?> getSomething(
        @RequireAuthenticatedUser AuthenticatedUser user
) {
    ...
}
```

전역 Spring Security 설정에만 의존하면 API 코드만 보고 권한 경계를 파악하기 어렵다.

## 9. Request/Response 규칙

### 9.1 요청 DTO와 응답 DTO는 분리한다

요청과 응답은 변경 속도가 다르다. 엔티티를 직접 노출하지 않고 DTO를 사용한다.

### 9.2 응답 필드명은 도메인 기준으로 통일한다

동일 의미에 여러 이름을 쓰지 않는다.

비권장:

```json
{
  "requirements": "요청사항",
  "requests": "요청사항",
  "imageUrls": [],
  "photoUrls": []
}
```

권장:

```json
{
  "requirements": "요청사항",
  "imageUrls": []
}
```

레거시 호환 필드는 deprecated 문서와 제거 일정을 둔다.

### 9.3 시간 표현은 ISO 계열을 기본으로 한다

- 날짜: `yyyy-MM-dd`
- 시간: `HH:mm` 또는 `HH:mm:ss` 중 API별로 하나를 고정
- 일시: ISO local date time

요청과 응답의 시간 포맷이 다르면 문서에 명시하고, 가능하면 동일하게 맞춘다.

## 10. Pagination과 Cursor

목록이 커질 수 있는 API는 처음부터 페이지 또는 커서를 고려한다.

### 10.1 최신순 커서

메시지처럼 증가 ID 기반 조회는 cursor를 사용할 수 있다.

```http
GET /api/v1/chat-rooms/{chatRoomId}/messages?cursor=1000&size=50
```

응답에는 다음 cursor를 포함하는 방식을 권장한다.

```json
{
  "items": [],
  "nextCursor": 950,
  "hasNext": true
}
```

현재 일부 API는 배열만 반환한다. 신규 API에서는 pagination metadata 포함을 우선 검토한다.

## 11. Deprecated API

레거시 API는 즉시 제거하지 않는다.

절차:

1. 신규 v1 API를 추가한다.
2. 기존 API 문서에 deprecated를 표시한다.
3. 프론트 전환 여부를 확인한다.
4. 로그 또는 모니터링으로 호출 잔존 여부를 확인한다.
5. 제거 이슈를 분리한다.

Deprecated 문서에는 아래를 포함한다.

- 대체 API
- 제거 예정 조건
- 응답 호환 차이
- 기존 클라이언트 영향

## 12. 현재 API 정리 우선순위

### 12.1 P0: GET 부수 효과 제거

현재 문제 후보:

```http
GET /chat/rooms/shop/{shopId}
GET /link/chat/{slugOrCode}
```

채팅방이 없을 때 생성한다면 `POST` 계열로 분리한다.

### 12.2 P1: 예약 상태 변경 API 정리

현재 문제 후보:

```http
PUT /api/reservations/{id}/confirm
PUT /api/reservations/{id}/reject
PUT /api/reservations/{id}/cancel
```

대안:

```http
PATCH /api/v1/reservations/{reservationId}
POST /api/v1/reservations/{reservationId}/status-transitions
POST /api/v1/reservations/{reservationId}/cancellation
```

### 12.3 P2: 예약 목록 조회 URI 정리

현재 문제 후보:

```http
GET /api/reservations/my
GET /api/reservations/shop
GET /api/reservations/chat/customer
GET /api/reservations/chat/owner
```

대안:

```http
GET /api/v1/me/reservations
GET /api/v1/shops/{shopId}/reservations
GET /api/v1/shops/{shopId}/reservations?customerId={customerId}
```

### 12.4 P3: Base path 통일

현재 혼재:

```http
/api/v1/**
/api/**
/shop/**
/user/**
/chat/**
/link/**
```

신규 API는 `/api/v1`을 기준으로 만들고, 기존 경로는 대체 API 제공 후 deprecated한다.

### 12.5 P4: 상태코드 정리

우선 정리 대상:

- 생성 API의 `201 Created` 적용
- 삭제/비활성화 API의 `204 No Content` 적용
- 중복 생성/상태 충돌의 `409 Conflict` 적용

## 13. 신규 API 설계 체크리스트

- [ ] URI가 `/api/v1` 아래에 있는가?
- [ ] 리소스명이 복수형인가?
- [ ] URI에 동사, 화면명, 역할명이 들어가지 않았는가?
- [ ] `GET`이 서버 상태를 변경하지 않는가?
- [ ] 생성은 `POST`, 부분 수정은 `PATCH`, 삭제는 `DELETE`를 사용하는가?
- [ ] 생성 성공은 `201 Created`인가?
- [ ] 삭제 성공은 `204 No Content`인가?
- [ ] 현재 사용자 기준 API는 `/me`를 사용하는가?
- [ ] 필터 조건은 query parameter로 표현되는가?
- [ ] 인증/인가 요구사항이 문서에 명시되어 있는가?
- [ ] 소유권 검증 위치가 명확한가?
- [ ] 응답 DTO가 엔티티를 직접 노출하지 않는가?
- [ ] 목록 API에 pagination/cursor 필요 여부를 검토했는가?
- [ ] 기존 API와 중복되면 deprecated/전환 계획이 있는가?

