# #99 메뉴 다중 선택 / 추가 입력 / 태그 필터링 — Design Plan

Created: 2026-02-10  
Issue: `#99`  
Parent ERD: `docs/issues/#97/reservation-erd-v2.md` (라인 7-9)  
Branch: `develop`

---

## 1. 배경 / 문제

### ERD v2 요구사항 (라인 7-9)

- 시작 시간(`start_at`)과 점유 블록(`block_start_at`)은 **모두 10분 단위**다.
- 예약은 **메뉴를 여러 개 선택 가능**, 메뉴별로 **추가 입력(NUMBER/TEXT)** 이 붙을 수 있다.
- 메뉴는 **태그로 필터링** 가능해야 한다.

### 현황

| 항목 | 상태 |
|------|------|
| DB 테이블 | Flyway V1~V3으로 6개 테이블 존재 → V4에서 `shop_menus`, `shop_menu_tags`, `shop_menu_input_fields`, `reservation_menu_items`, `reservation_menu_input_values` 로 리네이밍 |
| Java 엔티티 | 해당 테이블 매핑 엔티티 **없음** |
| 예약 흐름 | `formDataJson`(비정형 JSON)에 전적 의존 |
| #97 plan.md | 이 기능들은 "Non-goals(후속 이슈)"로 명시됨 |

### 구현 대상 엔티티 (6개)

```
shop_menus                    → ShopMenu              (매장별 메뉴)
tags                          → Tag                   (태그)
shop_menu_tags                → ShopMenuTag            (메뉴-태그 연결)
shop_menu_input_fields        → ShopMenuInputField     (메뉴별 추가 입력 정의)
reservation_menu_items        → ReservationMenuItem    (예약-메뉴 다중 선택 + 스냅샷)
reservation_menu_input_values → ReservationMenuInputValue (예약-메뉴별 입력값 + 스냅샷)
```

---

## 2. 설계안 비교

### 설계안 A: Vertical Slice (기능 단위 수직 관통) — 추천

각 기능을 엔티티 - Repository - Reader/Writer - Service - Controller까지 수직으로 관통하며 구현.

```
Phase 1: ShopMenu(메뉴) CRUD — 엔티티 ~ API
Phase 2: Tag + 태그 기반 메뉴 필터링 — 엔티티 ~ API
Phase 3: ShopMenuInputField(메뉴별 추가 입력 필드) CRUD — 엔티티 ~ API
Phase 4: 예약 생성 시 메뉴 선택 + 입력값 저장 — 엔티티 ~ 서비스
Phase 5: 예약 조회 시 메뉴/입력값 포함 응답 — 서비스 ~ API
```

| 장점 | 단점 |
|------|------|
| 각 Phase 완료 시 동작하는 기능 산출 | 초기에 공통 구조가 안 잡히면 리팩토링 비용 발생 가능 |
| 프론트엔드와 병렬 작업 가능 | |
| TDD Red-Green-Refactor와 잘 맞음 | |
| 빠른 피드백 루프 | |

### 설계안 B: Layered (계층별 일괄 구축)

엔티티 6개 전체 → Reader/Writer 전체 → Service 전체 → API 전체 순서로 구축.

| 장점 | 단점 |
|------|------|
| 구조 일관성, 공통 패턴 한 번에 확립 | API까지 오래 걸림 |
| | Phase별 동작하는 기능 없음 |
| | TDD 사이클이 길어짐 |

### 결정: 설계안 A (Vertical Slice)

**근거**: TDD 사이클에 적합하고, 각 Phase 완료 시 프론트엔드와 병렬 작업이 가능함. 리팩토링 비용은 Phase 간 Refactor 단계에서 흡수.

---

## 3. 단계별 롤아웃 (ADD → Dual-write → Switch → Cleanup)

### 3.1 ADD (이번 이슈 범위)

- 엔티티 클래스 + Repository + Reader/Writer + Service + Controller 추가
- DB 테이블은 V4 리네이밍 후 존재
- 메뉴 CRUD API, 태그 필터링 API, 입력 필드 CRUD API 제공

### 3.2 Dual-write (이번 이슈 범위)

- 예약 생성 시 기존 `formDataJson` **+ 신규 `reservation_menu_items`/`reservation_menu_input_values`** 동시 저장
- 기존 응답 포맷(`formDataJson` 기반) 유지
- 신규 응답 필드(`menus` 배열)를 별도로 추가

