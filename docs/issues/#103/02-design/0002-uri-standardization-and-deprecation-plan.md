# #103 Design Plan — URI 표준화 + 기존→신규 매핑 + Deprecation 일정

Created: 2026-03-02  
Issue: `#103`

---

## 1. 문제 재정의

현재 API URI는 `/api/v1`, `/api`, 루트 경로(`/shop`, `/user`, `/chat`, `/link`, `/dev`)가 혼재되어 있어 확장/운영/문서 동기화 비용이 증가한다.  
본 문서는 URI 표준을 확정하고, 기존 경로를 신규 표준 경로로 점진 전환하기 위한 매핑 및 폐기 일정을 정의한다.

---

## 2. 설계안 비교 (A/B)

### 설계안 A: 현행 유지 + 규약 문서만 추가

- 장점
  - 개발 비용 최소
  - 단기 리스크 낮음
- 단점
  - 경로 혼재가 지속되어 신규 API 설계 품질이 다시 흔들림
  - 문서-구현 드리프트 재발 가능성 높음
- 판단
  - 단기 방어는 가능하지만 구조적 문제를 해결하지 못함

### 설계안 B: 점진 통합 (권장)

- 장점
  - 호환성 유지하면서 `/api/v1`로 표준화 가능
  - 클라이언트 영향 최소화(구경로 alias + sunset 운영)
  - 운영 관측 지표를 기반으로 안전하게 폐기 가능
- 단점
  - 일정 기간 구/신규 경로 동시 운영 필요
  - 라우팅/문서/테스트 관리 복잡도 일시 증가
- 판단
  - 안정성과 개선의 균형이 가장 좋음

**결정: 설계안 B 채택**

---

## 3. URI 표준안 (Target Contract)

### 3.1 공통 규칙

- REST 공통 prefix: `/api/v1`
- 리소스명은 복수형 명사 사용: `shops`, `users`, `reservations`, `chat-rooms`
- trailing slash 금지: `/chat-rooms/` 대신 `/chat-rooms`
- 생성 응답: `201 Created` + 가능하면 `Location` 헤더 제공
- 상태 전이는 action path 남용 대신 상태 리소스/필드로 표현
- 예외 경로는 명시적으로 허용
  - 공개 단축 링크: `/s/{slugOrCode}` (유지)
  - WebSocket handshake: `/ws-connect` (현행 유지, 차기 `/ws/v1/connect` 검토)

### 3.2 네이밍 규칙

- 단수 경로 금지: `/shop`, `/user` -> `/shops`, `/users`
- 도메인 경계를 반영한 중첩만 허용
  - 허용: `/shops/{shopId}/menus/{menuId}`
  - 금지: 의미가 중복되거나 소유관계가 불명확한 경로

---

## 4. 기존 → 신규 URI 매핑표

