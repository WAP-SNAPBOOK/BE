# `#115` Worklog

## 2026-03-25 17:44:43 +09:00

### 작업 단위

- `#115` 문서 기준선 재생성

### Structural / Behavioral

- Structural

### 변경 내용

- 이슈 초안, 설계 메모, 구현 계획, worklog를 현재 규칙 기준으로 다시 생성했다.

### 이유

- 현재 규칙에서는 이슈 기반 작업을 문서 기준선부터 시작해야 한다.

### 검증

- `docs/issues/#115` 경로 생성 확인

### 다음 작업

- Structural 변경으로 예약 진입 조회 구조를 추가한다.

---

## 2026-03-25 17:45:42 +09:00

### 작업 단위

- `#115` Structural 변경

### Structural / Behavioral

- Structural

### 변경 내용

- `ShopReader`에 `readBySlugOrPublicCode`를 추가했다.
- `ShopService`, `LinkService`가 공통 `slugOrCode` 조회를 재사용하게 정리했다.
- `StaffRepository`, `StaffReader`에 `id ASC` 기준 직원 조회 메서드를 추가했다.
- `bookingentry` 패키지에 예약 진입 응답 DTO와 서비스 구조를 추가했다.

### 이유

- 링크 진입과 채팅 예약 진입에서 사용할 공통 조회 구조를 먼저 안정적으로 분리해야 했다.

### 검증

- 컴파일/테스트 전 단계라 아직 실행하지 않음

### 다음 작업

- Behavioral 변경으로 엔드포인트와 보안 설정, 레거시 API 표시를 추가한다.

---

## 2026-03-25 17:47:02 +09:00

### 작업 단위

- `#115` Behavioral 변경 및 테스트

### Structural / Behavioral

- Behavioral

### 변경 내용

- `booking entry` 공개/인증 엔드포인트를 추가했다.
- 공개 링크 경로를 `SecurityConfig.allowUrls`에 추가했다.
- 레거시 `shopId` 기반 예약 가능 시간 API에 deprecated 표시와 cleanup TODO를 추가했다.
- 예약 진입 통합 테스트와 allowlist 테스트를 추가했다.

### 이유

- 프론트가 링크 진입과 채팅 예약 버튼에서 같은 방식으로 직원 선택 데이터를 조회할 수 있어야 했다.

### 검증

- `./gradlew test --tests "com.example.easybooking.bookingentry.presentation.BookingEntryControllerIntegrationTest" --tests "com.example.easybooking.auth.SecurityConfigAllowUrlsTest"` 통과

### 다음 작업

- 변경을 `Structural`과 `Behavioral` 커밋으로 분리한다.
