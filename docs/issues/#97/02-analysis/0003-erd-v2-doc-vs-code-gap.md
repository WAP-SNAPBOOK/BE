# #97 — ERD v2 문서 vs 현재 코드/스키마 불일치 분석(갭 분석)

## 논리적 흐름 요약

- 현재 코드베이스에는 ERD v2의 핵심 구성요소( `staff`, `reservation_time_blocks`, `duration_minutes`, `start_at`, 유니크 제약/예외 매핑)가 **이미 Flyway/코드/테스트로 부분 반영**되어 있다.
- 하지만 **문서들(최종 ERD / 모델링 과정 / TDD plan / 예외 매핑 가이드) 간에 “시간 단위(10/15/30분)”가 서로 충돌**하고,
- 그 결과 **코드/스키마/문서가 같은 정책을 가리키지 않는 상태**다.
- 또한 `ReservationTimeBlock`의 **테이블 매핑명이 Flyway와 불일치**하여, `ddl-auto: update` 환경에서는 “테이블이 2개로 갈라지는” 데이터 사고 위험이 있다.

---

## 기준 문서/증거(SSOT 후보)

- 최종 ERD: `docs/issues/#97/reservation-erd-v2.md`
  - `start_at`, `duration_minutes`, `reservation_time_blocks` 및 유니크 제약 등 최종안 명시
- 모델링 과정: `docs/issues/#97/reservation-erd-v2-modeling-process.md`
  - 시간 단위를 15분으로 서술(최종 ERD와 상충)
- TDD SSOT: `docs/issues/#97/04-tdd/plan.md`
  - “plan.md가 개발 순서/정책의 SSOT” 규칙이 적용되는 상태
  - 다만 plan 내부에도 10분/30분 예시가 혼재(정리 필요)
- Flyway: `src/main/resources/db/migration/V1__reservation_v2_phase1_add_tables_and_columns.sql` 등
  - 실제 운영 DB에 반영될 물리 스키마의 근거
- 코드(핵심):
  - `src/main/java/com/example/easybooking/reservation/TimeBlockGenerator.java`
  - `src/main/java/com/example/easybooking/reservation/domain/ReservationTimeBlock.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`

---

## 갭 목록(문서 ↔ 스키마 ↔ 코드)

### 1) [P0/즉시] `ReservationTimeBlock` 테이블명 불일치(데이터 사고 위험)

- **Flyway 생성 테이블**: `reservation_time_blocks` (복수형)
- **JPA 엔티티 매핑**: `reservation_time_block` (단수형)
  - 파일: `src/main/java/com/example/easybooking/reservation/domain/ReservationTimeBlock.java`

**영향**
- 현재 `spring.jpa.hibernate.ddl-auto: update`이므로,
  - Flyway로 `reservation_time_blocks`가 존재해도,
  - Hibernate가 `reservation_time_block`를 새로 만들거나/업데이트하려고 시도할 수 있다.
- 결과적으로 “점유 데이터가 두 테이블에 분산”될 수 있어, **겹침 방지/조회/삭제 로직이 깨지는 치명적 리스크**가 있다.

**권장 조치(문서/코드 모두)**
- 엔티티 `@Table(name=...)`을 Flyway와 동일하게 정렬.
- 관련 테스트/문서의 테이블명 표기 또한 하나로 통일.

---

### 2) [P0/정책결정] 시간 단위(10분 vs 30분 vs 15분) 문서/코드 충돌

| 항목 | 최종 ERD(`reservation-erd-v2.md`) | 모델링 과정 | plan.md | 현재 코드/테스트 |
|---|---|---|---|---|
| `start_at` 단위 | 10분 | 10분 | 10분 | 10분 검증(`validateTimeIsOn10MinuteBoundary`) |
| `duration_minutes` 단위 | 10분 | 10분 | 10분 | 10분 검증(`@MultipleOf(base=10)`) |
| 점유 블록 단위 | 10분 | 10분 | 10분 | 10분 생성(`TimeBlockGenerator.unitMinutes=10`) |

**영향**
- 정책이 섞여 있으면 “겹침 방지 해상도/블록 수/검증 규칙/UX”가 모두 흔들린다.
- 테스트는 10분 정책을 전제로 작성되어 있어(예: 60분 → 6블록), ERD(30분)와 일치하지 않는다.

**권장 조치**
- 시간 단위는 **10분으로 확정**하고,
  - ERD/모델링 과정/plan/예외 매핑 문서/DTO 검증/생성기/테스트를 **10분 정책으로 정렬**한다.

---

### 3) [P1] `reservations` v2 컬럼 일부는 스키마에 있으나 엔티티에 미매핑

- Flyway가 추가한 컬럼(일부): `customer_note`, `updated_at`, `deleted_at`
- `Reservation` 엔티티(`src/main/java/.../Reservation.java`)에는 현재 미존재

**영향**
- 당장 “컬럼은 있지만 코드가 쓰지 않는” 상태라 기능/요구가 생기면 다시 작업이 필요.
- 특히 `deleted_at`(soft delete)를 ERD에 넣을지/운영에 쓸지 결정이 필요.

**권장 조치**
- 이번 이슈 범위(P1)에서 사용할 컬럼만 우선 매핑하고,
- soft delete/updatedAt 운영 정책은 후속(또는 ADR)로 명확히 한다.

---

### 4) [P1] `reservation_services`의 “동일 메뉴 중복 선택” 정책은 스키마에서 이미 ‘금지’로 확정됨

- Flyway V3에서 `UNIQUE(reservation_id, shop_service_id)`를 추가함
- 최종 ERD 문서에는 “선택 사항”으로 남아 있음

**권장 조치**
- 문서를 “현재 스키마 기준(금지)”으로 업데이트하거나,
- 정말로 중복 선택 허용이 필요하다면 V3 제약을 되돌리는 별도 마이그레이션/결정이 필요.

---

## 반영 전략(결정)

### A안) 10분 단위로 표준화(확정)
- **정책**: 시작 시간 10분 단위, duration 10분 배수, 점유 블록 10분 단위
- **효과**: 코드/테스트/문서가 같은 정책을 가리키도록 빠르게 정합성 회복

---

## 코드 수정 예시(실제 적용 전 ‘예시’)

> 아래는 “정책을 10분으로 정렬(A안)”할 때의 예시다. (동의 이후에만 실제 코드 변경)

- `TimeBlockGenerator`: `unitMinutes=10`, `count = durationMinutes / 10`
- `ReservationConfirmRequest.durationMinutes`: `@MultipleOf(base=10)` 또는 `@AssertTrue`로 10분 배수 검증
- `ReservationService.validateTimeIsOn...`: 10분 경계 체크로 변경
- `ReservationTimeBlock.@Table(name=...)`: `reservation_time_blocks`로 정렬
- 관련 테스트: 60분 → 6블록(14:00, 14:10, …, 14:50) 등으로 기대값 정렬

---

## 기대 결과(변경 후)

- 문서/plan/코드/테스트가 **동일 시간 단위 정책**을 공유한다.
- `ReservationTimeBlock` 저장 테이블이 단일화되어, 운영 DB에서 **겹침 방지(유니크)와 롤백 동작이 신뢰 가능**해진다.

