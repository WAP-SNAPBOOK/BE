# 예약 ERD v2 (최종안)

## 논리적 흐름 요약

- 예약 생성(`PENDING`) 단계에서는 **점유를 잡지 않는다(A0)** → 같은 시간대 접수는 중복될 수 있다.
- 점주가 확정(`CONFIRMED`)할 때 **duration(10분 단위)**을 결정하고, 그 순간에만 **점유 블록(10분 단위)**을 생성하여 **직원 스케줄 겹침을 DB에서 강제**한다.
- 시작 시간(`start_at`)과 점유 블록(`block_start_at`)은 **모두 10분 단위**다.
- 예약은 **메뉴를 여러 개 선택 가능**, 메뉴별로 **추가 입력(NUMBER/TEXT)** 이 붙을 수 있다.
- 메뉴는 **태그로 필터링** 가능해야 한다.

---

## 1) 도메인 정책(확정)

### 예약 흐름/상태
- `PENDING`: 고객(또는 점주 대리)이 접수 생성
- `CONFIRMED`: 직원이 수락(확정) + `duration_minutes` 결정 + 점유 생성
- `REJECTED`: 직원이 거절
- `CANCELLED`: 고객이 취소(고객은 변경 불가, 취소만 가능)

### 권한
- 고객: 변경 불가, 취소만 가능
- 점주: 시간/메뉴/입력값 등 예약 상세 전부 변경 가능
  - 시간/직원 변경 시: **기존 점유 즉시 해제 → 신규 점유 생성**

### 시간 규칙
- `start_at`: 10분 단위만 허용
- `duration_minutes`: 10분 단위만 허용

### 점유 규칙(A0)
- `PENDING`: 점유 없음
- `CONFIRMED`: 점유 생성
- `CANCELED/REJECTED`: 점유 삭제(즉시 해제)

### Staff 선택 정책(확정)
- **고객이 예약 생성 시 `staff_id`를 선택**해 저장한다.
- 확정 단계에서는 staff를 다시 선택하지 않는다(이미 저장된 `staff_id` 사용).

### 레거시 호환(현 단계)
- 예약 생성 요청은 점진적으로 **명시적 DTO(필드)**로 전환한다.
- 기존 화면/응답이 `formDataJson`에 의존하는 동안은, 서버가 명시 필드로부터 **레거시 키 포맷의 `formDataJson`을 조립하여 저장**한다(클라이언트가 임의 JSON을 보내지 않음).

---

## 2) ERD(테이블 목록)

예약 도메인에서 다루는 주요 테이블:

- 조직/사용자(기존): `shops`, `users`, `staff`
- 예약 핵심: `reservations`
- 점유(겹침 방지): `reservation_time_blocks`
- 예약 구성(메뉴 다중 선택): `shop_services`, `reservation_services`
- 메뉴 태그 필터: `tags`, `shop_service_tags`
- 메뉴별 추가 입력: `shop_service_input_fields`, `reservation_menu_input_values`
- 사진: `reservation_photos`
- 이력/감사: `reservation_status_histories`, `reservation_change_histories`

---

## 3) 테이블 상세(컬럼/제약/인덱스)

### 3.1 `shops`
- `id` (PK)
- (기존 스키마 기준)

### 3.2 `users`
- `id` (PK)
- (고객/점주 계정)

### 3.3 `staff`
- `id` (PK)
- `shop_id` (FK → `shops.id`, NOT NULL)
- `user_id` (NULL)  ← 직원 계정 없음(사용하지 않음)
- `name` …
- **index**: `idx_staff_shop (shop_id)`

---

### 3.4 `reservations` (핵심)

> 기존 코드에서는 `date`/`time` 분리 구조이므로, 물리 스키마 확정 시 “`start_at`로 통합 vs 기존 유지”를 선택해야 함. v2에서는 조회/정합성 관점에서 `start_at` 통합을 권장한다.

- `id` (PK)
- `shop_id` (FK → `shops.id`, NOT NULL)
- `staff_id` (FK → `staff.id`, NULL)  ← 레거시 보호로 NULL 허용(신규 생성에서는 필수)
- `customer_id` (FK → `users.id`, NOT NULL)

- `start_at` (DATETIME, NOT NULL)  ← 30분 단위
- `start_at` (DATETIME, NOT NULL)  ← 10분 단위
- `duration_minutes` (INT, NULL)   ← PENDING에서는 NULL 가능, CONFIRMED에서 결정(10분 단위)

- `status` (ENUM/VARCHAR, NOT NULL) = `PENDING | CONFIRMED | REJECTED | CANCELED`
- `rejection_reason` (TEXT/VARCHAR, NULL)
- `confirmation_message` (TEXT/VARCHAR, NULL)