| Domain | Method | 기존 URI | 신규 URI(표준) | 비고 |
|---|---|---|---|---|
| Auth | POST | `/oauth/login/kakao` | `/api/v1/auth/oauth/kakao/login` | 공개 유지 |
| Auth | POST | `/oauth/login/kakao/local` | `/api/v1/auth/oauth/kakao/login/local` | 공개 유지 |
| Auth | POST | `/auth/refresh` | `/api/v1/auth/tokens/refresh` | 공개 유지 |
| Auth | POST | `/auth/token-validation` | `/api/v1/auth/tokens/validation` | 공개 유지 |
| User | GET | `/user/me` | `/api/v1/users/me` | 인증 |
| User | POST | `/user/customer/signup` | `/api/v1/users/registrations/customer` | temp user |
| User | POST | `/user/owner/signup` | `/api/v1/users/registrations/owner` | temp user |
| Shop | POST | `/shop` | `/api/v1/shops` | `201` 통일 |
| Shop | GET | `/shop/{slugOrCode}` | `/api/v1/shops/{shopKey}` | `shopKey=slugOrCode` |
| Shop | GET | `/shop/info/{shopId}` | `/api/v1/shops/{shopId}` | 중복 조회 통합 |
| Shop/Link | GET | `/shop/link` | `/api/v1/shops/me/link` | 인증 |
| Shop/Link | PUT | `/shop/link/slug` | `/api/v1/shops/me/link` (`PATCH`) | slug 부분 수정 |
| Link | GET | `/link/chat/{slugOrCode}` | `/api/v1/links/{shopKey}/chat-room` | 링크 기반 방 조회/생성 |
| Public Link | GET | `/s/{slugOrCode}` | `/s/{slugOrCode}` | 공개 예외, 유지 |
| Menu | POST | `/api/shops/{shopId}/menus` | `/api/v1/shops/{shopId}/menus` | prefix 통일 |
| Menu | GET | `/api/shops/{shopId}/menus` | `/api/v1/shops/{shopId}/menus` | prefix 통일 |
| Menu | PATCH | `/api/shops/{shopId}/menus/{menuId}` | `/api/v1/shops/{shopId}/menus/{menuId}` | prefix 통일 |
| Menu | DELETE | `/api/shops/{shopId}/menus/{menuId}` | `/api/v1/shops/{shopId}/menus/{menuId}` | prefix 통일 |
| InputField | POST | `/api/shops/{shopId}/menus/{menuId}/input-fields` | `/api/v1/shops/{shopId}/menus/{menuId}/input-fields` | prefix 통일 |
| InputField | GET | `/api/shops/{shopId}/menus/{menuId}/input-fields` | `/api/v1/shops/{shopId}/menus/{menuId}/input-fields` | prefix 통일 |
| InputField | PATCH | `/api/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}` | `/api/v1/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}` | prefix 통일 |
| InputField | DELETE | `/api/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}` | `/api/v1/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}` | prefix 통일 |
| Tag | POST | `/api/tags` | `/api/v1/tags` | prefix 통일 |
| Tag | GET | `/api/tags` | `/api/v1/tags` | prefix 통일 |
| Tag | POST | `/api/shops/{shopId}/menus/{menuId}/tags` | `/api/v1/shops/{shopId}/menus/{menuId}/tags` | prefix 통일 |
| Tag | DELETE | `/api/shops/{shopId}/menus/{menuId}/tags/{tagId}` | `/api/v1/shops/{shopId}/menus/{menuId}/tags/{tagId}` | prefix 통일 |
| Form | GET | `/api/v1/form/{shopId}` | `/api/v1/shops/{shopId}/form` | 리소스 정규화 |
| Form | PATCH | `/api/v1/form/shops/{shopId}` | `/api/v1/shops/{shopId}/form` | 리소스 정규화 |
| Reservation | GET | `/api/reservations/{id}` | `/api/v1/reservations/{id}` | prefix 통일 |
| Reservation | POST | `/api/reservations` | `/api/v1/reservations` | prefix 통일 |
| Reservation | PUT | `/api/reservations/{id}/confirm` | `/api/v1/reservations/{id}/status` (`PATCH`) | `status=CONFIRMED` |
| Reservation | PUT | `/api/reservations/{id}/reject` | `/api/v1/reservations/{id}/status` (`PATCH`) | `status=REJECTED` |
| Reservation | GET | `/api/reservations/my` | `/api/v1/reservations/me` | 조회 주체 명시 |
| Reservation | GET | `/api/reservations/shop` | `/api/v1/shops/me/reservations` | 점주 컨텍스트 명시 |
| Reservation | GET | `/api/reservations/shop/{shopId}/availability` | `/api/v1/shops/{shopId}/reservation-availability` | prefix 통일 |
| Reservation | GET | `/api/reservations/chat/customer` | `/api/v1/shops/{shopId}/reservations/me` | `shopId` query -> path |
| Reservation | GET | `/api/reservations/chat/owner` | `/api/v1/shops/{shopId}/customers/{customerId}/reservations` | query -> path |
| Availability | GET | `/api/v1/shops/{shopId}/staff/{staffId}/availability` | 유지 | 이미 표준 부합 |
| Availability | GET | `/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly` | 유지 | 이미 표준 부합 |
| Schedule | GET | `/api/v1/shops/{shopId}/schedule/settings` | 유지 | 유지 |
| Schedule | PUT | `/api/v1/shops/{shopId}/schedule/interval` | `/api/v1/shops/{shopId}/schedule/settings` (`PATCH`) | 문서-코드 드리프트 해소 필요 |
| Schedule | PUT | `/api/v1/shops/{shopId}/schedule/operating-times` | 유지 | 유지 |
| Schedule | GET | `/api/v1/shops/{shopId}/schedule/operating-times` | 유지 | 유지 |
| Schedule | GET | `/api/v1/shops/{shopId}/schedule/holidays` | 유지 | 유지 |
| Schedule | POST | `/api/v1/shops/{shopId}/schedule/holidays` | 유지 | 유지 |
| Schedule | DELETE | `/api/v1/shops/{shopId}/schedule/holidays/{holidayId}` | 유지 | 유지 |
| Staff Times | PUT | `/api/v1/shops/{shopId}/staff/{staffId}/operating-times` | 유지 | 유지 |
| Staff Times | GET | `/api/v1/shops/{shopId}/staff/{staffId}/operating-times` | 유지 | 유지 |
| Chat REST | GET | `/chat/rooms/shop/{shopId}` | `/api/v1/chat-rooms/by-shop/{shopId}` | prefix 통일 |
| Chat REST | GET | `/chat/rooms/` | `/api/v1/chat-rooms` | trailing slash 제거 |
| Chat REST | PATCH | `/chat/rooms/{chatRoomId}/last-read-message` | `/api/v1/chat-rooms/{chatRoomId}/last-read-message` | prefix 통일 |
| Chat REST | GET | `/chat/rooms/{chatRoomId}/messages` | `/api/v1/chat-rooms/{chatRoomId}/messages` | prefix 통일 |
| Slot(legacy) | GET | `/api/slot/{shopId}/available-dates` | `/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly` | v1 availability로 이관 |
| Slot(legacy) | GET | `/api/slot/{shopId}/available-times` | `/api/v1/shops/{shopId}/staff/{staffId}/availability` | v1 availability로 이관 |
| Slot(legacy) | POST | `/api/slot/{shopId}/slots` | 신규 생성 금지(Deprecate) | 운영 데이터만 read 허용 |
| File | POST | `/api/files/upload` | `/api/v1/files:upload` 또는 `/api/v1/files/uploads` | 규약 확정 필요 |
| File | POST | `/api/files/upload-multiple` | `/api/v1/files:upload-multiple` 또는 `/api/v1/files/uploads/batch` | 규약 확정 필요 |
| Dev | DELETE | `/dev/user` | `/api/v1/dev/users/me` | 프로파일/역할 제한 |
| Dev | DELETE | `/dev/dev/user/{userId}` | `/api/v1/dev/users/{userId}` | 중복 prefix 제거 |
| WS | CONNECT | `/ws-connect` | 유지(단, `/ws/v1/connect` 후보) | 보안규칙 분리 관리 |
| WS | SEND | `/pub/chat/{chatRoomId}` | 유지(또는 `/ws/v1/pub/chat-rooms/{chatRoomId}`) | 차기 버전 검토 |
| WS | SUBSCRIBE | `/topic/chat/{chatRoomId}` | 유지(또는 `/ws/v1/topic/chat-rooms/{chatRoomId}`) | 차기 버전 검토 |

