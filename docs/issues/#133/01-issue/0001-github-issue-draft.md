# `[REFACTOR]: 가용성 운영시간과 휴무 규칙의 DB 제약을 정리`

Issue: `#133`  
Created: `2026-07-03`  
Repository: `WAP-SNAPBOOK/BE`  
Issue URL: `https://github.com/WAP-SNAPBOOK/BE/issues/133`  
Branch: `refactor/jiseob/#133`

---

## Background

가용성 도메인은 매장 운영시간, 직원 운영시간 override, 매장 휴무, 공휴일 설정을 기반으로 예약 가능 슬롯을 계산한다. 현재 코드는 계산형 모델을 사용하지만, 운영시간 범위와 휴무 타입별 필수 컬럼을 DB가 충분히 강제하지 않는다.

관련 코드:

- `src/main/java/com/example/easybooking/availability/domain/ShopOperatingTime.java`
- `src/main/java/com/example/easybooking/availability/domain/StaffOperatingTime.java`
- `src/main/java/com/example/easybooking/availability/domain/ShopHoliday.java`
- `src/main/java/com/example/easybooking/availability/OperatingTimeResolver.java`
- `src/main/java/com/example/easybooking/availability/HolidayChecker.java`

## Problem

`shop_operating_times`와 `staff_operating_times`는 시간 범위 정합성을 코드에서만 검증한다. `shop_holidays`는 `holiday_type`별로 필요한 컬럼이 다르지만 DB 제약이 없어 불완전한 휴무 규칙이 저장될 수 있다. 또한 직원 복수 운영시간은 TODO로 남아 있어 현재 스키마가 향후 요구를 수용할지 결정이 필요하다.

## Goal

가용성 계산의 입력 데이터가 DB 수준에서도 일관되도록 운영시간/휴무 제약과 인덱스를 정리한다.

## Scope

- `shop_operating_times`에 `start_time <= end_time` 체크 제약을 검토한다.
- `shop_operating_times(shop_id, day_of_week, sort_order)` 또는 조회 패턴에 맞는 인덱스를 검토한다.
- `staff_operating_times`의 단일 override 정책을 유지할지, 복수 구간 모델로 확장할지 결정한다.
- `staff_operating_times`에 `is_off` 상태와 `start_time/end_time` nullability 관계를 표현하는 제약을 검토한다.
- `shop_holidays`의 `holiday_type`별 필수 컬럼 제약을 정리한다.
- 중복 휴무 규칙을 막기 위한 유니크 제약 후보를 정리한다.
- `HolidayChecker`의 월별 휴무 계산 TODO와 DB 모델이 맞는지 확인한다.

## Out Of Scope

- 가용성 계산 알고리즘 전면 재작성
- 예약 점유 모델 변경
- 공휴일 데이터 수집/동기화 자동화

## Acceptance Criteria

- [ ] 운영시간 시작/종료 범위가 DB 또는 명시적 검증으로 일관되게 보장된다.
- [ ] 휴무 타입별 필수 컬럼 조합이 문서화되고 가능한 범위에서 DB 제약으로 보강된다.
- [ ] 직원 운영시간이 단일 override인지 복수 구간인지 결정된다.
- [ ] 가용성 조회 쿼리에 필요한 인덱스가 정리된다.
- [ ] 기존 데이터가 신규 제약을 만족하는지 검증할 수 있다.

## Risks

- MySQL 버전/설정에 따라 `CHECK` 제약 적용 방식이 다를 수 있다.
- 직원 복수 운영시간까지 한 번에 확장하면 작업 범위가 커진다.
- 기존 휴무 데이터가 불완전하면 마이그레이션 전 정리가 필요하다.

## References

- `src/main/java/com/example/easybooking/availability/domain/ShopOperatingTime.java`
- `src/main/java/com/example/easybooking/availability/domain/StaffOperatingTime.java`
- `src/main/java/com/example/easybooking/availability/domain/ShopHoliday.java`
- `src/main/java/com/example/easybooking/availability/OperatingTimeResolver.java`
- `src/main/java/com/example/easybooking/availability/HolidayChecker.java`
