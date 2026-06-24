# TDD Plan - #101 예약 가능 시간 관리

## 개요

이 문서는 #101 예약 가능 시간 관리 기능의 TDD 계획이다.

**핵심 방향:**

- 계산 기반 (slot 테이블 없음, 규칙만 저장)
- 운영 시간: 요일별 row, 복수 블록 가능
- 운영 시간의 `endTime`은 "마지막 예약 가능 슬롯 시간"(inclusive)
- 직원: 매장 기본 + 요일별 오버라이드
- 휴무일: 매장 레벨만 (매주/격주/매달/특정일 + 공휴일)

**의존:**

- `reservation_time_blocks` (이미 구현됨, #97)
- `Staff` 엔티티 (이미 구현됨)
- `Shop` 엔티티 (이미 구현됨)

---

## 테스트 체크리스트

### [x] 1. Flyway 마이그레이션

#### 1-1. V5 마이그레이션 — 테이블 생성 확인 [x]

```
Given: V5__shop_availability_settings.sql 작성됨
When: 애플리케이션 컨텍스트 로드
Then: shop_settings, shop_operating_times, staff_operating_times,
      shop_holidays, public_holidays 테이블 생성됨
```

---

### [x] 2. ShopSettings 엔티티 및 Repository

#### 2-1. ShopSettings 생성 — 기본값 [x]

```
Given: shopId=1L
When: ShopSettings.createDefault(shopId) 호출
Then: intervalMinutes=30, scheduleType=DAILY, bookingWindowDays=30,
      minBookingLeadMinutes=60, publicHolidayOff=false
```

#### 2-2. ShopSettings 저장 및 조회 [x]

```
Given: ShopSettings 엔티티 생성됨
When: repository.save() 후 findByShopId() 호출
Then: 저장된 엔티티 반환, 모든 필드 일치
```

#### 2-3. ShopSettings 간격 변경 [x]

```
Given: intervalMinutes=30인 ShopSettings
When: updateInterval(60) 호출
Then: intervalMinutes=60으로 변경됨
```

---

### [x] 3. ShopOperatingTime 엔티티 및 Repository

#### 3-1. ShopOperatingTime 생성 [x]

```
Given: shopId=1L, dayOfWeek=MONDAY, startTime=10:00, endTime=19:00
When: ShopOperatingTime.create() 호출
Then: 엔티티 생성됨, 필드 값 일치
```

#### 3-2. ShopOperatingTime 시간 검증 — start > end 거부 [x]

```
Given: startTime=19:00, endTime=10:00
When: ShopOperatingTime.create() 호출
Then: 예외 발생 (시작 시간이 종료 시간보다 늦음)
```

#### 3-3. ShopOperatingTime shopId로 목록 조회 [x]

```
Given: shopId=1L에 대해 월(2개 블록), 화(1개 블록) 저장됨
When: repository.findByShopId(1L) 호출
Then: 3개 row 반환
```

#### 3-4. ShopOperatingTime shopId + dayOfWeek 조회 [x]

```
Given: shopId=1L, MONDAY에 2개 블록 저장됨
When: repository.findByShopIdAndDayOfWeek(1L, MONDAY) 호출
Then: 2개 row 반환
```

#### 3-5. ShopOperatingTime shopId로 전체 삭제 [x]

```
Given: shopId=1L에 5개 row 저장됨
When: repository.deleteByShopId(1L) 호출
Then: 0개 남음
```

---

### [x] 4. StaffOperatingTime 엔티티 및 Repository

#### 4-1. StaffOperatingTime 생성 — 시간 오버라이드 [x]

```
Given: staffId=1L, dayOfWeek=MONDAY, isOff=false, startTime=10:00, endTime=17:00
When: StaffOperatingTime.create() 호출
Then: 엔티티 생성됨, isOff=false, 시간 설정됨
```

#### 4-2. StaffOperatingTime 생성 — 요일 off [x]

```
Given: staffId=1L, dayOfWeek=WEDNESDAY, isOff=true
When: StaffOperatingTime.createOff() 호출
Then: 엔티티 생성됨, isOff=true, startTime/endTime은 null
```

#### 4-3. StaffOperatingTime staffId + dayOfWeek 조회 [x]

```
Given: staffId=1L, MONDAY 오버라이드 저장됨
When: repository.findByStaffIdAndDayOfWeek(1L, MONDAY) 호출
Then: Optional에 값 존재
```

#### 4-4. StaffOperatingTime staffId + dayOfWeek 조회 — 없으면 빈 Optional [x]

```
Given: staffId=1L, MONDAY 오버라이드 없음
When: repository.findByStaffIdAndDayOfWeek(1L, MONDAY) 호출
Then: Optional.empty()
```

---

### [x] 5. ShopHoliday 엔티티 및 Repository

#### 5-1. ShopHoliday 생성 — WEEKLY [x]

```
Given: shopId=1L, holidayType=WEEKLY, dayOfWeek=SUNDAY
When: ShopHoliday.createWeekly() 호출
Then: 엔티티 생성됨, dayOfWeek=SUNDAY
```

#### 5-2. ShopHoliday 생성 — BIWEEKLY (기준날짜) [x]

```
Given: shopId=1L, holidayType=BIWEEKLY, dayOfWeek=SATURDAY, referenceDate=2026-02-14
When: ShopHoliday.createBiweekly() 호출
Then: 엔티티 생성됨, referenceDate 설정됨
```

#### 5-3. ShopHoliday 생성 — MONTHLY (N번째 주 + 요일) [x]

```
Given: shopId=1L, holidayType=MONTHLY, weekOfMonth=2, dayOfWeek=MONDAY
When: ShopHoliday.createMonthly() 호출
Then: 엔티티 생성됨, weekOfMonth=2, dayOfWeek=MONDAY
```

#### 5-4. ShopHoliday 생성 — CUSTOM (특정 날짜) [x]

```
Given: shopId=1L, holidayType=CUSTOM, specificDate=2026-03-15
When: ShopHoliday.createCustom() 호출
Then: 엔티티 생성됨, specificDate 설정됨
```

#### 5-5. ShopHoliday shopId로 목록 조회 [x]

```
Given: shopId=1L에 WEEKLY 1개, CUSTOM 2개 저장됨
When: repository.findByShopId(1L) 호출
Then: 3개 row 반환
```

---

### [x] 6. PublicHoliday 엔티티 및 Repository

#### 6-1. PublicHoliday 날짜 존재 확인 [x]

```
Given: 2026-01-01(신정) 저장됨
When: repository.existsByHolidayDate(2026-01-01) 호출
Then: true
```

#### 6-2. PublicHoliday 날짜 범위 조회 [x]

```
Given: 2026년 공휴일 여러 개 저장됨
When: repository.findByHolidayDateBetween(2026-02-01, 2026-02-28) 호출
Then: 해당 기간 공휴일만 반환
```

---

### [x] 7. ShopSettingsReader / Writer

#### 7-1. ShopSettingsWriter 저장 [x]

```
Given: ShopSettings 엔티티
When: writer.save() 호출
Then: 저장된 ShopSettings 반환
```

#### 7-2. ShopSettingsReader 조회 — 존재 [x]

```
Given: shopId=1L에 ShopSettings 저장됨
When: reader.readByShopId(1L) 호출
Then: ShopSettings 반환
```

#### 7-3. ShopSettingsReader 조회 — 미존재 시 예외 [x]

```
Given: shopId=999L에 ShopSettings 없음
When: reader.readByShopId(999L) 호출
Then: ShopSettingsNotFoundException 발생
```

---

### [x] 8. ShopOperatingTimeReader / Writer

#### 8-1. ShopOperatingTimeWriter replaceAll — 기존 삭제 후 새로 저장 [x]

```
Given: shopId=1L에 기존 3개 row 존재
When: writer.replaceAll(shopId, 새로운 목록 5개) 호출
Then: 기존 삭제, 5개 저장됨
```

#### 8-2. ShopOperatingTimeReader shopId + dayOfWeek 조회 [x]

```
Given: shopId=1L, MONDAY에 2개 블록 저장됨
When: reader.readByShopIdAndDayOfWeek(1L, MONDAY) 호출
Then: 2개 반환
```

---

### [x] 9. StaffOperatingTimeReader / Writer

#### 9-1. StaffOperatingTimeWriter 저장 [x]

```
Given: StaffOperatingTime 엔티티
When: writer.save() 호출
Then: 저장됨
```

#### 9-2. StaffOperatingTimeReader staffId + dayOfWeek 조회 — 존재 [x]

```
Given: staffId=1L, MONDAY 오버라이드 저장됨
When: reader.findByStaffIdAndDayOfWeek(1L, MONDAY) 호출
Then: Optional에 값 존재
```

#### 9-3. StaffOperatingTimeReader staffId + dayOfWeek 조회 — 미존재 (fallback 필요) [x]

```
Given: staffId=1L, TUESDAY 오버라이드 없음
When: reader.findByStaffIdAndDayOfWeek(1L, TUESDAY) 호출
Then: Optional.empty() (호출자가 매장 기본으로 fallback)
```

---

### [x] 10. HolidayChecker (휴무일 판정 서비스)

#### 10-1. WEEKLY 휴무 판정 [x]

```
Given: shopId=1L, WEEKLY SUNDAY 휴무 설정됨
When: holidayChecker.isHoliday(shopId, 2026-02-15(일)) 호출
Then: true
```

#### 10-2. WEEKLY 비휴무 판정 [x]

```
Given: shopId=1L, WEEKLY SUNDAY 휴무 설정됨
When: holidayChecker.isHoliday(shopId, 2026-02-16(월)) 호출
Then: false
```

#### 10-3. BIWEEKLY 휴무 판정 — 쉬는 주 [x]

```
Given: shopId=1L, BIWEEKLY SATURDAY, referenceDate=2026-02-14(토)
When: holidayChecker.isHoliday(shopId, 2026-02-28(토)) — 2주 후
Then: true
```

#### 10-4. BIWEEKLY 비휴무 판정 — 안 쉬는 주 [x]

```
Given: shopId=1L, BIWEEKLY SATURDAY, referenceDate=2026-02-14(토)
When: holidayChecker.isHoliday(shopId, 2026-02-21(토)) — 1주 후
Then: false
```
 
#### 10-5. MONTHLY 휴무 판정 [x]

```
Given: shopId=1L, MONTHLY weekOfMonth=2 MONDAY (매달 둘째 주 월요일)
When: holidayChecker.isHoliday(shopId, 2026-03-09(월, 3월 둘째 주))
Then: true
```

#### 10-6. CUSTOM 휴무 판정 [x]

```
Given: shopId=1L, CUSTOM specificDate=2026-03-15
When: holidayChecker.isHoliday(shopId, 2026-03-15) 호출
Then: true
```

#### 10-7. 공휴일 휴무 판정 — publicHolidayOff=true [x]

```
Given: shopId=1L, publicHolidayOff=true, public_holidays에 2026-03-01 저장됨
When: holidayChecker.isHoliday(shopId, 2026-03-01) 호출
Then: true
```

#### 10-8. 공휴일 비휴무 — publicHolidayOff=false [x]

```
Given: shopId=1L, publicHolidayOff=false, public_holidays에 2026-03-01 저장됨
When: holidayChecker.isHoliday(shopId, 2026-03-01) 호출
Then: false
```

---

### [x] 11. OperatingTimeResolver (운영 시간 결정 서비스)

#### 11-1. 매장 기본 시간 반환 — 직원 오버라이드 없음 [x]

```
Given: shopId=1L MONDAY 10:00-19:00, staffId=1L MONDAY 오버라이드 없음
When: resolver.resolve(staffId, MONDAY) 호출
Then: [(10:00, 19:00)] 반환
```

#### 11-2. 직원 오버라이드 시간 반환 [x]

```
Given: shopId=1L MONDAY 10:00-19:00, staffId=1L MONDAY 오버라이드 10:00-17:00
When: resolver.resolve(staffId, MONDAY) 호출
Then: [(10:00, 17:00)] 반환
```

#### 11-3. 직원 is_off=true — 빈 목록 [x]

```
Given: shopId=1L MONDAY 10:00-19:00, staffId=1L MONDAY isOff=true
When: resolver.resolve(staffId, MONDAY) 호출
Then: 빈 목록 반환
```

#### 11-4. 매장에 해당 요일 없음 — 빈 목록 [x]

```
Given: shopId=1L SUNDAY 운영 시간 없음
When: resolver.resolve(staffId, SUNDAY) 호출
Then: 빈 목록 반환
```

#### 11-5. 매장 복수 블록 (휴게시간) 반환 [x]

```
Given: shopId=1L MONDAY 10:00-13:00, 14:00-19:00
When: resolver.resolve(staffId, MONDAY) 호출 (오버라이드 없음)
Then: [(10:00, 13:00), (14:00, 19:00)] 반환
```

---

### [x] 12. SlotGenerator (슬롯 생성 유틸)

운영시간 블록의 `endTime`은 "마지막으로 예약을 받는 시간"이며, 마지막 슬롯으로 포함된다.

#### 12-1. 단일 블록 — 30분 간격 [x]

```
Given: timeBlocks=[(09:00, 18:00)], interval=30
When: SlotGenerator.generate() 호출
Then: [09:00, 09:30, 10:00, ..., 18:00] — 19개
```

#### 12-2. 단일 블록 — 60분 간격 [x]

```
Given: timeBlocks=[(09:00, 18:00)], interval=60
When: SlotGenerator.generate() 호출
Then: [09:00, 10:00, ..., 18:00] — 10개
```

#### 12-3. 복수 블록 (휴게시간) — 30분 간격 [x]

```
Given: timeBlocks=[(10:00, 13:00), (14:00, 19:00)], interval=30
When: SlotGenerator.generate() 호출
Then: [10:00, 10:30, 11:00, 11:30, 12:00, 12:30, 13:00, 14:00, 14:30, ..., 19:00] — 18개
```

#### 12-4. 빈 블록 — 빈 목록 [x]

```
Given: timeBlocks=[], interval=30
When: SlotGenerator.generate() 호출
Then: 빈 목록
```

---

### [x] 13. AvailabilityService (예약 가능 시간 조회 핵심)

#### 13-1. 기본 조회 — 운영 시간 내 슬롯 반환 [x]

```
Given: shop 30분 간격, MONDAY 10:00-13:00, staff 오버라이드 없음, 점유 없음
When: service.getAvailableSlots(staffId, 2026-02-16(월)) 호출
Then: [10:00, 10:30, 11:00, 11:30, 12:00, 12:30, 13:00] 반환
```

#### 13-2. 점유 제거 — 확정된 예약 시간대 제외 [x]

```
Given: 위 설정 + reservation_time_blocks에 staffId=1L 10:30, 10:40, 10:50 점유
When: service.getAvailableSlots(staffId, 2026-02-16(월)) 호출
Then: [10:00, 11:00, 11:30, 12:00, 12:30, 13:00] — 10:30 제외
```

#### 13-3. 휴무일 — 빈 목록 [x]

```
Given: 위 설정 + 해당 날짜가 휴무일
When: service.getAvailableSlots(staffId, 휴무일) 호출
Then: 빈 목록
```

#### 13-4. booking_window 초과 — 예외 또는 빈 목록 [x]

```
Given: bookingWindowDays=30, 오늘=2026-02-12
When: service.getAvailableSlots(staffId, 2026-04-01) 호출
Then: 예외 발생 (예약 가능 기간 초과)
```

#### 13-5. 당일 예약 — min_booking_lead 적용 [x]

```
Given: 30분 간격, MONDAY 10:00-13:00, minBookingLeadMinutes=60, 현재 시각=10:30
When: service.getAvailableSlots(staffId, 오늘) 호출
Then: [11:30, 12:00, 12:30, 13:00] — 현재+60분(11:30) 이전 슬롯 제외
```

#### 13-6. 직원 오버라이드 반영 [x]

```
Given: shop MONDAY 10:00-19:00, staff MONDAY 오버라이드 10:00-15:00, 30분 간격
When: service.getAvailableSlots(staffId, 2026-02-16(월)) 호출
Then: [10:00, 10:30, ..., 15:00] — 15:00까지
```

#### 13-7. 직원 is_off 반영 [x]

```
Given: shop MONDAY 10:00-19:00, staff MONDAY isOff=true
When: service.getAvailableSlots(staffId, 2026-02-16(월)) 호출
Then: 빈 목록
```

#### 13-8. 월 단위 조회 — 예약 가능 날짜 계산 [x]

```
Given: yearMonth=2026-03, 직원/매장 설정 완료, 일부 날짜는 휴무/운영없음/점유없음
When: service.getAvailableDatesInMonth(staffId, YearMonth.of(2026, 3)) 호출
Then: 해당 월에서 실제 예약 가능한 날짜(dayOfMonth) 목록 반환
```

#### 13-9. 월 단위 조회 — booking_window 경계 적용 [x]

```
Given: bookingWindowDays=30, 오늘=2026-02-12
When: service.getAvailableDatesInMonth(staffId, YearMonth.of(2026, 4)) 호출
Then: 30일 초과 날짜는 월 결과에서 제외됨
```

#### 13-10. 월 단위 조회 — 당일 min_booking_lead 반영 [x]

```
Given: 오늘=2026-02-12, minBookingLeadMinutes=60, 오늘 남은 슬롯 없음
When: service.getAvailableDatesInMonth(staffId, YearMonth.of(2026, 2)) 호출
Then: 오늘 날짜는 availableDates에서 제외됨
```

#### 13-11. 월 단위 조회 — 월 경계/타임존 기준 일자 계산 [x]

```
Given: 서버 기준 타임존(Asia/Seoul), 조회 월=2026-03
When: 월 시작일(2026-03-01)과 월 마지막일(2026-03-31) 포함 범위로 조회
Then: 다른 월 날짜는 포함되지 않고, 3월 일자만 판정됨
```

---

### [x] 14. 점주용 설정 API — ShopScheduleController

운영 시간 설정 API의 `end`는 "마지막 예약 가능 슬롯 시간"(inclusive) 의미로 저장/조회한다.

#### 14-1. 매장 설정 조회 API [x]

```
GET /api/v1/shops/{shopId}/schedule/settings
Given: shopId=1L에 ShopSettings 저장됨
When: 점주 인증으로 API 호출
Then: 200 OK, 설정 정보 반환
```

#### 14-2. 예약 간격 변경 API [x]

```
PUT /api/v1/shops/{shopId}/schedule/settings
Given: shopId=1L, body={intervalMinutes: 60, bookingWindowDays: 30, ...}
When: 점주 인증으로 API 호출
Then: 200 OK, intervalMinutes=60으로 변경됨
```

#### 14-3. 운영 시간 설정 API — DAILY [x]

```
PUT /api/v1/shops/{shopId}/schedule/operating-times
Given: body={scheduleType: "DAILY", times: [{start: "10:00", end: "13:00"}, {start: "14:00", end: "19:00"}]}
When: 점주 인증으로 API 호출
Then: 200 OK, 월~일 7요일 x 2블록 = 14개 row 저장됨
```

#### 14-4. 운영 시간 설정 API — WEEKDAY_WEEKEND [x]

```
PUT /api/v1/shops/{shopId}/schedule/operating-times
Given: body={scheduleType: "WEEKDAY_WEEKEND",
  weekdayTimes: [{start: "10:00", end: "19:00"}],
  weekendTimes: [{start: "11:00", end: "17:00"}]}
When: 점주 인증으로 API 호출
Then: 200 OK, 월~금 5개 + 토~일 2개 = 7개 row 저장됨
```

#### 14-5. 운영 시간 설정 API — BY_DAY [x]

```
PUT /api/v1/shops/{shopId}/schedule/operating-times
Given: body={scheduleType: "BY_DAY",
  dayTimes: {MONDAY: [{start: "10:00", end: "19:00"}], TUESDAY: [{start: "10:00", end: "17:00"}], ...}}
When: 점주 인증으로 API 호출
Then: 200 OK, 입력된 요일별 row 저장됨
```

#### 14-6. 운영 시간 조회 API [x]

```
GET /api/v1/shops/{shopId}/schedule/operating-times
Given: shopId=1L에 운영 시간 저장됨
When: 점주 인증으로 API 호출
Then: 200 OK, scheduleType + 요일별 시간 목록 반환
```

#### 14-7. 권한 없는 접근 — 다른 shop 점주 [x]

```
Given: shopId=1L의 점주가 아닌 사용자
When: 설정 API 호출
Then: 403 Forbidden
```

---

### [x] 15. 직원 운영 시간 오버라이드 API

#### 15-1. 직원 운영 시간 오버라이드 설정 API [x]

```
PUT /api/v1/shops/{shopId}/staff/{staffId}/operating-times
Given: body={overrides: [{dayOfWeek: "MONDAY", isOff: false, start: "10:00", end: "17:00"},
                         {dayOfWeek: "WEDNESDAY", isOff: true}]}
When: 점주 인증으로 API 호출
Then: 200 OK, staffId에 MONDAY, WEDNESDAY 오버라이드 저장됨
```

#### 15-2. 직원 운영 시간 오버라이드 조회 API [x]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/operating-times
Given: staffId=1L에 오버라이드 2개 저장됨
When: 점주 인증으로 API 호출
Then: 200 OK, 오버라이드 목록 반환
```

#### 15-3. 매장 범위 초과 검증 — 거부 [x]

```
PUT /api/v1/shops/{shopId}/staff/{staffId}/operating-times
Given: shop MONDAY 10:00-19:00, body에 MONDAY 08:00-20:00 (매장 범위 초과)
When: API 호출
Then: 400 Bad Request
```

---

### [x] 16. 휴무일 설정 API

#### 16-1. 휴무일 목록 조회 API [x]

```
GET /api/v1/shops/{shopId}/schedule/holidays
Given: shopId=1L에 WEEKLY 1개, CUSTOM 2개 저장됨
When: 점주 인증으로 API 호출
Then: 200 OK, 3개 휴무일 반환
```

#### 16-2. 휴무일 추가 API — WEEKLY [x]

```
POST /api/v1/shops/{shopId}/schedule/holidays
Given: body={holidayType: "WEEKLY", dayOfWeek: "SUNDAY"}
When: 점주 인증으로 API 호출
Then: 201 Created, WEEKLY SUNDAY 저장됨
```

#### 16-3. 휴무일 추가 API — BIWEEKLY [x]

```
POST /api/v1/shops/{shopId}/schedule/holidays
Given: body={holidayType: "BIWEEKLY", dayOfWeek: "SATURDAY", referenceDate: "2026-02-14"}
When: 점주 인증으로 API 호출
Then: 201 Created, BIWEEKLY 저장됨
```

#### 16-4. 휴무일 추가 API — MONTHLY [x]

```
POST /api/v1/shops/{shopId}/schedule/holidays
Given: body={holidayType: "MONTHLY", weekOfMonth: 2, dayOfWeek: "MONDAY"}
When: 점주 인증으로 API 호출
Then: 201 Created, MONTHLY 저장됨
```

#### 16-5. 휴무일 추가 API — CUSTOM [x]

```
POST /api/v1/shops/{shopId}/schedule/holidays
Given: body={holidayType: "CUSTOM", specificDate: "2026-03-15"}
When: 점주 인증으로 API 호출
Then: 201 Created, CUSTOM 저장됨
```

#### 16-6. 휴무일 삭제 API [x]

```
DELETE /api/v1/shops/{shopId}/schedule/holidays/{holidayId}
Given: holidayId=1L 존재
When: 점주 인증으로 API 호출
Then: 204 No Content, 삭제됨
```

---

### [x] 17. 고객용 예약 가능 시간 조회 API

#### 17-1. 조회 성공 [x]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability?date=2026-02-16
Given: shop 설정 완료, staff 존재, 운영 시간 설정됨
When: API 호출 (인증 불필요 또는 TempUser)
Then: 200 OK, {date: "2026-02-16", slots: ["10:00", "10:30", ...]} 반환
```

#### 17-2. 존재하지 않는 staff [x]

```
Given: staffId=999L 존재하지 않음
When: API 호출
Then: 404 Not Found
```

#### 17-3. 휴무일 조회 [x]

```
Given: 해당 날짜가 휴무일
When: API 호출
Then: 200 OK, {date: "2026-02-15", slots: [], holiday: true}
```

#### 17-4. 예약 가능 기간 초과 조회 [x]

```
Given: bookingWindowDays=30, 요청 날짜가 31일 후
When: API 호출
Then: 400 Bad Request
```

#### 17-5. 월별 캘린더 조회 성공 [x]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly?yearMonth=2026-03
Given: shop 설정 완료, staff 존재, 월 내 일부 날짜만 예약 가능
When: API 호출
Then: 200 OK, {yearMonth: "2026-03", availableDates: [...], holidayDates: [...], closedDates: [...]} 반환
```

#### 17-6. 월별 캘린더 조회 — 존재하지 않는 staff [x]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly?yearMonth=2026-03
Given: staffId=999L 존재하지 않음
When: API 호출
Then: 404 Not Found
```

#### 17-7. 월별 캘린더 조회 — booking_window 경계 반영 [x]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly?yearMonth=2026-04
Given: bookingWindowDays=30, 오늘=2026-02-12
When: API 호출
Then: 200 OK, booking_window를 넘는 날짜는 availableDates에서 제외
```

#### 17-8. 월별 캘린더 조회 — 응답 스펙 검증 [x]

```
GET /api/v1/shops/{shopId}/staff/{staffId}/availability/monthly?yearMonth=2026-03
Given: 휴무일/운영없음/예약가능일이 혼재
When: API 호출
Then:
  - availableDates: 예약 가능한 dayOfMonth 목록
  - holidayDates: 휴무일 dayOfMonth 목록 (정기휴무 + 공휴일)
  - closedDates: 운영시간 없음/직원 off로 예약 불가한 dayOfMonth 목록
```

---

### [x] 18. 통합 테스트

#### 18-1. 전체 플로우 — 설정부터 조회까지 [x]

```
Given:
  1. shop, staff 생성
  2. 간격 30분 설정
  3. 운영 시간 DAILY 10:00-13:00, 14:00-18:00 설정
  4. WEEKLY SUNDAY 휴무 설정
When:
  1. 월요일 available-slots 조회
  2. 일요일 available-slots 조회
Then:
  1. 월요일: [10:00, 10:30, 11:00, 11:30, 12:00, 12:30, 13:00, 14:00, 14:30, ..., 18:00] 반환
  2. 일요일: 빈 목록 (휴무)
```

#### 18-2. 예약 확정 후 가용 시간 변화 [x]

```
Given:
  1. 위 설정
  2. staffId=1L, 2026-02-16(월) 10:00에 예약 생성
  3. 예약 확정 (duration=60분) -> reservation_time_blocks 생성
When: available-slots 조회
Then: 10:00, 10:30은 목록에 없음
```

---

## 구현 순서

1. **Database Layer**: V5 Flyway 마이그레이션
2. **Domain Layer**: 엔티티 + Enum (ShopSettings, ShopOperatingTime, StaffOperatingTime, ShopHoliday, PublicHoliday)
3. **Repository Layer**: JPA Repository
4. **Reader/Writer Layer**: Reader/Writer 패턴
5. **Service Layer**: HolidayChecker, OperatingTimeResolver, SlotGenerator, AvailabilityService
6. **Controller Layer**: ShopScheduleController (점주용), AvailabilityController (고객용)

---

## 현재 상태

승인됨. `18` GREEN 완료, plan.md 전 항목 완료.
