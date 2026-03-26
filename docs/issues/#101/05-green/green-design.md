# Green Design - #101

## 대상 단계

- `04-tdd/plan.md`의 `2-2. ShopSettings 저장 및 조회`

## RED에서 검증할 것

- `ShopSettingsRepository`를 통한 `save()` 후 `findByShopId()` 조회 시나리오 테스트 추가
- 현재 상태에서는 `ShopSettings`가 JPA 엔티티가 아니므로 테스트 실패 확인

## GREEN 구현 최소 전략

- `ShopSettings`를 JPA 엔티티로 매핑
- `id` PK와 `shop_id` 유니크 제약을 반영
- `ShopSettingsRepository`에 `findByShopId(Long shopId)` 메서드 추가
- 테스트가 기대하는 필드 일치만 통과시키는 최소 구현만 적용

## 리스크

- 이후 단계(`2-3`)에서 간격 변경 메서드 추가 시 엔티티 불변성/검증 정책이 바뀔 수 있음
- 현재 단계에서는 저장/조회 계약만 우선 보장

---

## 대상 단계 (업데이트 - 7-2)

- `04-tdd/plan.md`의 `7-2. ShopSettingsReader 조회 — 존재`

## RED에서 검증할 것 (7-2)

- `ShopSettingsReader.readByShopId(shopId)`가 저장된 설정을 반환하는지 테스트로 검증
- 현재 코드베이스에는 `ShopSettingsReader`가 없어 컴파일/테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (7-2)

- `ShopSettingsReader` 클래스 추가
- `ShopSettingsRepository.findByShopId(shopId)`를 사용해 조회
- 존재 케이스 통과에 필요한 최소 구현만 반영

## 다음 단계 메모

- `7-3`에서 미존재 케이스 예외(`ShopSettingsNotFoundException`)를 추가 검증 예정

---

## 대상 단계 (업데이트 - 7-3)

- `04-tdd/plan.md`의 `7-3. ShopSettingsReader 조회 — 미존재 시 예외`

## RED에서 검증할 것 (7-3)

- 존재하지 않는 shopId 조회 시 `ShopSettingsNotFoundException`이 발생하는지 테스트로 검증
- 현재 구현(`orElseThrow()`)은 `NoSuchElementException`이라 테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (7-3)

- `ShopSettingsNotFoundException` 추가
- `ShopSettingsReader.readByShopId()`에서 `orElseThrow(ShopSettingsNotFoundException::new)`로 교체
- 예외 케이스 통과에 필요한 최소 변경만 반영

---

## 대상 단계 (업데이트 - 8-1)

- `04-tdd/plan.md`의 `8-1. ShopOperatingTimeWriter replaceAll — 기존 삭제 후 새로 저장`

## RED에서 검증할 것 (8-1)

- 기존 row가 있는 상태에서 `replaceAll(shopId, newRows)` 호출 시 기존 데이터가 삭제되고 새 데이터만 남는지 검증
- 현재 구현은 `UnsupportedOperationException`이라 테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (8-1)

- `shopOperatingTimeRepository.deleteByShopId(shopId)` 수행
- `shopOperatingTimeRepository.saveAllAndFlush(shopOperatingTimes)`로 신규 목록 저장
- 메서드는 저장된 목록을 반환

---

## 대상 단계 (업데이트 - 8-2)

- `04-tdd/plan.md`의 `8-2. ShopOperatingTimeReader shopId + dayOfWeek 조회`

## RED에서 검증할 것 (8-2)

- `ShopOperatingTimeReader.readByShopIdAndDayOfWeek(shopId, dayOfWeek)`가 해당 요일 운영시간 목록을 반환하는지 검증
- 현재 `ShopOperatingTimeReader` 미구현 상태라 컴파일/테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (8-2)

- `ShopOperatingTimeReader` 클래스 추가
- `shopOperatingTimeRepository.findByShopIdAndDayOfWeek(shopId, dayOfWeek)` 위임 구현

---

## 대상 단계 (업데이트 - 9-1)

- `04-tdd/plan.md`의 `9-1. StaffOperatingTimeWriter 저장`

## RED에서 검증할 것 (9-1)

- `StaffOperatingTimeWriter.save(staffOperatingTime)` 호출 시 엔티티가 저장되는지 검증
- 현재 `StaffOperatingTimeWriter` 미구현 상태라 컴파일/테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (9-1)

