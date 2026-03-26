# #97 문제 스캔 (Post-Flyway) — 코드 전환 단계

Created: 2026-02-05  
Issue: `#97`  
Status: Flyway V1~V3 적용 완료, 코드 전환 대기

---

# Context(현재 상황)

- **Flyway V1~V3**: DB 스키마(v2)가 이미 적용된 상태
  - `V1`: 테이블/컬럼 추가 (`staff`, `reservation_time_blocks`, `shop_services`, `reservation_services` 등)
  - `V2`: 백필 (`start_at`, `staff_id`)
  - `V3`: 제약/인덱스 (`UNIQUE(staff_id, block_start_at)` 등)
- **애플리케이션 코드**: 레거시 구조(`date/time`, `formDataJson`, Slot)에 의존 중
- **핵심 문제**: **DB 스키마와 애플리케이션 코드의 불일치**가 발생한 상태

---

# 증상/징후

## 재현 절차

### 1) 코드에서 v2 필드가 매핑되지 않음

**파일**: `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`

- DB에는 `staff_id`, `start_at`, `duration_minutes`, `customer_note`, `updated_at`, `deleted_at` 컬럼이 존재
- **엔티티에는 해당 필드가 없음** → JPA가 이 컬럼들을 사용하지 않음

현재 엔티티 상태:
```java
// 레거시 필드만 존재
private LocalDate date;
private LocalTime time;
private String formDataJson;

// v2 필드(startAt, staffId, durationMinutes 등)가 없음
```

### 2) v2 관련 엔티티 클래스가 존재하지 않음

다음 엔티티들이 **코드에 없음**:
- `Staff` (DB 테이블: `staff`)
- `ReservationTimeBlock` (DB 테이블: `reservation_time_blocks`)
- `ShopService` (DB 테이블: `shop_services`)
- `ReservationService` (DB 테이블: `reservation_services`)
- `ReservationMenuInputValue` (DB 테이블: `reservation_menu_input_values`)
- `ReservationStatusHistory` (DB 테이블: `reservation_status_histories`)
- `ReservationChangeHistory` (DB 테이블: `reservation_change_histories`)

### 3) 확정 시 점유 블록 생성 로직이 없음

**파일**: `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`

`confirmReservation()` 메소드 현재 상태:
- `reservation.confirm(message)` 호출 후 **점유 블록(`reservation_time_blocks`)을 생성하지 않음**
- `durationMinutes`를 받지 않음 → 확정 시 기간을 설정할 수 없음

**파일**: `src/main/java/com/example/easybooking/reservation/dto/ReservationConfirmRequest.java`

- `message` 필드만 존재
- `durationMinutes`, `startAt` 필드 없음

### 4) 설정 파일에 문제가 있음

**파일**: `src/main/resources/application.yml`

문제 1: **Git merge conflict 마커가 남아있음** (라인 74~80)
```yaml
<<<<<<< Updated upstream
      ddl-auto: update
=======
      ddl-auto: create
>>>>>>> Stashed changes
```

문제 2: **`baseline-on-migrate: true`가 활성화 상태**
- 이 설정이 유지되면 새 DB/새 환경에서 초기 마이그레이션이 스킵되는 사고 위험

### 5) 가용시간 조회가 PENDING을 점유로 취급할 수 있음

**파일**: `ReservationService.getShopAvailability()` (라인 403~416)

현재 로직:
```java
List<LocalTime> bookedTimes = reservations.stream()
    .filter(r -> r.getStatus() != Reservation.Status.CANCELED &&
            r.getStatus() != Reservation.Status.REJECTED)
    .map(Reservation::getTime)
    .collect(Collectors.toList());
```

- `PENDING` 상태도 `bookedTimes`에 포함됨
- 계획 문서(`0003-implementation-plan.md`)에서는 "PENDING은 점유가 아님"으로 정책이 변경되었다가 다시 "PENDING도 예약 불가로 취급하자"로 확정됨
- 정책 확정 여부와 코드 일치 여부 재확인 필요

---

# 영향 범위

## 사용자
- 겹침 방지 미동작: 확정 시 점유 블록이 생성되지 않아 **동일 시간대 중복 확정 가능**
- v2 기반 조회 불가: `start_at` 기준 조회/캘린더 기능 사용 불가

## 도메인
- **Reservation**: v2 필드(`startAt`, `staffId`, `durationMinutes`)가 사용되지 않음
- **Staff**: 점주 1명 운영이라도 `staff_id` FK가 필요하지만 Staff 엔티티/기본 데이터가 없음
- **ReservationTimeBlock**: CONFIRMED 시점 점유 블록 생성 로직 없음
- **이력**: 상태/변경 이력(`reservation_status_histories`, `reservation_change_histories`) 기록 로직 없음

## 성능
- 현재는 영향 없음
- 점유 블록 도입 시 UNIQUE 충돌 처리/락 대기 관측 필요

## 비용/운영
- 스키마-코드 불일치 상태가 길어질수록 전환 비용 증가
- `baseline-on-migrate` 오남용 시 새 환경에서 초기화 사고 위험
- git conflict 마커가 배포되면 앱 기동 실패

## 보안
- 직접적 보안 이슈 없음
- 운영 사고(정합성/삭제 실패)로 인한 간접 리스크

