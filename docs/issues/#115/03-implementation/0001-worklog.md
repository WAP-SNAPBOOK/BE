# `#115` Worklog

## 2026-03-25 21:58:55 +09:00

### 작업 단위

- `#115` 구현 원복 후 계획 단계 재시작

### Structural / Behavioral

- Structural

### 변경 내용

- 기존 `#115` 구현 커밋 두 개를 되돌려 코드와 이슈 문서를 원복했다.
- 현재 규칙 기준으로 이슈 초안, 설계 메모, 구현 계획, worklog를 다시 생성했다.

### 이유

- 코드 변경 없이 구현 직전 단계까지 다시 정리하고, 다음 구현 단위를 더 작게 나눠서 계획할 필요가 있었다.

### 검증

- `git log`에서 `#115` 구현 커밋이 revert 되었음을 확인했다.
- `docs/issues/#115` 경로를 다시 생성했다.

### 다음 작업

- `slugOrCode` 공통 조회 추출부터 가장 작은 다음 변경으로 시작한다.

---

## 2026-03-25 22:03:27 +09:00

### 작업 단위

- `refactor(shop): add slug-or-code shop resolver [Structural]`

### Structural / Behavioral

- Structural

### 변경 내용

- `ShopReader`에 `readBySlugOrPublicCode`를 추가했다.
- `ShopService.getShopInfo(String)`가 공통 조회 메서드를 재사용하게 정리했다.
- `LinkService.resolveShop`도 공통 조회 메서드를 재사용하게 정리했다.

### 이유

- 공개 링크와 매장 조회가 같은 `slugOrCode` 해석 규칙을 사용해야 했고, 이후 예약 진입 서비스도 같은 조회를 재사용할 수 있어야 했다.

### 검증

- `./gradlew test` 통과
- 기존 테스트 경고만 존재했고 실패는 없었다.

### 다음 작업

- `StaffRepository`, `StaffReader`에 정렬 조회를 추가한다.

---

## 2026-03-25 22:04:58 +09:00

### 작업 단위

- `refactor(staff): add ordered staff lookup by shop [Structural]`

### Structural / Behavioral

- Structural

### 변경 내용

- `StaffRepository`에 `findByShopIdOrderByIdAsc`를 추가했다.
- `StaffReader`에 정렬 조회용 메서드를 추가했다.

### 이유

- 예약 진입 응답에서 직원 목록과 기본 직원을 안정적으로 결정하려면 명시적인 정렬 조회가 필요했다.

### 검증

- `./gradlew test` 통과
- 기존 테스트 경고만 존재했고 실패는 없었다.

### 다음 작업

- `bookingentry` DTO와 서비스로 응답 조립 구조를 추가한다.

---

## 2026-03-25 22:06:26 +09:00

### 작업 단위

- `refactor(bookingentry): add booking entry response assembly [Structural]`

### Structural / Behavioral

- Structural

### 변경 내용

- `bookingentry` 패키지에 `BookingEntryResponse`, `BookingEntryStaffResponse`를 추가했다.
- `BookingEntryService`를 추가해 `shopId`, `slugOrCode` 기반 응답 조립 구조를 만들었다.

### 이유

- 엔드포인트를 열기 전에 예약 진입 응답의 조립 구조를 먼저 분리해야 이후 공개/인증 API가 같은 서비스를 재사용할 수 있다.

### 검증

- `./gradlew test` 통과

### 다음 작업

- 공개 booking entry 엔드포인트와 allowlist를 추가한다.