### 3.3 Switch (후속 이슈)

- 응답에서 `reservation_menu_items` 기반 데이터를 우선 사용
- `formDataJson`은 fallback으로만 유지

### 3.4 Cleanup (별도 이슈)

- `formDataJson` 의존 완전 제거
- `Reservation.formDataJson` 컬럼 nullable 전환 → 최종 삭제

---

## 4. 패키지 구조

기존 프로젝트 패턴(도메인별 패키지)을 따름.

```
com.example.easybooking.shop.domain/
  ShopMenu.java
  Tag.java
  ShopMenuTag.java
  ShopMenuInputField.java

com.example.easybooking.shop.repository/
  ShopMenuRepository.java
  TagRepository.java
  ShopMenuTagRepository.java
  ShopMenuInputFieldRepository.java

com.example.easybooking.shop/
  ShopMenuReader.java
  ShopMenuWriter.java
  TagReader.java
  TagWriter.java
  ShopMenuInputFieldReader.java
  ShopMenuInputFieldWriter.java

com.example.easybooking.shop.service/
  ShopMenuManagementService.java  (메뉴 CRUD 비즈니스 로직)
  TagService.java                 (태그 관리)

com.example.easybooking.shop.presentation/
  ShopMenuController.java         (메뉴 + 태그 + 입력 필드 API)

com.example.easybooking.shop.dto/
  (요청/응답 DTO)

com.example.easybooking.reservation.domain/
  ReservationMenuItem.java
  ReservationMenuInputValue.java

com.example.easybooking.reservation.repository/
  ReservationMenuItemRepository.java
  ReservationMenuInputValueRepository.java

com.example.easybooking.reservation/
  ReservationMenuItemReader.java
  ReservationMenuItemWriter.java
  ReservationMenuInputValueReader.java
  ReservationMenuInputValueWriter.java
```

---

## 5. 리스크 및 롤백 전략

### 리스크 1: `formDataJson` 의존 코드 산재

- **영향**: 프론트엔드 + 기존 API 응답이 `formDataJson`에 의존
- **완화**: Dual-write 단계에서 기존 응답 포맷 유지. `reservation_menu_items` 데이터는 **별도 필드**로 추가
- **롤백**: `reservation_menu_items` 저장 로직만 제거 → 기존 동작 복원

### 리스크 2: 태그 동시 생성 시 UNIQUE 충돌

- **영향**: 같은 이름의 태그를 동시에 생성하면 `UNIQUE(name)` 위반
- **완화**: `findByName()` → 없으면 `save()` 패턴 + `DataIntegrityViolationException` 시 retry
- **롤백**: 해당 없음 (데이터 정합성 문제없음)

### 리스크 3: 기존 예약에 `reservation_menu_items` 없음

- **영향**: 기존 예약 조회 시 메뉴 데이터 빈 상태
- **완화**: 조회 시 `reservation_menu_items` 비어있으면 `formDataJson` fallback
- **롤백**: fallback 분기 제거

### 리스크 4: `ShopMenu` 삭제 시 예약 참조 무결성

- **영향**: 삭제된 메뉴가 포함된 기존 예약 조회 불가
- **완화**: soft delete(`is_active=false`) + 스냅샷(`reservation_menu_items`에 이름/설명 복사)
- **롤백**: 해당 없음 (스냅샷이 원본과 독립)

---

## 6. 테스트 전략 (엣지 케이스 포함)

> 상세 TDD 테스트 리스트는 `docs/issues/#99/04-tdd/plan.md`에 SSOT로 관리.

### Phase 1: ShopMenu (메뉴) CRUD

| 구분 | 테스트 시나리오 | 엣지 케이스 |
|------|----------------|------------|
| 엔티티 매핑 | ShopMenu가 shop_menus에 매핑 | - |
| 저장 | save 후 ID 할당 | 같은 shop에 동일 이름 → UNIQUE 위반 |
| 저장 검증 | shopId, name 필수 | null/빈 문자열 → 예외 |
| 조회 | findByShopId → 활성 메뉴만, sort_order 순 | 메뉴 없으면 빈 리스트 |
| 조회 | findById → 메뉴 반환 | 없으면 예외 |
| API | POST 메뉴 생성 | 인증 없음 → 401, 다른 shop → 403 |
| API | GET 메뉴 목록 | 비활성 메뉴 제외 |
| API | PATCH 메뉴 수정 | 이름 중복 → 409 |
| API | DELETE 메뉴 비활성화 | 이미 비활성 → 멱등 |