- `StaffOperatingTimeWriter` 클래스 추가
- `StaffOperatingTimeRepository.save(staffOperatingTime)`를 반환하는 최소 구현 적용

---

## 대상 단계 (업데이트 - 9-2)

- `04-tdd/plan.md`의 `9-2. StaffOperatingTimeReader staffId + dayOfWeek 조회 — 존재`

## RED에서 검증할 것 (9-2)

- `StaffOperatingTimeReader.findByStaffIdAndDayOfWeek(staffId, dayOfWeek)`가 저장된 오버라이드를 반환하는지 검증
- 현재 `StaffOperatingTimeReader` 미구현 상태라 컴파일/테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (9-2)

- `StaffOperatingTimeReader` 클래스 추가
- `staffOperatingTimeRepository.findByStaffIdAndDayOfWeek(...)` 위임 구현

---

## 대상 단계 (업데이트 - 9-3)

- `04-tdd/plan.md`의 `9-3. StaffOperatingTimeReader staffId + dayOfWeek 조회 — 미존재`

## RED에서 검증할 것 (9-3)

- `StaffOperatingTimeReader.findByStaffIdAndDayOfWeek(staffId, dayOfWeek)`가 오버라이드 미존재 시 `Optional.empty()`를 반환하는지 검증
- 기존 테스트는 존재 케이스만 다루므로 미존재 계약을 명시적으로 확인하기 위해 테스트 추가

## GREEN 구현 최소 전략 (9-3)

- Reader 구현은 이미 Optional 반환 계약을 따르므로 테스트만 추가
- 미존재 케이스가 통과하면 구현 변경 없이 GREEN 처리

---

## 대상 단계 (업데이트 - 10)

- `04-tdd/plan.md`의 `10. HolidayChecker (휴무일 판정 서비스)`

## RED에서 검증할 것 (10)

- WEEKLY/BIWEEKLY/MONTHLY/CUSTOM 및 공휴일 on/off 시나리오 8개를 `HolidayChecker.isHoliday(shopId, date)`로 검증
- 현재 `HolidayChecker` 미구현 상태라 컴파일/테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (10)

- `HolidayChecker` 클래스 추가
- `shop_holidays` 조회 후 타입별 판정 로직 구현
- `publicHolidayOff=true`일 때만 `public_holidays`를 조회해 공휴일 휴무 판정


---

## 대상 단계 (업데이트 - 11)

- `04-tdd/plan.md`의 `11. OperatingTimeResolver (운영 시간 결정 서비스)`

## RED에서 검증할 것 (11)

- `resolve(staffId, dayOfWeek)`에 대해 11-1~11-5 시나리오를 통합 테스트로 검증
- 현재 `OperatingTimeResolver` 미구현 상태라 컴파일/테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (11)

- `OperatingTimeResolver` 클래스 추가
- `staffId -> shopId` 조회 후 요일별 직원 오버라이드 우선 적용
- 오버라이드가 없으면 매장 기본 운영 시간 fallback 반환
- `isOff=true`면 빈 목록 반환

---

## 대상 단계 (업데이트 - 12)

- `04-tdd/plan.md`의 `12. SlotGenerator (슬롯 생성 유틸)`

## RED에서 검증할 것 (12)

- 단일/복수 블록 및 30/60분 간격 시 마지막 슬롯이 `endTime`까지 포함되는지 검증
- 현재 `SlotGenerator` 미구현 상태라 컴파일/테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (12)

- `SlotGenerator` 클래스 추가
- 운영 시간 블록을 시작시간 기준 정렬 후 interval 단위 슬롯 생성
- `endTime` inclusive 규칙으로 마지막 슬롯 포함


---

## 대상 단계 (업데이트 - 13-1)

- `04-tdd/plan.md`의 `13-1. 기본 조회 — 운영 시간 내 슬롯 반환`

## RED에서 검증할 것 (13-1)

- `AvailabilityService.getAvailableSlots(staffId, date)`가 운영 시간/간격 기준 슬롯을 반환하는지 검증
- 현재 `AvailabilityService` 미구현 상태라 컴파일/테스트 실패(RED) 확인

## GREEN 구현 최소 전략 (13-1)

- `AvailabilityService` 클래스 추가
- `OperatingTimeResolver`로 블록 조회 -> `SlotGenerator`로 슬롯 생성
- `ShopSettings`의 `intervalMinutes`를 적용해 슬롯 반환