---

# 리팩토링 후보 목록(우선순위 + 근거)

## P0 — 즉시 수정 필요 (장애/사고 방지)

### 1) `application.yml` git conflict 마커 제거
- **근거**: 배포 시 앱 기동 실패
- **작업**: conflict 마커(`<<<<<<<`, `=======`, `>>>>>>>`) 제거 및 `ddl-auto` 값 확정

### 2) `baseline-on-migrate` 비활성화 또는 환경별 분리
- **근거**: 운영 안정화 문서에서 "다음 배포부터 제거/false" 권장
- **작업**: `baseline-on-migrate: false`로 변경 또는 해당 줄 제거

## P1 — 코드 전환 핵심 (ERD v2 기능 동작)

### 3) `Reservation` 엔티티에 v2 필드 매핑
- **근거**: DB 컬럼이 존재하지만 코드에서 사용하지 않음
- **작업**: `startAt`, `staffId`, `durationMinutes`, `customerNote`, `updatedAt`, `deletedAt` 필드 추가

### 4) `Staff` 엔티티 생성 + 매장 생성 시 기본 Staff 자동 생성
- **근거**: `reservation_time_blocks.staff_id` FK가 `staff` 테이블을 참조
- **작업**: 
  - `Staff` 엔티티/리포지토리 생성
  - 매장(Shop) 생성 시 "기본 직원" 1명 자동 생성 로직 추가

### 5) 확정 시 점유 블록 생성 + UNIQUE 충돌 처리
- **근거**: v2의 핵심 가치(겹침 방지 단일 소스)
- **작업**:
  - `ReservationTimeBlock` 엔티티/리포지토리 생성
  - `ReservationConfirmRequest`에 `durationMinutes` 필드 추가
  - `confirmReservation()` 에서 점유 블록 생성 로직 추가
  - UNIQUE 충돌 시 트랜잭션 롤백 + 예외 처리

### 6) 예약 생성 시 `start_at` 기록 (dual-write)
- **근거**: 점진 전환을 위해 레거시(`date/time`)와 신규(`start_at`)를 함께 저장
- **작업**: 
  - `Reservation.createReservation()`에서 `startAt = LocalDateTime.of(date, time)` 저장
  - `staffId`는 기본 Staff 조회하여 채움

## P2 — 메뉴/입력값/이력 (v2 확장 기능)

### 7) 메뉴/입력값 저장 전환
- **근거**: `reservation_services`, `reservation_menu_input_values` 테이블 활용
- **작업**:
  - `ShopService`, `ReservationServiceItem`, `ReservationMenuInputValue` 엔티티 생성
  - 예약 생성 시 메뉴 선택/입력값 저장 로직 추가

### 8) 상태/변경 이력 기록
- **근거**: `reservation_status_histories`, `reservation_change_histories` 테이블 활용
- **작업**:
  - `ReservationStatusHistory`, `ReservationChangeHistory` 엔티티 생성
  - 상태 전이/변경 시 이력 기록 로직 추가

## P3 — Slot 폐기 준비 (Phase 4)

### 9) Slot 폐기 조건 정의 및 로드맵
- **근거**: Slot 기반과 점유블록 기반 혼재 시 운영 이슈
- **작업**:
  - 가용시간 API를 스케줄+점유 기반으로 전환 완료
  - 운영 안정화 후 `V4` 마이그레이션(Slot drop)

---

# 지금 당장 안 하면 생기는 비용

1. **즉시(P0)**:
   - `application.yml` conflict 마커 → 배포 시 앱 기동 실패
   - `baseline-on-migrate` → 새 환경에서 마이그레이션 스킵 사고

2. **단기(P1)**:
   - 스키마-코드 불일치 → 전환 시점에서 변경 폭이 커져 회귀/장애 위험 증가
   - 점유 블록 미생성 → 동일 시간대 중복 확정 가능(겹침 방지 실패)

3. **중기(P2~P3)**:
   - Slot/레거시/신규 혼재 → 도메인 단일 책임 불명확, 디버깅 비용 급증
   - 메뉴/이력 미사용 → v2의 핵심 가치(스냅샷/감사 로그) 미실현

---

# 다음 단계 제안

1. **P0 즉시 처리**: `application.yml` conflict 제거 + `baseline-on-migrate` 비활성화
2. **TDD plan.md 작성**: P1 작업들을 테스트 케이스 단위로 분할
3. **단계별 구현**: go 프로토콜에 따라 테스트 → 구현 → 커밋 사이클 진행

---

# 관련 문서

- 기존 문제 스캔: `docs/issues/#97/00-intake/0001-problem-scan.md`
- GitHub 이슈 초안: `docs/issues/#97/01-issue/0001-github-issue-draft.md`
- 원인/대안 분석: `docs/issues/#97/02-analysis/0001-root-cause-and-options.md`
- 구현 계획: `archive/docs/feature/jiseob/#97-reservation-erd-v2-follow-up/issue/0003-implementation-plan.md`
- 운영 안정화 체크리스트: `study/2026-01-23/post-flyway-migration-next-steps.md`
- 영향도 리뷰: `study/2026-01-23/reservation-erd-v2-impact-review.md`