- `customer_note` (TEXT/LONGTEXT, NULL)  ← 고객이 남기는 추가 요청사항(비정형은 단일 메모로 시작)

- `created_at` (DATETIME, NOT NULL)
- `updated_at` (DATETIME, NOT NULL)
- `deleted_at` (DATETIME, NULL)  ← soft delete

**제약(권장)**:
- `CHECK(MOD(MINUTE(start_at), 10) = 0)`
- `CHECK(duration_minutes IS NULL OR duration_minutes > 0)`
- `CHECK(duration_minutes IS NULL OR MOD(duration_minutes, 10) = 0)`

**인덱스(요구사항 기반)**:
- 점주/직원 목록(날짜/상태, 최신/시간순):
  - `idx_res_shop_status_start_at (shop_id, status, start_at)`
  - `idx_res_staff_start_at (staff_id, start_at)`
  - `idx_res_shop_start_at (shop_id, start_at)`
- 고객 목록(최신순, shop 필터):
  - `idx_res_customer_created_at (customer_id, created_at)`
  - (필요 시) `idx_res_customer_shop_created_at (customer_id, shop_id, created_at)`

---

### 3.5 `reservation_time_blocks` (겹침 방지 “점유” 테이블)

- `id` (PK)
- `reservation_id` (FK → `reservations.id`, NOT NULL)
- `staff_id` (FK → `staff.id`, NOT NULL)  ← 제약/조회 단순화를 위한 중복 저장(denormalization)
- `block_start_at` (DATETIME, NOT NULL)   ← 10분 단위
- `created_at` (DATETIME, NOT NULL)

**핵심 제약(필수)**:
- `UNIQUE(staff_id, block_start_at)`  ← 직원 스케줄 겹침을 DB에서 원천 차단

**인덱스(권장)**:
- `idx_blocks_reservation (reservation_id)`

**운영 규칙**
- PENDING: 생성 없음
- CONFIRMED: `duration_minutes` 기반으로 블록 생성
  - 블록 수 = `ceil(duration_minutes / 10)`
  - 예: 60분이면 `start_at`, `start_at+10m`, …, `start_at+50m` 6개 생성
- CANCELED/REJECTED: 해당 예약의 블록 전부 삭제

---

### 3.6 메뉴(매장별) : `shop_services`

- `id` (PK)
- `shop_id` (FK → `shops.id`, NOT NULL)
- `name` (NOT NULL)
- `description` (NULL 허용)
- `is_active` (NOT NULL)
- `sort_order` (NOT NULL)
- `created_at`, `updated_at`, (선택) `deleted_at`

**제약/인덱스(권장)**:
- (정책에 따라) `UNIQUE(shop_id, name)`
- `idx_services_shop_active_sort (shop_id, is_active, sort_order)`

---

### 3.7 메뉴 태그: `tags`, `shop_service_tags`

#### `tags`
- `id` (PK)
- `name` (NOT NULL)  ← 예: 손관리/발관리
- `created_at`, `updated_at`

**제약(권장)**:
- `UNIQUE(name)`

#### `shop_service_tags`
- `id` (PK)
- `shop_service_id` (FK → `shop_services.id`, NOT NULL)
- `tag_id` (FK → `tags.id`, NOT NULL)

**제약/인덱스(권장)**:
- `UNIQUE(shop_service_id, tag_id)`
- `idx_service_tags_tag (tag_id, shop_service_id)`  ← 태그로 메뉴 필터링

---

### 3.8 메뉴별 추가 입력 정의: `shop_service_input_fields`

- `id` (PK)
- `shop_service_id` (FK → `shop_services.id`, NOT NULL)
- `key` (NOT NULL)          ← 예: `quantity`
- `label` (NOT NULL)        ← 예: “손연장 갯수”
- `input_type` (NOT NULL)   ← `NUMBER` | `TEXT`
- `required` (NOT NULL)
- (NUMBER) `min_value`, `max_value`, `step_value`
- (TEXT) `max_length`
- `placeholder` (NULL)
- `sort_order` (NOT NULL)
- `is_active` (NOT NULL)
- `created_at`, `updated_at`

**제약(권장)**:
- `UNIQUE(shop_service_id, key)`

---

### 3.9 예약-메뉴(다중 선택): `reservation_services`

- `id` (PK)
- `reservation_id` (FK → `reservations.id`, NOT NULL)
- `shop_service_id` (FK → `shop_services.id`, NOT NULL)

- `service_name_snapshot` (NOT NULL)
- `service_description_snapshot` (NULL 허용)
- (선택) `service_duration_snapshot_minutes` (NULL 허용)
- `sort_order` (NOT NULL)
- `created_at` (DATETIME, NOT NULL)

