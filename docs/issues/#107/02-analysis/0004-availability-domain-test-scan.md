# `#107` `availability.domain` 테스트 분석

작성일: `2026-03-09`

## Context

분석 대상은 `src/test/java/com/example/easybooking/availability/domain` 하위 테스트와 대응 프로덕션 도메인 클래스다.

검토 기준은 `study/study-tdd/프로젝트_TDD_적용_가이드라인.md` 의 다음 원칙에 맞췄다.

* Domain 테스트는 공개 API만 사용한다.
* 구현이 아니라 계약을 보호한다.
* 경계값과 예외 케이스를 포함한다.
* DB 제약에 기대기 전에 도메인 규칙을 단위 테스트로 고정한다.

검토한 테스트 파일:

* `src/test/java/com/example/easybooking/availability/domain/ShopHolidayTest.java`
* `src/test/java/com/example/easybooking/availability/domain/ShopOperatingTimeTest.java`
* `src/test/java/com/example/easybooking/availability/domain/ShopSettingsTest.java`
* `src/test/java/com/example/easybooking/availability/domain/StaffOperatingTimeTest.java`

함께 확인한 프로덕션 파일:

* `src/main/java/com/example/easybooking/availability/domain/ShopHoliday.java`
* `src/main/java/com/example/easybooking/availability/domain/ShopOperatingTime.java`
* `src/main/java/com/example/easybooking/availability/domain/ShopSettings.java`
* `src/main/java/com/example/easybooking/availability/domain/StaffOperatingTime.java`
* `src/main/java/com/example/easybooking/availability/domain/PublicHoliday.java`

## 증상/징후

### 재현 절차

다음 명령으로 대상 테스트만 실행했다.

```bash
./gradlew test --tests "com.example.easybooking.availability.domain.*"
```

실행 결과는 성공이다.

### 확인된 징후

좋은 징후:

* 네 테스트 모두 mock, reflection, `ReflectionTestUtils` 없이 공개 팩토리/행위 메서드만 사용한다.
* collaborator 호출 횟수 대신 생성 결과와 상태를 직접 검증한다.
* `ShopSettingsTest` 는 예외 타입뿐 아니라 `AvailabilityErrorCode` 까지 고정한다.
* `ShopOperatingTimeTest`, `StaffOperatingTimeTest` 는 `start == end` 경계값을 이미 다룬다.

부족한 징후:

* 정상 생성 확인 비중이 높고 null/예외 경계가 전반적으로 약하다.
* 일부 공개 API가 테스트되지 않았다.
* `availability.domain` 프로덕션 클래스 중 `PublicHoliday` 는 대응 테스트가 없다.
* 테스트 클래스명은 단위 테스트 성격이지만 `XxxUnitTest` 규칙과는 다르다.

## 영향 범위

도메인 규칙 영향:

* 시간 범위, 휴무 타입, 스케줄 설정 같은 핵심 규칙이 일부만 고정되어 있다.
* 현재는 DB `nullable = false` 나 호출자 검증에 규칙 일부를 위임한 상태다.

리팩토링 영향:

* 현재 테스트는 구현 결합도가 낮아 리팩토링을 막는 편은 아니다.
* 대신 실패 경계가 비어 있어 회귀를 초기에 막는 힘이 부족하다.

유지보수 영향:

* 신규 규칙이 추가되면 테스트를 확장하기는 쉽다.
* 하지만 지금 상태로는 "허용하면 안 되는 입력" 이 계속 묵인될 가능성이 있다.

## 파일별 분석

### `ShopHolidayTest`

현재 보호하는 계약:

* `createWeekly()`, `createBiweekly()`, `createMonthly()`, `createCustom()` 가 올바른 `HolidayType` 과 주요 필드를 세팅한다.

부족한 계약:

* 타입별 비관련 필드가 `null` 이어야 한다는 계약이 없다.
* 월차 휴무의 `weekOfMonth` 범위 계약이 없다.
* `dayOfWeek`, `referenceDate`, `specificDate` 에 대한 null 방어 계약이 없다.

추가 필요 테스트:

* `createWeekly_생성시_참조일자주차특정일자는_null`
* `createBiweekly_생성시_주차특정일자는_null`
* `createMonthly_생성시_참조일자특정일자는_null`
* `createCustom_생성시_요일주차참조일자는_null`
* `createMonthly_주차가_유효범위를벗어나면_예외`
* `createWeekly_요일이null이면_예외`
* `createBiweekly_기준일자가null이면_예외`
* `createCustom_특정일자가null이면_예외`

메모:

* 마지막 네 테스트는 현재 구현이 아직 검증을 하지 않으므로, 먼저 비즈니스 규칙 확정이 필요하다.

### `ShopOperatingTimeTest`

현재 보호하는 계약:

* 정상 생성
* `startTime > endTime` 예외
* `startTime == endTime` 허용

부족한 계약:

* `startTime == null`, `endTime == null` 예외가 빠져 있다.
* 실패 후 상태 불변 계약은 정적 팩토리 특성상 크게 중요하지 않지만, 입력 검증 범위는 더 좁힐 수 있다.
* `shopId`, `dayOfWeek` null 금지 여부가 테스트로 표현되지 않는다.

