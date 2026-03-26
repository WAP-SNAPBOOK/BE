# `#107` `reservation/domain` 테스트 분석

작성일: `2026-03-09`

## 분석 기준

기준 문서:

- `study/study-tdd/프로젝트_TDD_적용_가이드라인.md`

특히 다음 항목을 기준으로 봤다.

- Domain은 DB 없이 먼저 테스트할 수 있으면 단위 테스트로 작성
- 경계값과 예외 케이스 포함
- 생성자/팩토리/행위 메서드의 공개 API만 사용
- 테스트 이름과 범위를 맞출 것
- `Red` 잔재 테스트를 남기지 말 것

실행 확인:

```bash
./gradlew test --tests "com.example.easybooking.reservation.domain.*"
```

현재 해당 테스트 묶음은 통과한다.

---

## 한 줄 결론

현재 `reservation/domain`은 "나쁜 테스트가 많은 폴더"라기보다, "JPA 매핑 테스트는 충분하지만 진짜 도메인 행위 테스트가 비어 있는 폴더"에 가깝다.

---

## 파일별 평가

### 1. 유지하되 이름 정리 필요

#### `ReservationCreateStartAtDualWriteRedTest`

평가:

- `Reservation.createReservation(...)`이 `date + time`으로 `startAt`을 채운다는 계약을 검증한다.
- 검증 자체는 유효하다.
- 다만 현재 통과하는 테스트에 `Red`가 남아 있어 의미가 어긋난다.

권장 조치:

- `ReservationCreateStartAtJpaMappingTest`로 rename 하거나
- 더 나은 선택으로는 순수 단위 테스트 `ReservationUnitTest`의 생성 규칙 테스트로 흡수

우선순위:

- 높음

#### `ReservationMenuItemSnapshotIntegrationTest`

평가:

- 원본 `ShopMenu` 이름이 바뀌어도 `ReservationMenuItem.menuNameSnapshot`이 유지된다는 계약을 검증한다.
- 결과 중심 검증이고 비즈니스 가치가 분명하다.

문제:

- 이름은 `IntegrationTest`인데 실제로는 `@DataJpaTest`다.

권장 조치:

- `ReservationMenuItemSnapshotJpaTest` 또는 `ReservationMenuItemSnapshotPersistenceTest`로 rename

우선순위:

- 중간

### 2. 유지 가치가 높은 JPA 테스트

#### `ReservationMenuItemJpaMappingTest`

평가:

- 핵심 필드를 flush/clear 후 검증한다.
- 가이드라인의 persistence 기준과 잘 맞는다.

권장 조치:

- 유지

#### `ReservationMenuInputValueJpaMappingTest`

평가:

- 핵심 필드를 충분히 검증한다.
- value number/text snapshot까지 확인해 매핑 테스트로서 신호가 좋다.

권장 조치:

- 유지

#### `ReservationMenuItemUniqueConstraintTest`

평가:

- 중복 불가 제약을 `saveAndFlush`로 검증해 flush 시점도 명확하다.
- 부정 케이스 검증은 적절하다.

보완점:

- 허용 케이스에서 저장 후 조회 결과까지 확인하면 더 좋다.

권장 조치:

- 유지 + 허용 케이스 단언 강화

#### `ReservationMenuInputValueUniqueConstraintTest`

평가:

- 중복 불가 제약 검증 방식은 적절하다.
- broad assertion 문제도 없다.

보완점:

- 허용 케이스에서 실제 저장 결과를 확인하지 않는다.

권장 조치:

- 유지 + 허용 케이스 단언 강화

### 3. 유지 여부 재평가 대상

#### `ReservationStartAtJpaMappingTest`

평가:

- `startAt` 컬럼 round-trip 자체는 보호한다.
- 하지만 `setStartAt()`로 값만 넣고 그대로 저장하는 구조라 도메인 행위 관점의 신호는 약하다.

판단:

- 스키마 회귀 방지 목적이면 유지 가능
- 도메인 정리 우선순위에서는 후순위

권장 조치:

- 당장 삭제하지는 말고, 순수 단위 테스트 추가 후 중복 가치 재평가

#### `ReservationStaffIdJpaMappingTest`

평가:

