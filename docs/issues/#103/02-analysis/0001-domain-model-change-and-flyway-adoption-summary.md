# #999 도메인 모델 변경 과정 및 Flyway 적용 방식 정리

Created: 2026-03-01  
Issue: `#999`

---

## 1. 목적

현재 코드베이스 기준으로, 예약/메뉴/가용시간 도메인 모델이 어떤 순서로 변경되었는지와 Flyway 적용 방식(운영 원칙 포함)을 한 번에 확인할 수 있도록 정리한다.

---

## 2. 조사 범위

- 마이그레이션 SQL
  - `src/main/resources/db/migration/V1__reservation_v2_phase1_add_tables_and_columns.sql`
  - `src/main/resources/db/migration/V2__reservation_v2_phase2_backfill_start_at_and_staff.sql`
  - `src/main/resources/db/migration/V3__reservation_v2_phase3_constraints_and_indexes.sql`
  - `src/main/resources/db/migration/V4__rename_shop_services_to_shop_menus.sql`
  - `src/main/resources/db/migration/V5__shop_availability_settings.sql`
- 설정 파일
  - `src/main/resources/application.yml`
  - `src/main/resources/application-loadtest.yml`
- 이슈 문서
  - `docs/issues/#97/*`, `docs/issues/#99/*`, `docs/issues/#101/*`
- 운영 메모
  - `study/2026-01-23/post-flyway-migration-next-steps.md`

---

## 3. 변경 연표 (도메인 모델 + 마이그레이션)

### 3.1 2026-01-23: 예약 ERD v2 도입 (V1~V3)

#### V1 (Phase 1: Add-only)
- `reservations`에 v2 컬럼 추가: `staff_id`, `start_at`, `duration_minutes`, `customer_note`, `updated_at`, `deleted_at`
- 신규 테이블 생성:
  - `staff`
  - `reservation_time_blocks`
  - `shop_services`, `tags`, `shop_service_tags`, `shop_service_input_fields`
  - `reservation_services`, `reservation_menu_input_values`
  - `reservation_status_histories`, `reservation_change_histories`

#### V2 (Phase 2: Backfill)
- `reservations.date + time -> start_at` 백필
- 기존 예약 기준 `(shop_id, owner_user_id)`로 기본 `staff` 생성
- `reservations.staff_id` 백필

#### V3 (Phase 3: Constraints/Indexes)
- 핵심 유니크 제약 추가: `uq_rtb_staff_block_start_at` (`staff_id`, `block_start_at`)
- 메뉴/입력값 중복 방지 유니크 추가
- 조회 인덱스 및 FK 강화

문서상 의미:
- `#97` 문서군에서 이 시점을 "DB 스키마는 적용 완료, 코드 전환 대기" 상태로 정의함.

### 3.2 2026-02-10: 메뉴 도메인 명칭/구조 정리 (V4)

- `shop_services` 계열을 `shop_menus` 계열로 리네이밍
  - `shop_services -> shop_menus`
  - `shop_service_tags -> shop_menu_tags`
  - `shop_service_input_fields -> shop_menu_input_fields`
  - `reservation_services -> reservation_menu_items`
- 컬럼 리네임 및 제약/인덱스/외래키를 새 명칭으로 재구성
- `key`, `field_key_snapshot` 제거

문서상 의미:
- `#99`에서 메뉴 다중선택/추가입력/태그 필터링을 정형 모델로 전환하기 위한 기반 마이그레이션으로 사용.

### 3.3 2026-02-12: 가용시간 도메인 추가 (V5)

- 신규 테이블 생성:
  - `shop_settings`
  - `shop_operating_times`
  - `staff_operating_times`
  - `shop_holidays`
  - `public_holidays`

문서상 의미:
- `#101` TDD 계획에서 V5를 데이터베이스 레이어의 시작점으로 두고, 예약 가능 시간 도메인을 완성.

---

## 4. 코드 반영 상태 (현재)

V1~V5에서 의도한 도메인 다수가 실제 코드에 매핑되어 있음.

- 예약 v2 핵심
  - `Reservation` (`startAt`, `staffId`, `durationMinutes` 포함)
  - `ReservationTimeBlock` (`reservation_time_blocks`, `uq_rtb_staff_block_start_at` 매핑)
  - `Staff` (`staff` 매핑)
- 메뉴 v4 반영
  - `ShopMenu`, `ShopMenuInputField`, `ShopMenuTag`
  - `ReservationMenuItem`, `ReservationMenuInputValue`
- 가용시간 v5 반영
  - `ShopSettings`, `ShopOperatingTime`, `StaffOperatingTime`, `ShopHoliday`, `PublicHoliday`

즉, "V1~V3 적용 후 코드 전환 대기"였던 #97 초기 상태에서, 이후 #99/#101을 거치며 실제 코드 모델도 상당 부분 전환된 상태다.

---

## 5. Flyway 적용 방식 정리

### 5.1 적용 전략

문서 기준 운영 전략은 다음과 같다.
- 앱 기동 시 Flyway 자동 migrate (A안)
- `flyway_schema_history`에서 baseline(0) + 버전 성공 여부로 적용 완료 판단

근거: `study/2026-01-23/post-flyway-migration-next-steps.md`

### 5.2 운영 권장 원칙

문서에서 반복 강조한 원칙:
- `baseline-on-migrate: true`는 초기 1회성 용도
- 이후에는 제거 또는 `false` 권장
- 최종적으로 `ddl-auto`를 `validate`로 수렴해, 스키마 변경 책임을 Flyway로 단일화

---

## 6. 현재 설정과 문서 권장안의 갭

현재 설정 파일 확인 결과:
- `application.yml`:
  - `spring.flyway.baseline-on-migrate: true`
  - `spring.jpa.hibernate.ddl-auto: update` (local/dev)
- `application-loadtest.yml`:
  - `spring.jpa.hibernate.ddl-auto: update`

해석:
- 문서 권장안(전환 완료 후 baseline 비활성화 + ddl-auto validate) 대비, 아직 "전환 초기/완료 전" 설정이 유지되고 있다.

---

## 7. 결론

1. 도메인 모델 변경은 `V1~V3(예약 v2 기반 구축) -> V4(메뉴 도메인 리네이밍/정형화) -> V5(가용시간 도메인)` 순으로 진행되었다.
2. 현재 코드에는 해당 도메인 엔티티와 서비스가 대부분 반영되어, 초기에 문서가 지적한 "스키마-코드 불일치"는 상당 부분 해소된 상태다.
3. 다만 Flyway 운영 원칙 관점에서는 `baseline-on-migrate`/`ddl-auto` 설정이 문서의 최종 권장 상태까지는 아직 수렴되지 않았다.

---

## 8. 참고 문서

- `docs/issues/#97/00-intake/0001-problem-scan.md`
- `docs/issues/#97/00-intake/0002-problem-scan-post-flyway.md`
- `docs/issues/#97/01-issue/0001-github-issue-draft.md`
- `docs/issues/#99/02-design/design-plan.md`
- `docs/issues/#99/07-pr/0001-pr-draft.md`
- `docs/issues/#101/04-tdd/plan.md`
- `docs/issues/#101/07-pr/0001-pr-draft.md`
- `study/2026-01-23/post-flyway-migration-next-steps.md`