---

## 5. Deprecation 일정 (고정 날짜)

기준일: 2026-03-02

### Phase 1. ADD (2026-03-09 ~ 2026-03-20)

- 신규 표준 URI 추가
- 기존 URI는 동일 동작 alias로 유지
- 응답 헤더 추가
  - `Deprecation: true`
  - `Sunset: 2026-06-30`
  - `Link: <신규 URI>; rel="successor-version"`

### Phase 2. SWITCH (2026-03-23 ~ 2026-05-15)

- API 문서/SDK/프론트 기본 경로를 신규 URI로 전환
- 모니터링 지표
  - 구URI 호출 비율
  - 구URI 호출 클라이언트 식별(UA, token subject, app version)
- SLO
  - 구URI 비율 10% 미만 도달 시 다음 단계 진행

### Phase 3. SUNSET NOTICE 강화 (2026-05-18 ~ 2026-06-15)

- 구URI 호출 시 warning 로그 + 경고 응답 바디(비기능)
- 운영 공지 2회 이상 배포

### Phase 4. CLEANUP (2026-06-30)

- 구URI 라우팅 제거
- 문서에서 구URI 완전 삭제
- 회귀 검증 완료 후 종료

---

## 6. 단계별 롤아웃 (ADD → backfill → switch → cleanup)

