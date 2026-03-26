## ADR 0003 — 기본 Staff 정책: 오너 이름 + 첫 Staff 기준

- Issue: `#97`
- Status: Accepted

---

## Context

샵 생성 시 `staff_id` FK를 즉시 만족시키기 위해 “기본 Staff 자동 생성”이 필요하다.
기존 문서/테스트는 기본 Staff의 이름을 `"기본"`으로 고정했으나, 구현은 오너 이름으로 생성하도록 변경되어
테스트가 실패하고 “기본 Staff 조회” 로직(`StaffReader#getDefaultStaffByShopId`)도 이름 고정에 의존하고 있었다.

---

## Decision

1. **기본 Staff의 name은 오너 이름**으로 한다.
   - Shop 생성 시 자동 생성되는 Staff는 `Staff(shopId, ownerName)`으로 생성한다.

2. **기본 Staff 조회는 name 기반이 아니라 “샵의 첫 Staff(예: id 오름차순 첫 번째)”**로 한다.
   - name이 정책상 가변이므로 `"기본"` 같은 하드코딩 문자열에 의존하지 않는다.

---

## Options Considered

### Option A) name을 `"기본"`으로 유지

- 장점: 조회/테스트가 단순, 명확한 규칙
- 단점: 오너 이름 기반 정책과 충돌, UI/도메인에서 “기본”이 사용자 기대와 어긋날 수 있음

### Option B) `is_default` 또는 `defaultStaffId` 같은 명시 필드 도입

- 장점: 기본 Staff를 명확하게 식별, 다수 Staff 도입 시에도 안정적
- 단점: 스키마 변경/마이그레이션 필요, 이번 단계(#97) 범위를 키움

### Option C) “첫 Staff”를 기본으로 간주 (채택)

- 장점: 추가 스키마 없이 정책 일치(오너 이름) + 조회 안정성(이름 비의존)
- 단점: “첫 Staff = 기본”이라는 암묵적 가정이 존재 (후속 확장 시 재검토 필요)

---

## Consequences

- 문서(`plan.md`)와 테스트는 더 이상 `"기본"` 문자열에 의존하지 않도록 정렬된다.
- 후속 이슈에서 Staff CRUD/다수 Staff 관리가 본격화되면 Option B(명시 필드)를 재검토한다.

