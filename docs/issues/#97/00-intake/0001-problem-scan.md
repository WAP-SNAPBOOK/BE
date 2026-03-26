# Context(현재 상황)

- 이슈: `#97` (예약 ERD v2 후속 조치)
- 선행 작업: `#95-reservation-erd-v2-flyway`
  - Flyway `V1~V3`로 예약 ERD v2 스키마(추가/백필/제약·인덱스)가 **이미 적용된 상태**
  - 현재 앱 코드는 “ERD v2 스키마를 실제로 쓰는 단계(코드 전환)”로 넘어가기 전 단계
- 핵심 전제(근거):
  - `V1__reservation_v2_phase1_add_tables_and_columns.sql`: v2 테이블/컬럼 추가(파괴적 변경 없음)
  - `V2__reservation_v2_phase2_backfill_start_at_and_staff.sql`: `start_at`, `staff_id` 백필
  - `V3__reservation_v2_phase3_constraints_and_indexes.sql`: 점유/중복 방지 제약 및 조회 인덱스 적용
  - 기존 문서: `archive/docs/feature/jiseob/#95-reservation-erd-v2-flyway/issue/0002-follow-up-plan.md`,
    `archive/docs/feature/jiseob/#97-reservation-erd-v2-follow-up/issue/0002-design-plan.md`,
    `study/2026-01-23/post-flyway-migration-next-steps.md`

---

# 증상/징후

## 재현 절차

### 1) 스키마는 존재하나(ADD/backfill/제약) “기능 전환”이 되지 않은 상태

- DB 관점에서:
  - `reservations`에 v2 컬럼(`staff_id`, `start_at`, `duration_minutes` 등)이 존재하고,
  - v2 테이블(`reservation_time_blocks`, `reservation_services` 등)이 존재하며,
  - 제약(예: `reservation_time_blocks.staff_id + block_start_at` UNIQUE)이 적용되어 있을 수 있음
- 앱/도메인 관점에서:
  - 예약 생성/조회/가용시간/확정 로직이 여전히 레거시 축(date/time, Slot, formDataJson)에 의존할 여지가 큼
  - 확정(confirmed) 시점의 “겹침 방지 단일 소스”가 v2(점유 블록)로 아직 수렴되지 않았을 수 있음

### 2) 운영 리스크가 남아있음(설정/동시성/삭제)

- `baseline-on-migrate`를 상시 유지하면 “새 DB/새 스키마”에서 초기 마이그레이션이 스킵되는 사고 위험이 있음
- 다중 인스턴스 기동 시 Flyway migrate 경쟁(락 대기/기동 지연) 관리가 필요함
- v2 자식 테이블이 실제로 쓰이기 시작하면 FK로 인해 기존 “물리 삭제/정리” 로직이 실패할 수 있음

## 로그/지표/스크린샷(가능한 경우 링크/발췌)

- (운영 확인 기준) `study/2026-01-23/post-flyway-migration-next-steps.md`의 체크리스트:
  - `flyway_schema_history`에서 baseline(0), `V1~V3`가 모두 `success=1`인지 확인
  - 애플리케이션 로그에서 `V1 → V2 → V3` 실행/성공 여부 확인

---

# 영향 범위

- 사용자:
  - 가용시간/겹침 방지 혼재 시 “보이는데 예약 안됨/예약되는데 안 보임” 형태의 신뢰도 하락
  - 확정 시점 충돌 처리 정책 미비 시 예약 확정 실패/이상 상태 발생
- 도메인:
  - 예약 시간축이 `start_at`로 수렴되지 않으면(dual-write/전환 실패) v2 기반 도메인(직원, 점유, 메뉴/입력, 이력)이 제대로 동작하지 못함
  - 삭제 정책 미정(FK) 시 운영 정리/취소/삭제가 장애 지점이 될 수 있음
- 성능:
  - 확정(점유 블록 생성) 트랜잭션에서 락/인덱스/UNIQUE 충돌이 병목이 될 수 있음
- 비용/운영:
  - 배포 시 기동 지연/락 대기/5xx 증가 가능성
  - 설정 실수(`baseline-on-migrate`)로 인한 초기화/누락 사고 비용 급증
- 보안:
  - 직접적인 신규 보안 리스크보다는 운영 사고(데이터 정합성/삭제 실패)에 따른 간접 리스크가 큼

---

# 리팩토링 후보 목록(우선순위 + 근거)

> 여기의 “리팩토링 후보”는 #97의 “후속 조치(코드/운영 전환)”를 기능 단위로 잘게 쪼갠 작업 후보 목록이다.

1) P0 — 운영에서 “적용 완료” 확정 및 설정 안정화
   - 근거: 스키마 적용이 확정되지 않으면 이후 코드 전환이 환경별로 어긋남
   - 포함:
     - `flyway_schema_history` success 확인
     - 다음 배포부터 `baseline-on-migrate` 제거/false
     - (전환 완료 후) `ddl-auto`를 `validate`로 수렴하는 체크리스트 준비

2) P0 — 확정(CONFIRMED) 시 점유 블록 생성 + 충돌 정책 확정(UNIQUE 기반)
   - 근거: `reservation_time_blocks` UNIQUE 제약은 “겹침 방지”의 핵심이며, 정책 없으면 바로 장애로 이어짐

3) P0 — 삭제 정책 결정(FK 대응)
   - 근거: v2 자식 테이블이 쌓이면 기존 삭제가 실패할 수 있음
   - 선택지:
     - 옵션 A: FK에 `ON DELETE CASCADE`(추가 마이그레이션)
     - 옵션 B: 앱에서 자식 → 부모 삭제 순서 보장

4) P1 — Dual-write로 예약 생성/수정 시 v2 구조 기록 시작(`start_at`, `staff_id`, `duration_minutes`)
   - 근거: 전면 스위치보다 리스크 분산(문서 상 권장안: Dual-write + 점진 switch)

5) P1 — 조회/캘린더/가용시간을 v2 기반으로 점진 전환(읽기 switch)
   - 근거: 혼재 기간을 최소화하고 “단일 소스”를 기능 단위로 수렴

6) P2 — 메뉴/입력값/이력 저장 전환(`reservation_services`, `reservation_menu_input_values`, histories)
   - 근거: v2의 핵심 가치를 제공(스냅샷/이력/확장)

7) P3 — Slot 폐기(Phase 4) 준비 조건 정의 및 실행 계획 수립
   - 근거: 파괴적 변경은 안정화 이후로 미루고, 선행 조건(가용시간 v2 전환/운영 안정화)을 충족해야 함

---

# 지금 당장 안 하면 생기는 비용

- “스키마는 있는데 코드가 안 쓰는 상태”가 길어질수록:
  - 전환 시점에서 변경 폭이 커져 회귀/장애 위험이 증가
  - 운영 설정(`baseline-on-migrate`, 다중 기동) 사고 가능성이 누적
  - Slot/레거시/신규 구조 혼재로 도메인 단일 책임이 불명확해져 디버깅 비용이 급증

