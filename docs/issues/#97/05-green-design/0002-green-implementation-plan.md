---
issue: "#97"
title: "Green Design 0002 — Phase 1-A-2 StaffReader.findByShopId(shopId)"
date: "2026-02-05"
status: "draft"
---

## 입력(관련 문서)

- TDD SSOT: `docs/issues/#97/04-tdd/plan.md`
- Green(이전): `docs/issues/#97/05-green-design/0001-green-implementation-plan.md`

---

## 목표: 이번 Green에서 통과시킬 테스트(1개)

- **`1-A-2`**: `StaffReader.findByShopId(shopId)`가 해당 샵의 Staff 목록을 반환한다
  - 엣지케이스: 없으면 **빈 리스트**

---

## 설계(최소)

### 책임/경계(클래스/모듈/함수 수준)

- **`StaffRepository`**
  - 위치(예상): `com.example.easybooking.staff.repository.StaffRepository`
  - 역할: `staff` 테이블에 대한 Spring Data JPA 접근
  - 필요한 쿼리: `List<Staff> findByShopId(Long shopId)`

- **`StaffReader`**
  - 위치(예상): `com.example.easybooking.staff.StaffReader` (타 도메인 Reader와 동일 레벨 패턴)
  - 역할: “조회(use-case)” 관점에서 Staff 조회를 캡슐화
  - 메소드:
    - `List<Staff> findByShopId(Long shopId)` → `staffRepository.findByShopId(shopId)` 그대로 반환

### 데이터 흐름/의존성

- 테스트에서 `Staff` 2개를 shopId=1로 저장
- `StaffReader.findByShopId(1)` 호출
- 반환 리스트에 2개가 포함됨을 검증
- 없는 shopId(예: 999) 호출 시 빈 리스트 검증

---

## 구현 계획(최소) + 각 단계에서 확인할 테스트

### Step 1) 테스트(RED): `StaffReader.findByShopId` 동작 명세 고정

- 형태(권장)
  - `@DataJpaTest`로 영속성 레이어를 띄우고,
  - `StaffRepository`를 주입 받아 `new StaffReader(staffRepository)`로 Reader를 직접 생성(스프링 컴포넌트 스캔 의존 제거)

- **예시 코드(참고용 — 아직 적용하지 않음)**

```diff
@DataJpaTest
class StaffReaderTest {
  @Autowired StaffRepository staffRepository;

  @Test
  void findByShopId_returnsStaffList() { ... }

  @Test
  void findByShopId_returnsEmptyList_whenNone() { ... }
}
```

- **확인할 테스트**
  - `1-A-2`에 해당하는 신규 테스트 1개(가능하면 2개로 쪼개도 되지만, plan.md는 1개 항목이므로 “빈 리스트”는 같은 테스트에서 같이 확인해도 됨)

### Step 2) 최소 구현(GREEN): Repository + Reader 추가

- `StaffRepository` 추가 + `findByShopId` 메소드 선언
- `StaffReader` 추가 + `findByShopId` 메소드 구현
- **확인할 테스트**
  - Step 1에서 만든 테스트 통과

---

## 최소 구현 전략(이번 Green에서 하지 않을 것 포함)

- **이번 Green에서 할 것**
  - “샵별 Staff 목록 조회”가 가능함을 Reader 레벨에서 고정

- **이번 Green에서 하지 않을 것(명시)**
  - 기본 Staff 개념/조회(`1-B-*`) 및 예외(`StaffNotFoundException`) 도입
  - Staff 생성/저장 Writer(`1-A-3`)
  - Shop 생성 흐름에 Staff 자동 생성 연결

---

## Tidy First(구조/동작 분리) 계획

- 구조 변경은 최소화한다.
- 새 파일 추가로만 해결:
  - `staff/repository/StaffRepository.java` (structural 성격)
  - `staff/StaffReader.java` (behavioral 성격이지만, 실질은 thin wrapper)
  - 테스트 파일 1개

---

## 리스크/디버깅 포인트

- `@DataJpaTest` 슬라이스에서 Repository 스캔이 누락되면 빈 컨텍스트 문제가 날 수 있음
  - 해결: 패키지 위치를 기존 패턴(`com.example.easybooking.<domain>.repository`)과 동일하게 유지
- 정렬 요구사항은 현재 없음(리스트 순서는 테스트에서 강하게 고정하지 않기)

---

## 완료 정의(DoD)

- `StaffReader.findByShopId(shopId)`가
  - 해당 샵의 Staff를 반환하고
  - 없으면 빈 리스트를 반환함을 테스트로 증명한다.