- `staffId` nullable 매핑 보호 외 가치는 크지 않다.
- setter round-trip 테스트라 계약 강도는 낮다.

권장 조치:

- 후순위 유지
- 나중에 `confirm` 또는 staff 할당 행위가 도입되면 더 강한 테스트로 대체 검토

#### `ReservationDurationMinutesJpaMappingTest`

평가:

- `durationMinutes` 매핑 회귀 방지 목적은 있다.
- 마찬가지로 setter round-trip 테스트라 행위 기반 보호는 약하다.

권장 조치:

- 후순위 유지
- `confirm(message, durationMinutes)` 단위 테스트 추가 후 중복 여부 재평가

---

## 빠진 테스트

현재 가장 큰 공백은 아래 순수 단위 테스트 부재다.

### `Reservation.createReservation(...)`

필요 계약:

- `status`가 `PENDING`으로 시작
- `startAt`이 `date + time`으로 계산됨
- 입력한 `shopId`, `ownerUserId`, `customerId`, `date`, `time`이 그대로 세팅됨

### `Reservation.confirm(...)`

필요 계약:

- `PENDING`에서만 성공
- `status`가 `CONFIRMED`로 변경
- `confirmationMessage`, `durationMinutes` 저장
- 이미 `CONFIRMED`, `REJECTED`, `CANCELED` 상태면 예외

### `Reservation.reschedule(...)`

필요 계약:

- `PENDING`에서만 성공
- `time`과 `startAt`이 함께 변경
- 비정상 상태에서는 예외

### `Reservation.reject(...)`

필요 계약:

- `PENDING`에서만 성공
- `rejectionReason`, `status=REJECTED`
- 비정상 상태에서는 예외

### `Reservation.cancel()`

필요 계약:

- `PENDING`, `CONFIRMED`에서는 취소 가능
- 이미 `CANCELED`, `REJECTED`면 예외

---

## 추천 정리 순서

### 1단계. 도메인 단위 테스트 추가

새 파일 예시:

- `ReservationUnitTest`

먼저 이 파일에 상태 전이와 예외 계약을 채운다.

### 2단계. `Red` 잔재 제거

대상:

- `ReservationCreateStartAtDualWriteRedTest`

rename 또는 단위 테스트로 흡수한다.

### 3단계. JPA 테스트 네이밍/단언 정리

대상:

- `ReservationMenuItemSnapshotIntegrationTest`
- `ReservationMenuItemUniqueConstraintTest`
- `ReservationMenuInputValueUniqueConstraintTest`

### 4단계. setter 기반 매핑 테스트 가치 재판단

대상:

- `ReservationStartAtJpaMappingTest`
- `ReservationStaffIdJpaMappingTest`
- `ReservationDurationMinutesJpaMappingTest`

---

## 액션 분류

### Keep

- `ReservationMenuItemJpaMappingTest`
- `ReservationMenuInputValueJpaMappingTest`
- `ReservationMenuItemUniqueConstraintTest`
- `ReservationMenuInputValueUniqueConstraintTest`
- `ReservationMenuItemSnapshotIntegrationTest`

### Modify

- `ReservationCreateStartAtDualWriteRedTest`
- `ReservationMenuItemSnapshotIntegrationTest`
- `ReservationMenuItemUniqueConstraintTest`
- `ReservationMenuInputValueUniqueConstraintTest`

### Re-evaluate Later

- `ReservationStartAtJpaMappingTest`
- `ReservationStaffIdJpaMappingTest`
- `ReservationDurationMinutesJpaMappingTest`

### Add

- `ReservationUnitTest`

---

## 추천 구현 방향

이 폴더 정리는 "JPA 테스트 삭제"부터 시작하면 안 된다.

우선순위는 다음이 맞다.

1. 도메인 행위 단위 테스트 추가
2. `Red` 잔재와 잘못된 네이밍 수정
3. 허용 케이스 단언 강화
4. 마지막에 중복 가치가 낮은 JPA 매핑 테스트를 줄일지 판단

즉, 지금 `reservation/domain`에서 가장 필요한 것은 "더 많은 매핑 테스트"가 아니라 "`Reservation` 행위 계약을 빠르게 보호하는 단위 테스트"다.
