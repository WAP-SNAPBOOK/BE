# #97 TDD Plan — ERD v2 코드 전환

Created: 2026-02-05  
Issue: `#97`  
Status: 계획 수립

---

## Ground Rules

1. **Red → Green → Refactor**: 실패 테스트 → 최소 구현 → 리팩토링
2. **Tidy First**: 구조 변경과 동작 변경을 같은 커밋에 섞지 않음
3. **Defect Protocol**: API-level 실패 테스트 → 최소 재현 테스트 → 둘 다 통과
4. **한 커밋 = 한 논리 단위**: 테스트 통과 상태에서만 커밋
5. **프로젝트 강제 규칙**:
  - **public API만 테스트** (private 메서드 테스트 금지, public 테스트로 간접 검증)
  - **Reflection 금지**
  - **RED 단계 임시 구현 허용**: 컴파일만 되는 최소 임시 구현(`return null;`, `throw new UnsupportedOperationException("TODO");` 등)으로 “실행 실패(RED)”를 만든다.

---

## Scope

### Goals

- P0: 운영 설정 안정화 (`application.yml` 정리)
- P1: 예약 도메인을 ERD v2로 전환 (핵심)
  - `Reservation` 엔티티 v2 필드 매핑
  - `Staff` 엔티티 도입 + 기본 Staff 자동 생성
  - 확정 시 `ReservationTimeBlock` 생성 + UNIQUE 충돌 처리
  - 예약 생성 시 `start_at` dual-write
- P2: 이력 기록 도입 (`reservation_status_histories`)

### Non-goals

- P3 Slot 폐기 (Phase 4) — 별도 이슈로 분리
- 메뉴/입력값 저장 전환 (`reservation_services`, `reservation_menu_input_values`) — 후속 이슈
- 과거 예약 데이터 마이그레이션/호환 정책 — 별도 논의

---

## Test List

### Phase 0: 설정 안정화 (Tidy/Structural — 테스트 불필요)

- [x] `application.yml` git conflict 마커 제거  
  - **목적**: 앱 기동 실패 방지  
  - **작업**: conflict 마커 삭제, `ddl-auto: update` 확정  
  - **검증**: 앱 기동 성공

- [x] `baseline-on-migrate` 비활성화  
  - **목적**: 새 환경에서 마이그레이션 스킵 사고 방지  
  - **작업**: `baseline-on-migrate: false` 또는 해당 줄 제거  
  - **검증**: Flyway 설정 변경 확인

---

### Phase 1-A: Staff 엔티티 도입

> **정책**: Staff = "예약 담당 직원" (데이터 개념). 별도 로그인 계정 없음. 점주가 CRUD.

- [x] **1-A-1**: `Staff` 엔티티가 `staff` 테이블에 매핑된다  
  - **목적**: v2 FK 참조 대상(Staff) 제공  
  - **입력**: `Staff(shopId, name)` — userId는 사용하지 않음  
  - **출력**: DB에 저장 후 조회 성공  
  - **엣지케이스**: `shopId` 필수, `name` nullable

- [x] **1-A-2**: `StaffReader.findByShopId(shopId)`가 해당 샵의 Staff 목록을 반환한다  
  - **목적**: 샵별 직원 조회 (점주가 직원 목록 확인)  
  - **입력**: `shopId`  
  - **출력**: `List<Staff>`  
  - **엣지케이스**: 없으면 빈 리스트

- [x] **1-A-3**: `StaffWriter.save(staff)`가 Staff를 저장한다  
  - **목적**: Staff 생성/저장 (점주가 직원 등록)  
  - **입력**: `Staff` 엔티티  
  - **출력**: 저장된 `Staff` (id 할당됨)

---

### Phase 1-B: Shop 생성 시 기본 Staff 자동 생성

