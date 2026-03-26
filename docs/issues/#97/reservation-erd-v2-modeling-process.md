# 예약 ERD 설계 논리 흐름 정리 (개념적 → 논리적 → 물리적 모델링)

이 문서는 `reservation-erd-v2.md`(최종 ERD v2)를 만들 때의 사고 과정을 **3단계 모델링 관점**으로 정리한 것이다.

---

## 1) 개념적 모델링(Conceptual Modeling)

### 1.1 “무엇이 존재하는가”(엔티티 후보를 명사로 뽑기)
- **예약(Reservation)**: 고객이 특정 직원의 시간을 요청/확정하는 업무 단위(수명주기 존재)
- **직원(Staff)**: 예약이 배정되는 주체(스케줄이 겹치면 안 됨)
- **매장(Shop)**: 직원/메뉴의 소속 범위
- **고객(Customer/User)**: 예약 주체(회원만)
- **메뉴(시술 메뉴)**: 예약 시 고객이 선택하는 항목(매장마다 다름, 여러 개 선택 가능)
- **메뉴 태그(Tag)**: 메뉴 필터링 단위(손관리/발관리 등)
- **메뉴별 추가 입력필드(Input Field)**: 특정 메뉴 선택 시 추가로 받는 NUMBER/TEXT 입력(필수/선택)
- **예약의 사진(ReservationPhoto)**: 예약에 첨부되는 이미지
- **상태 이력(Status History)**: 예약 상태 변화 기록
- **변경 이력(Change History)**: 점주가 예약 상세를 수정할 수 있으므로 변경의 before/after 기록
- **시간 점유(Time Block)**: “확정된 예약이 점유하는 직원 시간”을 표현하는 개념

### 1.2 “무엇이 누구에 속하는가”(관계/카디널리티를 말로 확정)
- 한 매장(Shop)에는 여러 직원(Staff)이 있다. (1:N)
- 한 예약(Reservation)은 정확히 한 직원(Staff)에 배정된다. (N:1)
- 한 예약은 메뉴를 여러 개 선택할 수 있다. (Reservation : Menu = N:M)
- 메뉴는 태그로 분류될 수 있다. (Menu : Tag = N:M)
- 메뉴는 메뉴별 추가 입력필드를 가질 수 있다. (Menu : InputField = 1:N)
- 예약이 선택한 “각 메뉴”에는 입력값이 0..N개 붙을 수 있다. (ReservationSelectedMenu : InputValue = 1:N)
- 예약은 사진을 0..N개 첨부할 수 있다. (1:N)
- 예약은 상태가 변하고, 그 상태 변화는 이력으로 남는다. (1:N)
- 예약 상세(시간/직원/메뉴/입력값)가 바뀌면 변경 이력으로 남는다. (1:N)

### 1.3 “절대 깨지면 안 되는 규칙”을 문장으로 확정(무결성 규칙)
- **고객은 변경 불가, 취소만 가능** / **점주는 상세 변경 가능**
- `PENDING` 단계에서는 **접수는 겹칠 수 있음**
- `CONFIRMED` 순간에는 **직원 스케줄이 겹치면 안 됨**
- 시작 시간은 **10분 단위**, duration은 **10분 단위**
- 메뉴는 매장별로 다르고, 예약에서 **여러 개 선택 가능**
- 특정 메뉴를 선택했을 때만 추가 입력이 존재할 수 있음(예: 손연장 → 갯수)

> 개념적 모델링 산출물은 “명사(엔티티) + 관계 + 핵심 규칙”이며, 이 단계에서는 아직 FK/인덱스/데이터 타입을 확정하지 않는다.

---

## 2) 논리적 모델링(Logical Modeling)

개념을 “테이블/키/관계”로 내리는 단계.

### 2.1 N:M을 조인 테이블로 분해
- **예약 ↔ 메뉴(N:M)** → `reservation_services` 조인 테이블로 분해
  - 이유: 예약에서 메뉴 다중 선택 요구
  - 추가로 `sort_order`, `snapshot`을 같이 들기 위해 조인 엔티티가 자연스러움
- **메뉴 ↔ 태그(N:M)** → `shop_service_tags` 조인 테이블로 분해