**제약/인덱스(권장)**:
- (정책에 따라) `UNIQUE(reservation_id, shop_service_id)`  ← 동일 메뉴 중복 선택 금지/허용 결정
- `idx_res_services_reservation (reservation_id, sort_order)`

---

### 3.10 예약-메뉴별 추가 입력값: `reservation_menu_input_values`

- `id` (PK)
- `reservation_service_id` (FK → `reservation_services.id`, NOT NULL)
- `shop_service_input_field_id` (FK → `shop_service_input_fields.id`, NOT NULL)

- `field_key_snapshot` (NOT NULL)
- `field_label_snapshot` (NOT NULL)
- `input_type_snapshot` (NOT NULL)  ← NUMBER/TEXT

- `value_number` (NULL 가능)
- `value_text` (NULL 가능)
- `created_at` (DATETIME, NOT NULL)

**제약/인덱스(권장)**:
- `UNIQUE(reservation_service_id, shop_service_input_field_id)`
- `idx_menu_input_values_res_service (reservation_service_id)`

---

### 3.11 사진: `reservation_photos` (기존)

- `reservation_id` (FK → `reservations.id`, NOT NULL)
- `photo_url`
- (PK 구성은 현재 방식 유지 또는 별도 `id` 추가 선택)

---

### 3.12 상태 이력: `reservation_status_histories`

- `id` (PK)
- `reservation_id` (FK → `reservations.id`, NOT NULL)
- `from_status` (NULL 가능: 최초 생성)
- `to_status` (NOT NULL)
- `changed_by_user_id` (FK → `users.id`, NOT NULL)
- `reason` (NULL)
- `created_at` (NOT NULL)

**인덱스(권장)**:
- `idx_status_hist_reservation (reservation_id, created_at)`

---

### 3.13 변경 이력: `reservation_change_histories`

- `id` (PK)
- `reservation_id` (FK → `reservations.id`, NOT NULL)
- `changed_by_user_id` (FK → `users.id`, NOT NULL)
- `change_type` (예: `TIME_CHANGE`, `STAFF_CHANGE`, `MENU_CHANGE`, `INPUT_CHANGE` …)
- `before_json` (JSON/TEXT)
- `after_json` (JSON/TEXT)
- `reason` (NULL)
- `created_at` (NOT NULL)

**인덱스(권장)**:
- `idx_change_hist_reservation (reservation_id, created_at)`

---

## 4) 트랜잭션 흐름(점유 생성/해제 포함)

### 4.1 예약 생성(PENDING)
- `reservations` INSERT (status=PENDING, duration_minutes=NULL)
- (후속) `reservation_services` INSERT (메뉴 N개 + 스냅샷)
- (후속) `reservation_menu_input_values` INSERT (메뉴별 추가 입력이 있으면)
- (후속) `reservation_photos` INSERT (사진이 있으면)
- 점유 생성 없음
- `reservation_status_histories` 기록(from=NULL → PENDING)

### 4.2 점주 확정(CONFIRMED, duration 확정)
- 트랜잭션 시작
- `reservations` UPDATE (status=CONFIRMED, duration_minutes=…)
- `reservation_time_blocks` INSERT (10분 단위 블록 N개)
  - UNIQUE 충돌 발생 시: 전체 롤백 → “해당 시간대에 이미 확정 예약이 존재” (조정 후 재확정/거절)
- `reservation_status_histories` 기록(PENDING → CONFIRMED)
- 트랜잭션 커밋

### 4.3 거절(REJECTED) / 취소(CANCELED)
- `reservations` UPDATE (status=REJECTED/CANCELED)
- `reservation_time_blocks` DELETE (reservation_id 기준, 즉시 해제)
- `reservation_status_histories` 기록

### 4.4 점주 변경(시간/직원/메뉴/입력값)
- 시간/직원 변경이면:
  - 트랜잭션 시작
  - 기존 `reservation_time_blocks` DELETE
  - `reservations` UPDATE (start_at/staff_id 등)
  - status=CONFIRMED인 경우에만 `reservation_time_blocks` 재생성
  - `reservation_change_histories` 기록
  - 트랜잭션 커밋
- 메뉴/입력값 변경이면:
  - `reservation_services` / `reservation_menu_input_values`를 갱신(이력 기록 권장)
  - 점유에는 영향 없음(시간 변경이 아니면)

---

## 5) 남은 결정(선택 사항)

1) `reservation_services`에서 동일 메뉴 중복 선택 허용 여부
   - 금지면 `UNIQUE(reservation_id, shop_service_id)` 적용
2) `start_at`를 DB CHECK로 강제할지(버전/정책에 따라 앱 검증으로 대체)
3) 메뉴/입력필드의 soft delete 여부(운영/감사 요구에 따라)
4) `staff_id` / `start_at` 컬럼의 NOT NULL 전환 시점(레거시 데이터 정리 후)