### 6.1 ADD

- 신규 경로 컨트롤러/매핑 추가
- 기존 경로는 신규 서비스 호출로 위임(동일 비즈니스 로직)
- OpenAPI에 `deprecated: true` 표시

### 6.2 backfill

- URI 전환은 데이터 backfill이 필요하지 않음
- 대신 클라이언트 경로 사용 현황 backfill 지표를 수집/정리

### 6.3 switch

- 프론트/모바일/외부 연동을 신규 URI로 순차 전환
- 운영 대시보드에서 구URI 호출 잔량 추적

### 6.4 cleanup

- 구URI alias 제거
- 보안 설정(`allowUrls`, 인증 예외)에서 구경로 제거
- 문서와 테스트의 구경로 케이스 제거

---

## 7. 리스크 및 롤백

### 리스크

1. 일부 클라이언트가 구경로에 고정되어 Sunset 이후 장애 발생  
2. 구/신규 동시 운영 중 권한 정책 불일치 발생  
3. 예약/가용시간(`slot` vs `availability`) 경로 전환 중 기능 혼선

### 완화

- 구URI 사용량 지표 기반 단계 전환
- 신규/구경로 공통 인가 필터 및 소유권 검증 재사용
- `slot`은 쓰기 중단 우선, 읽기만 한시 유지

### 롤백

- Sunset 전: 구URI alias 재활성화로 즉시 복구 가능
- Sunset 후: 특정 클라이언트 대상 임시 allowlist 라우팅(기간 제한) 후 재종료

---

## 8. 테스트 전략 (TDD + 체크리스트)

### 8.1 Contract 테스트

- [ ] 동일 기능에 대해 구URI/신규URI 응답 본문/상태코드 동등성 검증
- [ ] 신규 생성 API `201 Created` 및 `Location` 헤더 검증
- [ ] trailing slash 요청(`/chat-rooms/`)의 처리 정책(redirect 또는 404) 고정

### 8.2 Security 테스트

- [ ] 공개 API(`auth`, `/s/**`, `ws-connect`)만 permitAll인지 검증
- [ ] 신규 URI에서 `@RequireAuthenticatedUser`/소유권 검증 누락 없는지 검증

### 8.3 Deprecation 테스트

- [ ] 구URI 응답에 `Deprecation`, `Sunset`, `Link` 헤더 포함 검증
- [ ] Sunset 날짜 이후 구URI가 의도한 에러(`404` 또는 `410`) 반환하는지 검증

### 8.4 엣지 케이스

- [ ] query 기반 기존 API를 path 기반 신규 API로 변환 시 파라미터 누락 처리
- [ ] `confirm/reject` -> `status PATCH` 변환에서 유효하지 않은 상태값 거부
- [ ] 구URI와 신규URI가 동시에 호출될 때 idempotency 보장

---

## 9. 구현 순서 요약

1. 표준 규칙 문서 확정(본 문서)  
2. 신규 URI 추가 + 구URI alias 연결  
3. 관측/Deprecation 헤더 적용  
4. 클라이언트 전환  
5. Sunset 도달 후 구URI 제거

---

## 10. 참고

- `docs/issues/#103/00-intake/0002-api-catalog-and-design-review.md`
- `src/main/java/com/example/easybooking/**/*Controller.java`
- `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
