# `#107` `reservation/domain` 개선 리스트와 전략

작성일: `2026-03-09`

## 목적

`reservation/domain` 테스트를 `study/study-tdd/프로젝트_TDD_적용_가이드라인.md` 기준에 맞게 정리한다.

핵심 목표는 테스트 수를 늘리는 것이 아니라 다음을 달성하는 것이다.

- `Reservation`의 도메인 행위 계약을 빠르게 보호
- snapshot / unique constraint 같은 persistence 계약은 필요한 수준으로 유지
- `Red` 잔재, 범위 불일치, 약한 단언을 정리

---

## 현재 상태 요약

- 현재 폴더 테스트는 전부 `@DataJpaTest`
- 매핑/제약 테스트는 비교적 잘 갖춰져 있음
- `Reservation`의 상태 전이와 예외 규칙을 검증하는 순수 단위 테스트는 없음
- 일부 테스트는 이름이 범위와 어긋나거나 `Red` 잔재가 남아 있음
- 일부 허용 케이스는 "예외가 안 난다" 외 계약 검증이 약함

---

## 개선 리스트

### P1. `Reservation` 순수 단위 테스트 추가

대상:

- `createReservation`
- `confirm`
- `reschedule`
- `reject`
- `cancel`

이유:

- 가장 높은 신호의 도메인 계약이다.
- DB 없이 빠르게 실패 원인을 드러낼 수 있다.

### P1. `Red` 잔재 제거

대상:

- `ReservationCreateStartAtDualWriteRedTest`

이유:

- 현재 의미와 이름이 맞지 않는다.
- 이후 유지보수 시 혼란을 유발한다.

### P2. 네이밍과 범위 정리

대상:

- `ReservationMenuItemSnapshotIntegrationTest`

이유:

- `@DataJpaTest`인데 `IntegrationTest` 네이밍을 사용한다.
- 테스트 실패 시 레이어 해석이 모호해진다.

### P2. 허용 케이스 단언 강화

대상:

- `ReservationMenuItemUniqueConstraintTest`
- `ReservationMenuInputValueUniqueConstraintTest`

이유:

- 허용 케이스는 저장 성공 후 결과를 더 명확히 확인할 수 있다.
- 단순 no-exception 테스트보다 계약이 강해진다.

### P3. 저신호 매핑 테스트 재평가

대상:

- `ReservationStartAtJpaMappingTest`
- `ReservationStaffIdJpaMappingTest`
- `ReservationDurationMinutesJpaMappingTest`

이유:

- 매핑 회귀 방지 가치는 있으나 도메인 정리의 우선순위는 아니다.
- 더 강한 단위 테스트가 생긴 뒤 중복 가치를 판단할 수 있다.

---

## 전략안

### 전략 A. 단위 테스트를 먼저 추가하고 기존 JPA 테스트는 보존한 채 정리한다

방법:

1. `ReservationUnitTest`를 추가한다.
2. 상태 전이와 예외 시나리오를 먼저 고정한다.
3. 그 다음 기존 JPA 테스트의 이름과 단언을 정리한다.
4. 마지막에 저신호 JPA 테스트를 줄일지 판단한다.

장점:

- 회귀 위험이 가장 낮다.
- 현재 보호 범위를 잃지 않고 더 강한 테스트를 쌓을 수 있다.
- Tidy First 원칙에 맞게 구조/동작 변경을 분리하기 쉽다.

단점:

- 짧은 기간 동안 테스트 수가 늘어난다.
- 일부 중복 테스트가 잠시 공존한다.

### 전략 B. 기존 JPA 테스트를 먼저 줄이고 그 자리를 단위 테스트로 대체한다

방법:

1. 저신호 JPA 테스트를 선별 삭제 또는 통합한다.
2. 이후 단위 테스트로 다시 채운다.

장점:

- 테스트 묶음이 빨리 단순해진다.

단점:

- 정리 도중 보호 범위가 잠시 비어 있을 수 있다.
- 어떤 계약을 실제로 잃었는지 추적하기 어렵다.
- 현재 폴더처럼 매핑 테스트가 이미 통과하는 상태에서는 얻는 이득보다 위험이 크다.

### 추천안

전략 A를 추천한다.

이 폴더의 핵심 문제는 "쓸모없는 테스트가 너무 많다"가 아니라 "진짜 도메인 행위 테스트가 없다"이기 때문이다. 먼저 더 강한 테스트를 추가하고, 그 다음 약한 테스트를 줄이는 편이 안전하다.

---

## 단계별 실행 전략

### 1단계. 도메인 행위 보호막 추가

산출물:

- `ReservationUnitTest`

포함 범위:

- 생성 규칙
- 상태 전이
- 예외 규칙
- 파생 필드 동기화

### 2단계. 기존 테스트 정리

산출물:

- rename
- 테스트 이름 정리
- 허용 케이스 단언 보강

대상:

- `ReservationCreateStartAtDualWriteRedTest`
- `ReservationMenuItemSnapshotIntegrationTest`
- unique constraint 허용 케이스 2건

### 3단계. 저신호 매핑 테스트 재평가

질문:

- 이 테스트가 단순 컬럼 매핑 이상의 의미를 갖는가?
- 단위 테스트와 함께 둘 이유가 충분한가?
- 스키마 회귀 보호로서 남길 가치가 있는가?

대상:

- `ReservationStartAtJpaMappingTest`
- `ReservationStaffIdJpaMappingTest`
- `ReservationDurationMinutesJpaMappingTest`

---

## 리스크와 롤백

### 리스크

- 도메인 단위 테스트를 추가하면서 기존 테스트와 역할이 일부 겹칠 수 있다.
- 현재 `Reservation` API가 setter 중심이라 테스트 설계가 임시로 혼합될 수 있다.
- 한 번에 많은 테스트를 고치면 원인 분리가 어려워진다.

### 완화

- 테스트는 1개씩 추가한다.
- rename과 단언 강화는 별도 커밋 단위로 분리한다.
- 기존 JPA 테스트 삭제는 마지막까지 미룬다.

### 롤백

- 새 단위 테스트가 기대 계약을 잘못 고정한 경우 해당 테스트만 되돌리고 JPA 테스트는 유지한다.
- JPA 테스트 정리 후 문제가 생기면 삭제분만 복원하면 된다.

---

## 테스트 전략

- 가장 먼저 `ReservationUnitTest`에서 빠른 행위 테스트를 만든다.
- persistence 계약은 `@DataJpaTest`로 최소 유지한다.
- 허용 케이스는 no-exception만 보지 말고 실제 저장 결과까지 본다.
- `Red` 잔재 제거와 네이밍 정리는 동작 변경과 분리한다.

---

## 추천 작업 순서

1. `ReservationUnitTest` 초안 작성
2. `createReservation`, `confirm`, `reschedule` 시나리오부터 구현
3. `reject`, `cancel` 시나리오 추가
4. `ReservationCreateStartAtDualWriteRedTest` rename 또는 흡수
5. unique constraint 허용 케이스 단언 강화
6. `ReservationMenuItemSnapshotIntegrationTest` 네이밍 정리
7. 마지막에 저신호 매핑 테스트 유지 여부 결정

한 줄로 정리하면, 이 폴더의 개선 전략은 "기존 테스트를 지우는 작업"이 아니라 "`Reservation`의 핵심 행위 계약을 가장 먼저 빠르게 보호하는 작업"이다.
