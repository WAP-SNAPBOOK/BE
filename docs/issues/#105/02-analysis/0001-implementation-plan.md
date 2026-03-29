# `#105` 후속 구현 계획

작성일: `2026-03-26`  
기준 이슈: `#105`  
관련 설계: `docs/issues/#105/02-analysis/0002-contract-gap-and-fix-plan.md`

## 목적

`#105`의 현재 계약 갭 중 우선순위가 높은 두 가지를 실제 코드로 정리한다.

- `requirements`를 예약 데이터로 실제 저장하고 재조회 가능하게 만든다.
- 요청/생성응답/상세응답/목록응답의 필드명을 새 표준(`requirements`, `imageUrls`, `imageCount`) 기준으로 맞춘다.

## 작업 시작 시점 기준 핵심 사실

- `ReservationCreateRequest`는 이미 `requirements`, `imageUrls`를 받는다.
- `Reservation` 엔티티에는 `requirements` 필드가 없어서 재조회가 되지 않는다.
- 생성 응답은 `requests`, `photoUrls`, `photoCount`만 반환한다.
- 상세/목록 응답은 `photoUrls`, `photoCount`만 있고 `requirements`가 없다.

## 이번 작업 범위

### 포함

- `reservations.requirements` nullable 컬럼 추가
- `Reservation` 엔티티 매핑 추가
- 생성 시 `requirements` 저장
- 생성 응답에 새 표준 필드 추가
- 상세/목록 응답에 `requirements`, `imageUrls`, `imageCount` 추가
- 기존 레거시 응답 필드(`requests`, `photoUrls`, `photoCount`) 유지
- 관련 단위/통합/JPA 매핑 테스트 보강
- worklog 및 API 문서 정합화

### 제외

- `time` 응답 포맷 변경
- 레거시 응답 필드 제거
- 예약 수정 API 재설계

## 구현 순서

### 1. 구조 변경

- Flyway 마이그레이션으로 `requirements` 컬럼을 추가한다.
- `Reservation` 엔티티에 `requirements` 필드를 매핑한다.
- 도메인 생성 팩토리와 setter를 정리해 이후 동작 변경이 한 곳에서만 일어나게 만든다.

### 2. 동작 변경

- 예약 생성 시 `request.requirements`를 엔티티에 저장한다.
- 생성 응답에 아래 표준 필드를 추가한다.
  - `requirements`
  - `imageUrls`
  - `imageCount`
- 상세/목록 응답도 같은 의미의 표준 필드를 노출한다.
- 레거시 필드는 제거하지 않고 병행 유지한다.

### 3. 검증

- 생성 응답에서 새 필드와 레거시 필드가 함께 내려가는지 확인한다.
- 생성 후 재조회 시 `requirements`가 유지되는지 확인한다.
- 상세/목록 응답에서 `requirements`, `imageUrls`, `imageCount`가 노출되는지 확인한다.
- JPA 매핑 테스트로 컬럼 영속화를 확인한다.

## 리스크와 대응

### DB 스키마 변경

- `requirements`는 nullable로 추가해 기존 데이터와 충돌을 피한다.

### API 응답 필드 확장

- additive 방식으로만 추가하고 기존 필드는 유지해 프론트 회귀를 줄인다.

### 테스트 범위 누락

- 생성, 상세, 고객 목록, 점주 목록, 채팅 컨텍스트까지 응답 경로를 모두 커버한다.

## 완료 기준

- [x] `requirements`가 DB에 저장되고 조회된다.
- [x] 생성 응답에 `requirements`, `imageUrls`, `imageCount`가 추가된다.
- [x] 상세/목록 응답에 `requirements`, `imageUrls`, `imageCount`가 추가된다.
- [x] 기존 `requests`, `photoUrls`, `photoCount`는 계속 유지된다.
- [x] 관련 테스트가 통과한다.