### Phase 2: Tag + 태그 기반 메뉴 필터링

| 구분 | 테스트 시나리오 | 엣지 케이스 |
|------|----------------|------------|
| 엔티티 매핑 | Tag가 tags에 매핑 | - |
| 저장 | 동일 이름 태그 UNIQUE | idempotent 처리 또는 예외 |
| 연결 | ShopMenuTag 메뉴-태그 연결 | 중복 연결 → UNIQUE 위반 |
| 필터링 | tagIds=1 → 해당 태그 메뉴 | 존재하지 않는 tagId → 빈 결과 |
| 필터링 | tagIds=1,2 → OR 필터 | 태그 없는 메뉴 → 필터 시 제외 |
| 필터링 | tagIds 미지정 → 전체 활성 메뉴 | 비활성 메뉴 → 무조건 제외 |

### Phase 3: ShopMenuInputField (메뉴별 추가 입력 필드)

| 구분 | 테스트 시나리오 | 엣지 케이스 |
|------|----------------|------------|
| 엔티티 매핑 | InputField가 shop_menu_input_fields에 매핑 | - |
| 저장 | 같은 메뉴에 동일 label → UNIQUE 위반 | - |
| 타입 검증 | input_type = NUMBER 또는 TEXT만 허용 | 그 외 값 → 예외 |
| NUMBER 검증 | min_value > max_value → 실패 | step_value <= 0 → 실패 |
| TEXT 검증 | max_length <= 0 → 실패 | TEXT에 min/max/step 설정 → 무시 |
| 교차 검증 | NUMBER에 max_length 설정 → 무시 | - |
| CRUD | 활성 필드만 sort_order 순 조회 | 비활성 필드 제외 |

### Phase 4: 예약 생성 시 메뉴 선택 + 입력값 저장

| 구분 | 테스트 시나리오 | 엣지 케이스 |
|------|----------------|------------|
| 메뉴 저장 | 선택한 메뉴가 reservation_menu_items에 저장 | 메뉴 0개 → 예외 (최소 1개) |
| 스냅샷 | 메뉴 이름/설명이 스냅샷으로 저장 | 원본 변경 후 조회 → 스냅샷 유지 |
| 중복 | 동일 메뉴 중복 선택 → UNIQUE 위반 | - |
| 권한 | 다른 shop 메뉴 선택 → 예외 | - |
| 활성 | 비활성 메뉴 선택 → 예외 | - |
| 입력값 | required 필드 누락 → 실패 | - |
| 입력값 | NUMBER 범위 초과 → 실패 | step 미준수 → 실패 |
| 입력값 | TEXT max_length 초과 → 실패 | - |
| 입력값 | 타입 불일치 (NUMBER에 text) → 예외 | - |
| 입력값 | 미정의 field_id에 값 → 예외 | - |
| Dual-write | formDataJson도 함께 저장 | reservation_menu_items 실패 → 롤백 |

### Phase 5: 예약 조회 시 메뉴/입력값 응답

| 구분 | 테스트 시나리오 | 엣지 케이스 |
|------|----------------|------------|
| 조회 | 예약 상세에 메뉴 + 입력값 포함 | reservation_menu_items 비면 formDataJson fallback |
| 중첩 | 메뉴별 입력값이 하위로 중첩 | 입력값 없는 메뉴 → 빈 배열 |

---

## 7. 관련 문서

- ERD v2 설계: `docs/issues/#97/reservation-erd-v2.md`
- Flyway V1: `src/main/resources/db/migration/V1__reservation_v2_phase1_add_tables_and_columns.sql`
- Flyway V3: `src/main/resources/db/migration/V3__reservation_v2_phase3_constraints_and_indexes.sql`
- Flyway V4 (리네이밍 + key 제거): `src/main/resources/db/migration/V4__rename_shop_services_to_shop_menus.sql`
- TDD plan (SSOT): `docs/issues/#99/04-tdd/plan.md`
