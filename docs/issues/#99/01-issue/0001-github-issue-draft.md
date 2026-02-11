# 예약 메뉴 다중 선택 / 추가 입력 / 태그 필터링

Issue: `#99`  
Created: 2026-02-10

---

## 배경

#97에서 예약 ERD v2 핵심(시간축 전환, 점유 블록)을 구현했으나, 메뉴/입력값/태그 관련 기능은 Non-goals로 분리됨. 현재 예약 데이터는 `formDataJson`(비정형 JSON)에 전적으로 의존하고 있어 구조화된 메뉴 관리가 불가능한 상태.

## Goals

- 매장별 메뉴(ShopMenu) CRUD API
- 태그(Tag) 기반 메뉴 필터링 API
- 메뉴별 추가 입력 필드(ShopMenuInputField) CRUD API
- 예약 생성 시 메뉴 다중 선택 + 입력값 저장 (dual-write)
- 예약 조회 시 메뉴/입력값 포함 응답

## Non-goals

- `formDataJson` 완전 제거 (별도 이슈)
- 메뉴별 가격/이미지 관리

## 요구사항

### 기능

- 점주가 메뉴를 생성/수정/비활성화할 수 있다
- 메뉴에 태그를 붙이고, 태그로 필터링할 수 있다
- 메뉴별 추가 입력 필드(NUMBER/TEXT)를 정의할 수 있다
- 고객이 예약 시 메뉴를 여러 개 선택하고, 입력값을 제출할 수 있다
- 예약 조회 시 선택된 메뉴 + 입력값이 응답에 포함된다

### 비기능

- 기존 `formDataJson` 호환 유지 (dual-write)
- 메뉴 삭제 시 soft delete + 스냅샷으로 예약 데이터 보존

## 수용 기준 (AC)

- AC-1: `POST /api/shops/{shopId}/menus`로 메뉴 생성, `GET`으로 활성 메뉴 목록 조회
- AC-2: `GET /api/shops/{shopId}/menus?tagIds=1,2`로 태그 필터링 동작
- AC-3: 메뉴별 입력 필드 CRUD 동작
- AC-4: 예약 생성 API가 `menuIds` + 메뉴별 입력값을 구조화된 형태로 수신하고, `reservation_menu_items` / `reservation_menu_input_values`에 저장한다
- AC-5: 예약 생성 시 기존 `formDataJson`도 함께 저장된다 (dual-write, 레거시 호환)
- AC-6: 예약 조회 응답에 메뉴/입력값 포함 (없으면 `formDataJson` fallback)

## 범위/의존성

- DB 테이블: V1~V3에서 생성, V4에서 `shop_menus` 계열로 리네이밍 + `key` 컬럼 제거
- 의존: #97 (예약 ERD v2 핵심 구현 완료 전제)

## 리스크

- `formDataJson` 의존 코드 산재 → dual-write로 완화
- 기존 예약에 `reservation_menu_items` 없음 → 조회 시 fallback 처리

## 작업 체크리스트

- [ ] V4 마이그레이션 적용 (리네이밍 + key 제거)
- [ ] ShopMenu 엔티티 + CRUD API (Phase 1)
- [ ] Tag + 태그 필터링 API (Phase 2)
- [ ] ShopMenuInputField + CRUD API (Phase 3)
- [ ] 예약-메뉴 선택 + 입력값 저장 (Phase 4)
- [ ] 예약 조회 응답 확장 (Phase 5)

## 관련 문서

- 설계안: `docs/issues/#99/02-design/design-plan.md`
- TDD plan: `docs/issues/#99/04-tdd/plan.md`
- ERD v2: `docs/issues/#97/reservation-erd-v2.md`
