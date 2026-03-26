---
issue: "#97"
title: "Green Design 0003 — Phase 1-A-3 StaffWriter.save(staff)"
date: "2026-02-05"
status: "draft"
---

## 입력(관련 문서)

- TDD SSOT: `docs/issues/#97/04-tdd/plan.md` (`1-A-3`)
- RED 결과 로그: `study/2026-02-05/0007-issue-97-go-red-1a3-staff-writer-save.md`

---

## 목표: 이번 Green에서 통과시킬 테스트(1개)

- **`1-A-3`**: `StaffWriter.save(staff)`가 Staff를 저장한다 (저장 후 id가 할당됨)

---

## 설계(최소)

### 책임/경계

- **`StaffWriter`**: `staff` 도메인의 “저장(use-case)” 캡슐화
  - 메소드: `Staff save(Staff staff)`
  - 동작: `staffRepository.save(staff)`를 그대로 위임(thin wrapper)

### 의존성

- `StaffWriter` → `StaffRepository`

---

## 구현 계획(최소)

- `src/main/java/com/example/easybooking/staff/StaffWriter.java` 추가
  - 생성자: `StaffRepository` 1개 받는 형태(테스트에서 `new StaffWriter(staffRepository)`로 사용)
  - `save()` 구현: repository 위임

## 이번 Green에서 하지 않을 것

- 유효성 검증(shopId/name 규칙), 중복 정책, 예외 타입 정의
- 트랜잭션 경계/서비스 계층 연결(추후 단계에서)

---

## 완료 정의(DoD)

- `StaffWriterTest.save_persistsStaffAndAssignsId`가 테스트 수정 없이 통과한다.