### 2.2 “메뉴별 추가 입력”을 어디에 붙일지 결정
- 입력값은 예약 전체(`reservation_id`)가 아니라 **예약 안에서 선택된 특정 메뉴(`reservation_service_id`)**에 붙여야 함
  - 그래서 `reservation_menu_input_values`는 `reservation_service_id`를 FK로 참조
  - 이유: 예약이 메뉴를 여러 개 선택하므로 “어느 메뉴에 대한 입력인지”가 식별돼야 함

### 2.3 “겹침 방지”를 논리적으로 모델링(제약을 걸 수 있는 형태로 바꾸기)
- 범위 겹침(10:00~11:00 vs 10:30~11:30)은 단순 UNIQUE로 강제하기 어렵다(MySQL 특성).
- 그래서 “직원 스케줄 겹침” 규칙을 **블록 점유 레코드의 유니크**로 치환:
  - `reservation_time_blocks`에 `(staff_id, block_start_at)` 유니크
  - `block_start_at`는 10분 단위
- 또한 정책상 `PENDING`은 점유를 만들지 않으므로, **점유는 CONFIRMED에서만 생성**하도록 논리 흐름을 고정

### 2.4 이력 테이블의 논리적 필요성 확정
- 상태 이력: `reservation_status_histories` (from/to, changed_by, reason, created_at)
- 변경 이력: `reservation_change_histories` (change_type, before/after, reason, created_at)
  - 점주가 “예약 상세 전부 변경 가능”이기 때문에 논리적으로 필요

### 2.5 논리 모델 산출물(테이블 목록)
- 핵심: `reservations`, `reservation_time_blocks`
- 메뉴/입력: `shop_services`, `reservation_services`, `shop_service_input_fields`, `reservation_menu_input_values`
- 태그: `tags`, `shop_service_tags`
- 부가: `reservation_photos`, `reservation_status_histories`, `reservation_change_histories`
- 조직: `shops`, `staff`, `users`

> 논리 모델링 산출물은 “테이블/PK/FK/카디널리티/유니크 규칙(무결성)”이며, 아직 구체 타입/인덱스/성능 튜닝은 최종 확정하지 않는다.

---

## 3) 물리적 모델링(Physical Modeling)

논리 모델을 “실제 MySQL 테이블”로 내리는 단계(타입/인덱스/제약/운영 정책).

### 3.1 컬럼 타입/표현 방식 확정
- 시간:
  - `reservations.start_at`: `DATETIME` (10분 단위)
  - `reservation_time_blocks.block_start_at`: `DATETIME` (10분 단위)
- duration:
  - `reservations.duration_minutes`: `INT`, `MOD(duration_minutes, 10)=0`(권장)
- 비정형:
  - 동적 폼은 제거 확정 → 최소한의 텍스트 메모로 `reservations.customer_note`만 유지

### 3.2 DB 제약으로 강제할 것(핵심)
- **겹침 방지(확정 시점)**:
  - `reservation_time_blocks`에 `UNIQUE(staff_id, block_start_at)`
- 단위 제약(권장):
  - `CHECK(MOD(MINUTE(start_at), 10) = 0)`
  - `CHECK(duration_minutes IS NULL OR MOD(duration_minutes, 10) = 0)`
  - (MySQL 버전/운영 정책에 따라 CHECK가 약하면 앱 검증으로 보강)

### 3.3 인덱스(조회 패턴 기반)
- 점주/직원 예약 목록(날짜/상태 + 정렬):
  - `(shop_id, status, start_at)`
  - `(staff_id, start_at)`
- 고객 예약 목록(최신순):
  - `(customer_id, created_at)` (+ 필요 시 `(customer_id, shop_id, created_at)`)
- 태그로 메뉴 필터링:
  - `shop_service_tags(tag_id, shop_service_id)`

### 3.4 운영 정책(트랜잭션/동시성)
- `PENDING` 생성: 점유 없음
- `CONFIRMED` 확정 트랜잭션:
  - `reservations` 업데이트 + `reservation_time_blocks` 다건 insert를 **한 트랜잭션**
  - UNIQUE 충돌 시 롤백 → 시간 조정/재확정 또는 거절 처리
- 점주가 시간/직원 변경:
  - 기존 블록 삭제 → 예약 갱신 → (CONFIRMED라면) 블록 재생성

### 3.5 물리 모델 산출물
- 실제 구현 가능한 스키마/제약/인덱스/트랜잭션 규칙이 포함된 최종 문서:
  - `study/2026-01-22/erd/reservation-erd-v2.md`
  - `study/2026-01-22/erd/reservation-erd-v2-diagram.md`