- [x] **1-B-1**: Shop 생성 시 기본 Staff 1명이 자동 생성된다  
  - **목적**: "점주 1명" 운영에서 `staff_id` FK 충족  
  - **입력**: `Shop` 생성 요청  
  - **출력**: Shop 생성 후 `Staff(shopId=shop.id, name=ownerName)` 존재  
  - **엣지케이스**: 이미 Staff가 있는 경우 중복 생성 안 함

- [x] **1-B-2**: `StaffReader.getDefaultStaffByShopId(shopId)`가 기본 Staff를 반환한다  
  - **목적**: 레거시/예외 케이스(기존 예약에 staffId가 없는 경우 등)에서 기본 Staff 조회  
  - **입력**: `shopId`  
  - **출력**: 해당 샵의 기본 Staff  
  - **엣지케이스**: 없으면 예외(`StaffNotFoundException`)

---

### Phase 1-C: Reservation 엔티티 v2 필드 매핑

- [x] **1-C-1**: `Reservation` 엔티티에 `startAt` 필드가 DB 컬럼 `start_at`에 매핑된다  
  - **목적**: v2 시간축 사용 준비  
  - **입력**: `Reservation.startAt = LocalDateTime.of(2026, 2, 5, 14, 0)`  
  - **출력**: DB 조회 시 `start_at` 컬럼에 값 존재  
  - **엣지케이스**: null 허용x (이미 backfill 완료)
 
- [x] **1-C-2**: `Reservation` 엔티티에 `staffId` 필드가 DB 컬럼 `staff_id`에 매핑된다  
  - **목적**: 담당 직원 저장  
  - **입력**: `Reservation.staffId = 1L`  
  - **출력**: DB 조회 시 `staff_id` 컬럼에 값 존재  
  - **엣지케이스**: null 허용 (레거시 호환)

- [x]**목적**: 확정 시 기간 저장  
  - **입력**: `Reservation.durationMinutes = 60`  
  - **출력**: DB 조회 시 `duration_minutes` 컬럼에 값 존재  
  - **엣지케이스**: null 허용 (CONFIRMED 전까지)

---

### Phase 1-D: 예약 생성 시 start_at + staffId 기록 (Dual-write)

- [x] **1-D-1**: 예약 생성 시 `startAt`이 `date + time`으로 계산되어 저장된다  
  - **목적**: dual-write로 점진 전환  
  - **입력**: `ReservationCreateRequest(date=2026-02-05, time=14:00)`  
  - **출력**: 생성된 예약의 `startAt = 2026-02-05T14:00`  
  - **엣지케이스**: date/time 파싱 실패 시 기존대로 예외

- [x] **1-D-2**: 예약 생성 시 고객이 선택한 `staffId`가 저장된다  
  - **목적**: \"담당 직원 선택\" 주체를 고객으로 고정 (확정 시점 선택 제거)  
  - **입력**: `ReservationCreateRequest(staffId=1, date=2026-02-05, time=14:00)`  
  - **출력**: 생성된 예약의 `staffId = 1`  
  - **엣지케이스**: \n+    - staffId 누락/존재하지 않는 staffId → 예외\n+    - staffId가 해당 shopId 소속이 아니면 → 예외

- [x] **1-D-3**: 예약 생성 시 `time`은 10분 단위만 허용된다  
  - **목적**: duration(10분 단위) 및 점유 블록(10분 단위)와 시간 정렬 일관성 유지  
  - **입력**: `time=14:15` 또는 `time=14:30`  
  - **출력**: `14:15`는 예외, `14:30`은 정상 생성  
  - **엣지케이스**: `14:00`, `23:30` 허용

---

### Phase 1-E: ReservationTimeBlock 엔티티 도입

- [x] **1-E-1**: `ReservationTimeBlockWriter.saveAll(blocks)`가 여러 블록을 저장한다  
  - **목적**: 확정 시 duration에 따른 다중 블록 생성  
  - **입력**: 10분 단위 블록 리스트  
  - **출력**: 모두 저장됨

