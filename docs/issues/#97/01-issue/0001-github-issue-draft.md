---
issue: "#97"
title: "feat: 예약 ERD v2 코드 전환 (post Flyway V1~V3)"
date: "2026-02-05"
status: "draft"
source:
  - "https://github.com/WAP-SNAPBOOK/BE/issues/97"
ssot:
  - "docs/issues/#97/04-tdd/plan.md"
---

## 배경/문제 정의

Flyway `V1~V3`로 예약 ERD v2 스키마는 이미 적용되었지만, 애플리케이션 코드는 여전히 레거시 예약 모델(`date/time`, `formDataJson`, Slot 등)을 중심으로 동작한다. 이 상태가 길어질수록 **스키마-코드 불일치**, **운영 설정 리스크(Flyway/JPA)**, 그리고 **겹침 방지 단일 소스 미수렴**으로 인한 장애/운영 비용이 커진다.

이번 이슈는 `docs/issues/#97/04-tdd/plan.md`를 SSOT로 하여, “스키마만 깔린 상태”에서 **코드 레벨로 실제 전환**을 완료하는 것을 목표로 한다.

---

## 목표(Goals)

- **운영 설정 안정화**: `application.yml`에서 Flyway/JPA 설정을 안전한 상태로 정리한다.
- **예약 시간축 전환 기반 마련**: 예약 생성 시 `start_at`을 dual-write로 채워 v2 축 데이터를 쌓는다.
- **Staff 도입(계정 없음)**: 한 점주(계정) → 한 매장 → 여러 Staff(데이터) 구조를 코드로 반영한다.
- **겹침 방지 단일 소스**: 확정(CONFIRMED) 시점에 `reservation_time_blocks`를 생성하여 중복 확정을 방지한다.
- **상태 이력 기록**: `reservation_status_histories`에 상태 전이를 기록한다.

---

## 비목표(Non-goals)

- Slot 제거(Phase 4), 레거시 컬럼/테이블 drop 등 **파괴적 DB 변경**
- 메뉴/입력값 전환(`reservation_services`, `reservation_menu_input_values`) — 후속 이슈로 분리
- 과거 예약 데이터의 완전 마이그레이션/표시 정책 확정

---

## 요구사항

### 기능

- **Staff 정책**
  - Staff는 “예약 담당 직원” **데이터 개념**이며, **별도 직원 계정(User)은 없다**.
  - 점주(OWNER)가 매장 내 Staff를 관리한다.
  - 매장 생성 시 **기본 Staff 1명**이 자동 생성된다.

- **예약 생성(Create)**
  - 고객이 예약 생성 시 **`staffId`를 선택**해 저장한다.
  - 예약 생성 시 `date/time`은 레거시 호환을 위해 유지하되, `start_at = date + time`을 함께 저장한다(dual-write).
  - 예약 생성 시 `time`은 **30분 단위만 허용**한다. (예: 14:00, 14:30)

- **예약 확정(Confirm)**
  - 확정 요청에서 `durationMinutes`를 입력받고, **30분 단위만 허용**한다. (예: 30, 60, 90…)
  - 확정 시점에는 staff를 다시 선택하지 않는다(이미 예약에 저장된 `staffId` 사용).
  - 레거시 보호: 기존 데이터 등으로 `Reservation.staffId`가 null인 경우, 확정 시 **기본 Staff로 보정**한다.
  - 확정 시 `reservation_time_blocks`를 **30분 단위**로 생성한다.
  - 동일 `(staff_id, block_start_at)` UNIQUE 충돌 시 트랜잭션 롤백 및 사용자 친화적 에러 응답(정책에 맞는 에러코드)을 반환한다.

### 비기능(성능/보안/운영)

- Flyway
  - `flyway_schema_history`로 `V1~V3` 적용 완료를 확인 가능해야 한다.
  - `baseline-on-migrate`는 초기 1회 목적 이후 **비활성화/제거**한다(새 DB 사고 방지).
  - 다중 인스턴스 동시 기동 시 migrate 경쟁 리스크를 낮출 배포 순서를 정리한다.
- JPA
  - 전환 완료 후 `ddl-auto`는 `validate`로 수렴 가능한 구조를 만든다.
- 관측
  - 확정 시 UNIQUE 충돌(겹침) 빈도, 롤백 여부를 추적 가능해야 한다(로그/에러코드).

---

## 수용 기준(AC)

### AC-0. 설정 안정화

- [ ] `src/main/resources/application.yml`의 **git conflict 마커가 제거**되어 정상 파싱/기동된다.
- [ ] `baseline-on-migrate`가 **비활성화/제거**되어 새 환경에서 마이그레이션 스킵 사고를 방지한다.

### AC-1. Staff 도입(계정 없음) + 기본 Staff

- [ ] `Staff` 엔티티가 `staff` 테이블에 매핑되고 저장/조회가 가능하다.
- [ ] 매장 생성 시 기본 Staff 1명이 생성된다.

### AC-2. 예약 생성: 고객 staff 선택 + start_at dual-write + 시간 정렬

- [ ] 고객이 선택한 `staffId`가 예약에 저장된다.
- [ ] 예약 생성 시 `start_at = date + time`이 저장된다(레거시 호환을 위해 `date/time`도 유지).
- [ ] 예약 생성 시 `time`은 30분 단위만 허용된다.

### AC-3. 예약 확정: duration(30분 단위) + 점유 블록(30분 단위) + 겹침 방지

- [ ] 확정 요청으로 `durationMinutes`를 받고 30분 단위로 검증한다.
- [ ] 확정 시 `duration_minutes`가 저장된다.
- [ ] 확정 시 `reservation_time_blocks`가 30분 단위로 생성된다.
- [ ] 동일 staff + 동일 시간대 점유가 있으면 UNIQUE 충돌로 확정이 실패하며, 트랜잭션이 롤백된다.
- [ ] 레거시 보호: 예약의 `staffId`가 null이면 확정 시 기본 Staff로 보정된다.

### AC-4. 상태 이력

- [ ] 확정/거절/취소 시 `reservation_status_histories`에 상태 전이가 기록된다.

---

## 범위/의존성

- 범위
  - 예약 생성/확정 흐름을 v2 축(`start_at`, `staff_id`, `duration_minutes`, `reservation_time_blocks`)으로 수렴
  - Staff(계정 없음) 및 기본 Staff 생성
  - 상태 이력 기록
  - 운영 설정 안정화(Flyway/JPA)
- 의존성
  - Flyway `V1~V3`가 적용되어 있어야 함(`staff`, `reservation_time_blocks`, 제약/인덱스 포함)
  - “고객 staff 선택”을 위한 프론트/요청 DTO 합의(필드 추가)

---

## 리스크/운영 메모

- **설정 리스크**: `application.yml` 충돌 마커/잘못된 `baseline-on-migrate` 설정은 즉시 장애로 이어질 수 있다.
- **혼재 리스크**: `date/time`과 `start_at` 동시 보유 구간에서 정합성(특히 reschedule) 정책이 필요하다.
- **동시성 리스크**: UNIQUE 충돌이 “정상적인 사용자 플로우”가 될 수 있으므로 에러코드/메시지/재시도 UX가 중요하다.

---

