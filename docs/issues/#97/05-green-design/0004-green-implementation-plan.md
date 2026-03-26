---
issue: "#97"
title: "Green Design 0004 — Phase 1-B-1 Shop 생성 시 기본 Staff 자동 생성"
date: "2026-02-05"
status: "draft"
---

## 입력(관련 문서)

- TDD SSOT: `docs/issues/#97/04-tdd/plan.md` (`1-B-1`)
- RED 테스트/로그:
  - `src/test/java/com/example/easybooking/shop/ShopDefaultStaffCreationTest.java`
  - `study/2026-02-05/0009-issue-97-go-red-1b1-default-staff-auto-create.md`

---

## 목표: 이번 Green에서 통과시킬 테스트(1개)

- **`1-B-1`**: Shop 생성 시 기본 Staff 1명이 자동 생성된다
  - 기대: `Staff(shopId=shop.id, name="기본")` 1개가 존재

---

## 설계(최소)

### 책임/경계

- **Shop 생성 유스케이스 경계**: `ShopService.createShop()`
  - Shop 생성 직후(동일 트랜잭션) 기본 Staff를 1명 보장한다.

- **Staff 조회/저장**: 기존 `StaffReader`/`StaffWriter`를 사용
  - 중복 방지(최소): 해당 shopId에 Staff가 없다면 1명 생성

### 데이터 흐름

1. `shopwriter.create(ownerId, request)`로 Shop 저장 및 shopId 확보
2. `staffReader.findByShopId(shopId)` 조회
3. 비어있으면 `staffWriter.save(Staff.create(shopId, "기본"))`
4. (기존 로직) `formService.createDefaultForm(shopId)`

---

## 구현 계획(최소)

- `ShopService`에 `StaffReader`, `StaffWriter` 의존성 주입
- `createShop()` 내에서 Shop 생성 후 기본 Staff 보장 로직 추가
- 테스트는 수정하지 않고 그대로 재실행해 GREEN 확인

---

## 이번 Green에서 하지 않을 것

- “기본 Staff”의 식별 규칙(예: 플래그 컬럼/유니크 제약) 도입
- `StaffNotFoundException` 등 예외 체계(다음 `1-B-2`에서)
- 동시성(중복 생성 레이스) 방어를 위한 DB 제약/락 설계

---

## 완료 정의(DoD)

- `ShopDefaultStaffCreationTest.createShop_createsDefaultStaff()`가 테스트 수정 없이 통과한다.