- [x] **1-E-2**: 동일 `(staff_id, block_start_at)` 중복 저장 시 UNIQUE 제약 위반 예외가 발생한다  
  - **목적**: 겹침 방지 검증  
  - **입력**: 이미 존재하는 (staffId=1, blockStartAt=14:00) + 동일 값 저장 시도  
  - **출력**: `DataIntegrityViolationException` 또는 유사 예외  
  - **관측**: 트랜잭션 롤백 확인

---

### Phase 1-F: 확정 시 duration 수신 + 점유 블록 생성

> **정책**: 고객이 예약 생성 시 담당 Staff를 선택한다. 확정 시점에는 Staff를 다시 선택하지 않는다.

- [x] **1-F-1**: `ReservationConfirmRequest`에 `durationMinutes` 필드가 추가된다  
  - **목적**: 확정 시 기간 입력(10분 단위)  
  - **입력**: `ReservationConfirmRequest(message, durationMinutes=60)`  
  - **출력**: DTO에서 값 접근 가능  
  - **엣지케이스**: 
    - duration: 10분 단위 검증 (10, 20, 30, ...)

- [x] **1-F-1a**: 확정 요청의 `durationMinutes`는 10분 단위(10, 20, 30, ...)만 허용된다  
  - **목적**: 점유 블록(10분 단위) 정책과 입력값 정렬 유지  
  - **입력**: `ReservationConfirmRequest(durationMinutes=45)`  
  - **출력**: 검증 실패(violation) 또는 400 응답(@Valid)  
  - **엣지케이스**:
    - 30은 허용
    - 0/음수/미지정(null) 정책은 후속 테스트에서 결정

- [ ] **1-F-2**: 확정 시 `Reservation.durationMinutes`가 설정된다  
  - **목적**: 예약에 기간 저장  
  - **입력**: 확정 요청 (durationMinutes=60)  
  - **출력**: 예약 엔티티의 durationMinutes=60

- [ ] **1-F-3**: 확정 시 예약의 `staffId`가 null이면 기본 Staff로 보정된다(레거시 보호)  
  - **목적**: 기존 데이터/호환 경로에서 staffId가 비어있어도 확정이 가능하게 함  
  - **입력**: staffId=null인 예약 확정 요청  
  - **출력**: 예약의 staffId = 해당 샵의 기본 Staff.id  
  - **관측**: 보정이 일어난 경우 로그/메트릭 남김(추후 제거 가능)

- [x] **1-F-4**: 확정 시 `ReservationTimeBlock`이 10분 단위로 생성된다  
  - **목적**: 점유 블록으로 겹침 방지  
  - **입력**: 예약(startAt=14:00, durationMinutes=60, staffId=1) 확정  
  - **출력**: 6개 블록 생성 (staffId=1, 14:00/14:10/14:20/14:30/14:40/14:50)  
  - **엣지케이스**: duration=10 → 1개, duration=30 → 3개, duration=90 → 9개

- [ ] **1-F-5**: 확정 시 동일 Staff + 동일 시간대에 이미 점유가 있으면 예외가 발생한다  
  - **목적**: 중복 확정 방지 (Staff별)  
  - **입력**: staffId=1, 14:00에 이미 점유 존재 + staffId=1로 14:00~15:00 확정 시도  
  - **출력**: `TimeSlotAlreadyBookedException` (또는 유사)  
  - **관측**: 트랜잭션 롤백, 예약 상태 PENDING 유지  
  - **엣지케이스**: 다른 Staff(staffId=2)로는 동일 시간 확정 가능

- [ ] **1-F-6**: 확정 시 점주가 시간을 변경(reschedule)할 수 있다  
  - **목적**: 점주 변경 권한  
  - **입력**: 확정 요청 (startAt=15:00, durationMinutes=60) — 원래 14:00이었음  
  - **출력**: 예약의 startAt=15:00, 점유 블록도 15:00/15:10/…/15:50  
  - **엣지케이스**: 변경된 시간이 이미 점유 시 예외

