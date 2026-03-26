# `#107` `reservation/domain` 테스트 정리 문제 스캔

작성일: `2026-03-09`

## Context

`src/test/java/com/example/easybooking/reservation/domain` 폴더를 `study/study-tdd/프로젝트_TDD_적용_가이드라인.md` 기준으로 정리하기 전에 현재 상태를 스캔한다.

이번 스캔의 목적은 다음 두 가지다.

- 현재 테스트가 실제 도메인 계약을 얼마나 보호하는지 확인
- 무엇을 유지하고 무엇을 정리할지 우선순위를 정하기

---

## 증상 / 징후

### 1. 도메인 테스트인데 JPA 매핑 검증 비중이 과도하다

현재 폴더의 테스트 9개는 모두 `@DataJpaTest`다.

대상 파일:

- `src/test/java/com/example/easybooking/reservation/domain/ReservationCreateStartAtDualWriteRedTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationDurationMinutesJpaMappingTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationMenuInputValueJpaMappingTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationMenuInputValueUniqueConstraintTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationMenuItemJpaMappingTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationMenuItemSnapshotIntegrationTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationMenuItemUniqueConstraintTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationStaffIdJpaMappingTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationStartAtJpaMappingTest.java`

가이드라인 기준:

- Domain은 가능하면 DB 없이 먼저 테스트
- 경계값과 예외 케이스 포함
- 공개 API만 사용

현재 상태는 persistence 검증은 있으나 `Reservation`의 핵심 행위 규칙 테스트는 비어 있다.

### 2. `Reservation`의 상태 전이 규칙 테스트가 없다

`src/main/java/com/example/easybooking/reservation/domain/Reservation.java`에는 다음 공개 행위가 있다.

- `createReservation(...)`
- `confirm(...)`
- `reschedule(...)`
- `reject(...)`
- `cancel()`

하지만 현재 폴더에는 아래 계약을 직접 검증하는 순수 단위 테스트가 없다.

- 생성 시 `startAt`, `status` 초기값
- `PENDING -> CONFIRMED`
- `PENDING -> REJECTED`
- `PENDING -> CANCELED`
- 비정상 상태에서의 예외
- `reschedule()` 시 `time`, `startAt` 동시 변경

### 3. `Red` 잔재 테스트가 남아 있다

파일:

- `src/test/java/com/example/easybooking/reservation/domain/ReservationCreateStartAtDualWriteRedTest.java`

문제:

- 현재 통과하는 테스트인데 클래스명에 `Red`가 남아 있다.
- 가이드라인이 정리 대상으로 명시한 `Red` 잔재 패턴에 해당한다.

### 4. 테스트 이름과 실제 범위가 일부 어긋난다

파일:

- `src/test/java/com/example/easybooking/reservation/domain/ReservationMenuItemSnapshotIntegrationTest.java`

문제:

- 이름은 `IntegrationTest`지만 실제 어노테이션은 `@DataJpaTest`다.
- 실패 원인 추적 시 테스트 범위 해석이 불분명하다.

### 5. 일부 허용 케이스 테스트는 결과 계약 검증이 약하다

파일:

- `src/test/java/com/example/easybooking/reservation/domain/ReservationMenuItemUniqueConstraintTest.java`
- `src/test/java/com/example/easybooking/reservation/domain/ReservationMenuInputValueUniqueConstraintTest.java`

문제:

- 중복 불가 케이스는 잘 검증하지만, 허용 케이스는 "예외가 나지 않는다" 수준에 가깝다.
- 저장 후 조회 또는 핵심 필드 검증이 없어 계약 강도가 약하다.

---

## 재현 절차

1. `src/test/java/com/example/easybooking/reservation/domain` 파일 목록을 확인한다.
2. 각 테스트가 `@DataJpaTest`인지, 순수 단위 테스트가 있는지 확인한다.
3. `Reservation` 도메인 공개 행위를 확인한다.
4. `./gradlew test --tests "com.example.easybooking.reservation.domain.*"`를 실행한다.

확인 결과:

- 현재 테스트 묶음은 통과한다.
- 문제는 "깨지는 테스트"보다 "도메인 계약 보호 범위가 비어 있음"에 가깝다.

---

## 영향 범위

### 도메인

- `Reservation` 상태 전이 규칙 회귀를 빠르게 잡지 못한다.
- `confirm`, `reject`, `cancel`, `reschedule` 변경 시 DB 테스트까지 가야 의도가 드러난다.

### 유지보수 비용

- 단순 도메인 규칙 수정에도 JPA 슬라이스 테스트 비용을 먼저 지불한다.
- 테스트가 느리게 쌓이면 Red-Green-Refactor 사이클이 무거워진다.

### 설계

- 도메인 공개 API 중심 테스트보다 persistence 중심 테스트가 많아, 설계 논의가 "행위"보다 "컬럼" 중심으로 흐르기 쉽다.

### 신뢰도

- 매핑/제약은 어느 정도 보호되지만, 실제 비즈니스 규칙 보호는 상대적으로 약하다.

---

## 리팩토링 후보 목록

### P1. `Reservation` 순수 단위 테스트 추가

근거:

- 가이드라인의 Domain 우선 원칙과 가장 직접적으로 맞는다.
- 상태 전이와 예외 계약을 가장 높은 신호로 보호할 수 있다.

대상:

- `createReservation`
- `confirm`
- `reschedule`
- `reject`
- `cancel`

### P1. `ReservationCreateStartAtDualWriteRedTest` 정리

근거:

- 현재 의미와 이름이 불일치한다.
- 도메인 생성 규칙 테스트로 흡수하거나 `JpaMappingTest` 성격으로 rename 해야 한다.

### P2. 네이밍/범위 정리

근거:

- `ReservationMenuItemSnapshotIntegrationTest`는 `@DataJpaTest` 범위를 이름에 맞게 드러내는 편이 낫다.

### P2. 허용 케이스 제약 테스트 강화

근거:

- 허용 케이스도 저장 결과를 확인해야 "왜 허용되는지"가 더 명확해진다.

### P3. 저신호 setter 기반 매핑 테스트 재평가

대상:

- `ReservationStartAtJpaMappingTest`
- `ReservationStaffIdJpaMappingTest`
- `ReservationDurationMinutesJpaMappingTest`

근거:

- 스키마 회귀 방지 가치는 있지만, 도메인 정리의 첫 우선순위는 아니다.
- 일부는 도메인 행위 테스트와 역할이 겹치거나 더 강한 계약으로 대체 가능하다.

---

## 지금 당장 안 하면 생기는 비용

- `Reservation` 규칙 변경 시 느린 JPA 테스트에 먼저 의존하게 된다.
- 도메인 행위 버그가 매핑 테스트 사이에 숨어 발견 시점이 늦어진다.
- `Red` 잔재와 범위 불일치가 계속 쌓이면 이후 정리 비용이 커진다.
- 테스트 수는 충분해 보여도 실제 보호 범위는 비어 있는 상태가 유지된다.
