---
issue: "#97"
title: "Green Design 0001 — Phase 1-A-1 Staff 엔티티 JPA 매핑"
date: "2026-02-05"
status: "draft"
---

## 입력(관련 문서)

- TDD SSOT: `docs/issues/#97/04-tdd/plan.md`
- 문제 스캔: `docs/issues/#97/00-intake/0002-problem-scan-post-flyway.md`
- 분석/대안: `docs/issues/#97/02-analysis/0001-root-cause-and-options.md`

---

## 목표: 이번 Green에서 통과시킬 테스트(1개)

- **`1-A-1`**: `Staff` 엔티티가 `staff` 테이블에 매핑된다
  - 테스트 형태(예상): `@DataJpaTest`에서 `Staff.create(shopId, name)`을 `persist → flush/clear → find` 했을 때 값이 유지된다

---

## 설계(최소)

### 책임/경계(클래스/모듈 수준)

- **도메인 엔티티**: `com.example.easybooking.staff.domain.Staff`
  - 책임: Staff 데이터의 “영속 모델(JPA 매핑)”만 제공
  - 정책: Staff는 “예약 담당 직원(데이터 개념)”이며 **로그인 계정(User)과 분리**. 이번 단계에서 `userId`는 도입하지 않는다.

- **테스트**: `com.example.easybooking.staff.domain.StaffJpaMappingTest`
  - 책임: JPA가 `Staff`를 저장/조회할 수 있음을 검증(최소 영속성 smoke test)

### 데이터 흐름/의존성

- 테스트 → `EntityManager.persist(staff)` → JPA/Hibernate가 테이블 매핑 생성(or 기존 스키마 매핑) → `em.find(Staff.class, id)`로 재조회
- 외부 의존성 없음(리포지토리/서비스/Reader-Writer 미사용)

---

## 구현 계획(최소) + 각 단계에서 확인할 테스트

### Step 1) `Staff` 엔티티 최소 필드/매핑 정의

- 요구 매핑(최소)
  - 테이블: `staff`
  - PK: `id` (자동 증가)
  - 컬럼:
    - `shop_id` (NOT NULL)
    - `name` (nullable 허용)
    - `created_at` / `updated_at` (자동 타임스탬프)

- **예시 코드(diff 스타일, 참고용 — 아직 적용하지 않음)**

```diff
@Entity
@Table(name = "staff")
public class Staff {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "shop_id", nullable = false)
  private Long shopId;

  @Column(name = "name", length = 100)
  private String name;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public static Staff create(Long shopId, String name) { ... }
}
```

- **확인할 테스트**
  - `StaffJpaMappingTest.canPersistAndLoadStaff()` (1-A-1)

### Step 2) `@DataJpaTest` 매핑 테스트 추가/정리

- 최소 시나리오
  - given: `Staff.create(1L, "기본")`
  - when: persist 후 `flush/clear`
  - then: `id != null`, `shopId == 1L`, `name == "기본"`

- **확인할 테스트**
  - `StaffJpaMappingTest.canPersistAndLoadStaff()` (1-A-1)

---

## 최소 구현 전략(이번 Green에서 하지 않을 것 포함)

- **이번 Green에서 할 것**
  - Staff 엔티티/테이블 매핑이 깨지지 않음을 테스트로 고정

- **이번 Green에서 하지 않을 것(명시)**
  - `StaffReader` / `StaffWriter` / Repository 추가(다음 항목 `1-A-2`, `1-A-3`에서)
  - Shop 생성 시 기본 Staff 자동 생성(`1-B-*`에서)
  - Reservation 도메인에 `staff_id` FK 연결/검증(`1-C`, `1-D`에서)

---

## Tidy First(구조/동작 분리) 계획

- **필요 없음(이번 1-A-1 범위)**: 신규 엔티티/테스트 추가 수준으로 구조 변경(리네임/추출/이동)을 요구하지 않는다.
- 단, 기존 패키지 구조와 충돌하거나 순환 의존이 발생한다면:
  - (Structural) 패키지 경로/네이밍 정리 커밋을 먼저 하고
  - (Behavioral) Staff 엔티티/테스트 도입을 다음 커밋으로 분리한다.

---

## 리스크/디버깅 포인트

- **스키마 불일치**: Flyway의 `staff` 스키마(컬럼명/nullable)와 JPA 매핑이 다르면 런타임에서 DDL/쿼리 오류가 날 수 있음
  - 특히 `created_at`, `updated_at` nullable/기본값 여부 확인 필요
- **ID 전략**: 운영(MySQL)에서 `IDENTITY`가 기대대로 동작하는지 확인 필요
- **테스트 DB 설정**: `@DataJpaTest`에서 DDL 생성 정책이 다르면 테이블 생성/매핑 시점 문제가 드러날 수 있음

---

## 완료 정의(DoD)

- `1-A-1` 테스트가 **RED → GREEN**으로 전환되어 통과한다.
- Staff 엔티티의 최소 매핑(테이블/컬럼명/nullable)이 문서와 일치한다.
- 다른 Phase의 요구사항(Reader/Writer, Shop 연동 등)은 이번 변경에 포함되지 않는다.