---

### Phase 2: 상태 이력 기록

- [ ] **2-1**: `ReservationStatusHistory` 엔티티가 `reservation_status_histories` 테이블에 매핑된다  
  - **목적**: 상태 전이 기록  
  - **입력**: `ReservationStatusHistory(reservationId, fromStatus, toStatus, changedByUserId, reason)`  
  - **출력**: DB에 저장 후 조회 성공

- [ ] **2-2**: 예약 확정 시 상태 이력이 기록된다  
  - **목적**: 감사 로그  
  - **입력**: PENDING → CONFIRMED 전이  
  - **출력**: `reservation_status_histories`에 레코드 생성  
  - **엣지케이스**: reason은 nullable

- [ ] **2-3**: 예약 거절 시 상태 이력이 기록된다  
  - **목적**: 감사 로그  
  - **입력**: PENDING → REJECTED 전이  
  - **출력**: `reservation_status_histories`에 레코드 생성 (reason 포함)

- [ ] **2-4**: 예약 취소 시 상태 이력이 기록된다  
  - **목적**: 감사 로그  
  - **입력**: PENDING/CONFIRMED → CANCELED 전이  
  - **출력**: `reservation_status_histories`에 레코드 생성

---

## Notes

### Long-running 테스트 제외 기준
- 실제 DB 연결이 필요한 통합 테스트는 `@Tag("integration")` 붙임
- 기본 테스트 실행 시 제외 가능하도록 설정

### 모호한 요구사항/질문 목록

1. **duration 검증 범위**: 15분 단위 강제? 최소/최대 시간?
   - 확정 : 10분 단위 강제, 최소/최대 미정(정책 결정 필요)

2. **reschedule 시 레거시 date/time도 업데이트?**
   - 확정: 레거시 호환을 위해 date/time도 함께 업데이트

3. **UNIQUE 충돌 시 사용자 메시지 정책**
   - 확정: "해당 시간대는 이미 예약되어 있습니다. 다른 시간을 선택해주세요."

4. **기존 PENDING 예약의 staffId는 어떻게 채우나?**
   - 확정: 기존 예약은 staffId=null 허용, **확정 시 기본 Staff로 보정**

### 확정된 정책

1. **Staff = 데이터 개념** (로그인 계정 아님)
   - 별도 직원 계정(User) 없음
   - Staff.userId는 사용하지 않음 (항상 null)
   - 점주가 Staff를 CRUD

2. **관계**: 1 User(OWNER) → 1 Shop → N Staff
   - 직원이 여러 명이어도 점주 1명이 관리
   - Shop 생성 시 기본 Staff 1명 자동 생성

3. **Staff 선택 주체**
   - 고객이 예약 생성 시 `staffId`를 선택해 저장
   - 확정(점주) 단계에서는 staff를 다시 선택하지 않음

---

## 진행 상태

| Phase | 상태 | 비고 |
|-------|------|------|
| Phase 0 | 대기 | 설정 파일 정리 (Tidy) |
| Phase 1-A | 대기 | Staff 엔티티 |
| Phase 1-B | 대기 | Shop-Staff 연동 |
| Phase 1-C | 대기 | Reservation v2 필드 |
| Phase 1-D | 대기 | 예약 생성 dual-write |
| Phase 1-E | 대기 | ReservationTimeBlock |
| Phase 1-F | 대기 | 확정 로직 |
| Phase 2 | 대기 | 상태 이력 |

---

## 관련 문서

- 문제 스캔: `docs/issues/#97/00-intake/0002-problem-scan-post-flyway.md`
- 구현 계획: `archive/docs/feature/jiseob/#97-reservation-erd-v2-follow-up/issue/0003-implementation-plan.md`
- 원인/대안 분석: `docs/issues/#97/02-analysis/0001-root-cause-and-options.md`
