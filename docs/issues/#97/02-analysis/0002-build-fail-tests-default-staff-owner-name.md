## 배경

`./gradlew build` 실행 시 테스트 3개가 실패하여 빌드가 중단되었다.

- `ReservationChatPublishIntegrationTest`
- `ReservationTimeBlockUniqueConstraintTest`
- `ShopDefaultStaffCreationTest`

이 문서는 실패 원인(증거 포함)과 수정 방향(결정/대안/리스크/테스트 전략)을 정리한다.

---

## 관찰(증거)

### 1) Shop 생성 직후 “기본 스태프(=name: 기본)”가 존재한다는 전제가 깨짐

- `ReservationChatPublishIntegrationTest`는 `shopService.createShop()` 직후
  `staffRepository.findByShopIdAndName(shopId, "기본")`를 호출하고 `orElseThrow()`로 실패한다.
- `ShopDefaultStaffCreationTest`는 Shop 생성 후 생성된 Staff 1명의 이름이 `"기본"`이라고 기대한다.

그러나 현재 `ShopService#createShop`의 기본 Staff 생성은 **오너 이름**으로 생성되고 있다.

### 2) ReservationTimeBlock 유니크 제약이 JPA 스키마(테스트용 create-drop)에서 보장되지 않음

- `ReservationTimeBlockUniqueConstraintTest`는 `(staff_id, block_start_at)` 중복 저장 시
  `DataIntegrityViolationException`을 기대한다.
- 하지만 현재 `ReservationTimeBlock` 엔티티에 `@Table(uniqueConstraints=...)`가 없어
  `@DataJpaTest`의 스키마 생성 단계에서 유니크 제약이 만들어지지 않는다.

또한 `ReservationTimeBlockWriter`는 유니크 제약 이름을 `"uq_rtb_staff_block_start_at"`로 전제하고 있어,
예외 매핑(충돌 → `TIME_BLOCK_ALREADY_BOOKED`)을 위해서도 제약 이름 정합성이 필요하다.

---

## 원인 분석

1. **정책/계약 불일치**
   - 문서(`plan.md`)와 테스트는 “기본 Staff의 name은 `기본`”이라는 계약을 가진다.
   - 구현은 “기본 Staff의 name은 오너 이름”으로 변경되어 계약이 깨졌다.
   - `StaffReader#getDefaultStaffByShopId`는 여전히 이름 `"기본"`으로 기본 직원 조회를 시도한다.

2. **유니크 제약 누락**
   - 운영 DB에서 Flyway로 유니크를 보장하더라도,
     `@DataJpaTest`는 엔티티 메타데이터로 테이블을 생성하므로 엔티티 수준의 제약이 없으면 테스트가 깨진다.

---

## 결정(사용자 요청 반영)

### D1. 기본 Staff의 name은 “오너 이름”으로 한다

- Shop 생성 시 자동 생성되는 기본 Staff는 `Staff(shopId, ownerName)` 규칙을 따른다.
- 테스트/문서/리더(조회)도 동일 정책으로 정렬한다.

### D2. “기본 Staff 조회”는 name 기반이 아니라 “샵의 첫 번째 Staff” 기준으로 한다

name이 정책상 가변(오너 이름)이므로, `StaffReader#getDefaultStaffByShopId`는
`shopId` 기준으로 “가장 먼저 생성된 Staff(예: id 오름차순 첫 번째)”를 기본 Staff로 간주한다.

> 주의: 이 기준은 “샵 생성 시 자동 생성된 Staff가 항상 가장 먼저 저장된다”는 전제를 가진다.
> 다수 Staff를 도입하는 후속 기능이 생기면 `is_default` 같은 명시 필드 도입을 재검토한다.

### D3. ReservationTimeBlock의 유니크 제약을 엔티티 수준에서 복구한다

- `ReservationTimeBlock`에 `(staff_id, block_start_at)` 유니크를 추가한다.
- 제약 이름은 `uq_rtb_staff_block_start_at`로 고정해 예외 매핑과 정합을 맞춘다.

---

## 변경 범위(예상)

### 코드(프로덕션)

- `StaffRepository`: 기본 Staff 조회용 메서드 추가(예: `findFirstByShopIdOrderByIdAsc`)
- `StaffReader#getDefaultStaffByShopId`: name 기반 조회 제거 → “첫 Staff” 기반 조회로 변경
- `ReservationTimeBlock`: `@Table(uniqueConstraints=...)` 추가(테이블명 포함)

### 테스트

- `ShopDefaultStaffCreationTest`: 기대 name `"기본"` → `owner.getName()`
- `ReservationChatPublishIntegrationTest`: `"기본"` name 조회 제거 → 기본 Staff 조회 방식으로 변경
- `StaffReaderTest`: `"기본"` name 전제 제거 → 첫 Staff 반환을 검증
- (선택) `StaffWriterTest`, `StaffJpaMappingTest`의 고정 문자열 `"기본"`도 정책 혼동 방지를 위해 교체

---

## 테스트 전략

- 빠른 회귀:
  - `./gradlew test --tests ...ShopDefaultStaffCreationTest`
  - `./gradlew test --tests ...ReservationChatPublishIntegrationTest`
  - `./gradlew test --tests ...ReservationTimeBlockUniqueConstraintTest`
  - `./gradlew test --tests ...StaffReaderTest`
- 전체 회귀:
  - `./gradlew build`

---

## 리스크 & 롤백

- **리스크**: “첫 Staff = 기본 Staff” 가정이 장기적으로 깨질 수 있음(직원 CRUD가 확장되면).
  - **완화**: 후속 이슈에서 `Staff.isDefault`/`defaultStaffId` 같은 명시적 모델 도입 검토.
- **리스크**: 엔티티 유니크 제약 추가가 운영 DDL과 불일치할 수 있음.
  - **완화**: 운영은 Flyway가 SSOT이므로, 제약 이름/컬럼 조합을 Flyway와 맞춰 검증.
  - **롤백**: 유니크 제약을 제거하고 애플리케이션 레벨 선검사만 유지(충돌 레이스 취약).