추가 필요 테스트:

* `create_시작시간이null이면_예외`
* `create_종료시간이null이면_예외`
* `create_요일이null이면_예외`
* `create_상점ID가null이면_예외`

메모:

* 마지막 두 테스트는 도메인 규칙으로 올릴지 먼저 결정해야 한다.

### `ShopSettingsTest`

현재 보호하는 계약:

* `createDefault()` 기본값
* `updateInterval()` 허용 값
* `updateInterval()` 실패 시 `AvailabilityErrorCode.INVALID_INTERVAL_MINUTES`

강점:

* 이 패키지에서 가장 계약 중심적이다.
* broad assertion 대신 에러 코드를 고정해 회귀 방지 가치가 높다.

부족한 계약:

* 공개 메서드 `updateScheduleType()` 이 미테스트다.
* `updateInterval()` 실패 시 기존 값 유지 여부가 없다.

추가 필요 테스트:

* `updateScheduleType_유효한값이면_스케줄타입이변경된다`
* `updateScheduleType_null이면_예외`
* `updateInterval_실패하면_기존간격을유지한다`

### `StaffOperatingTimeTest`

현재 보호하는 계약:

* 정상 근무시간 생성
* 휴무 override 생성
* `startTime == endTime` 허용
* `startTime > endTime` 예외

부족한 계약:

* `startTime == null`, `endTime == null` 예외가 없다.
* `staffId`, `dayOfWeek` null 금지 여부가 정의되지 않았다.
* `createOff()` 는 시간 필드가 null 인지만 확인하고, 휴무 계약이 더 늘어날 여지는 남아 있다.

추가 필요 테스트:

* `create_시작시간이null이면_예외`
* `create_종료시간이null이면_예외`
* `create_요일이null이면_예외`
* `create_직원ID가null이면_예외`
* `createOff_휴무생성시_시간범위는항상null`

### `PublicHoliday`

현재 상태:

* `src/main/java/com/example/easybooking/availability/domain/PublicHoliday.java` 는 공개 팩토리 `create(LocalDate holidayDate, String name)` 를 제공한다.
* 하지만 대응 테스트가 없다.

영향:

* 현재는 단순 생성기처럼 보여도, 공휴일 계산 로직이 붙기 쉬운 엔트리 포인트인데 보호막이 전혀 없다.

추가 필요 테스트:

* `create_정상입력이면_공휴일정보를반환한다`
* `create_공휴일날짜가null이면_예외`

메모:

* 두 번째 테스트는 현재 구현이 통과하지 못한다.

## 리팩토링 후보 목록

### P1. 공개 API 미테스트 영역 보강

대상:

* `PublicHoliday.create()`
* `ShopSettings.updateScheduleType()`

근거:

* 공개 API가 있는데 테스트가 없으면 추후 규칙 추가 시 회귀를 막을 수 없다.

### P1. null 입력 경계 테스트 보강

대상:

* `ShopOperatingTime.create()`
* `StaffOperatingTime.create()`

근거:

* 현재 구현은 null 을 예외로 처리하지만 테스트는 `start > end` 만 확인한다.
* 가이드라인의 "경계값과 예외 케이스 포함" 원칙에 직접 대응한다.

### P2. 타입별 불변식 테스트 추가

대상:

* `ShopHoliday`

근거:

* holiday type 마다 관련 없는 필드는 비어 있어야 한다는 계약이 있으면 이후 매핑/변환 로직 리팩토링이 쉬워진다.

### P3. 도메인 입력 검증 규칙 명시

대상:

* `ShopHoliday`
* `ShopOperatingTime`
* `StaffOperatingTime`

근거:

* `shopId`, `staffId`, `dayOfWeek`, `weekOfMonth` 의 유효 범위를 DB 제약이 아니라 도메인 계약으로 올릴지 결정해야 한다.

## 지금 당장 안 하면 생기는 비용

* `PublicHoliday` 와 `ShopSettings.updateScheduleType()` 변경 시 회귀가 테스트 없이 들어갈 수 있다.
* null 입력이 서비스나 persistence 레이어에서 늦게 터져 원인 추적 비용이 커질 수 있다.
* `ShopHoliday` 타입별 불변식이 문서와 테스트로 고정되지 않아, 이후 조회/직렬화 코드가 암묵 규칙에 의존하게 된다.

## 결론

`availability.domain` 테스트는 현재 프로젝트 전체 기준으로는 품질이 양호하다.

하지만 `study-tdd` 기준으로 보면 아직 "정상 생성 확인" 중심이다. 다음 보강 순서는 아래가 적절하다.

1. `PublicHoliday` 기본 계약 테스트 추가
2. `ShopSettings.updateScheduleType()` 성공/실패 테스트 추가
3. `ShopOperatingTime`, `StaffOperatingTime` null 시간 경계 테스트 추가
4. `ShopHoliday` 타입별 불변식 테스트 추가
