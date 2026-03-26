# Worklog - #101

## 2026-02-12

### go 요청 처리

- 이슈 번호 `#101` 확인
- 다음 미체크 항목 확인: `2-2. ShopSettings 저장 및 조회`
- RED 단계 수행 준비: 저장/조회 리포지토리 테스트 추가 예정

### RED 실행 결과 (`2-2`)

- 추가 파일:
  - `src/main/java/com/example/easybooking/availability/repository/ShopSettingsRepository.java`
  - `src/test/java/com/example/easybooking/availability/repository/ShopSettingsRepositoryTest.java`
- 임시 컴파일 보강:
  - `ShopSettings`에 `id` 필드/getter 추가 (아직 JPA 엔티티 미적용)
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.repository.ShopSettingsRepositoryTest"`
- 결과:
  - 테스트 1건 실패(RED 확인)
  - `BeanCreationException`/`IllegalArgumentException`로 컨텍스트 로딩 실패
  - 원인: `ShopSettings`가 JPA 엔티티로 관리되지 않는 상태

### GREEN 실행 결과 (`2-2`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/domain/ShopSettings.java`
- 적용 내용:
  - `@Entity`, `@Table(name = "shop_settings")` 추가
  - `id`에 `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)` 적용
  - 모든 필드에 DB 컬럼 매핑 추가
  - `scheduleType`에 `@Enumerated(EnumType.STRING)` 적용
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.repository.ShopSettingsRepositoryTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### GREEN 실행 결과 (`2-3`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/domain/ShopSettings.java`
  - `src/test/java/com/example/easybooking/availability/domain/ShopSettingsTest.java`
- 적용 내용:
  - `updateInterval(int intervalMinutes)` 메서드 추가
  - `updateInterval_updatesIntervalMinutes` 테스트 추가
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.domain.ShopSettingsTest" --tests "com.example.easybooking.availability.repository.ShopSettingsRepositoryTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### GREEN 실행 결과 (`3-1` ~ `3-5`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/domain/ShopOperatingTime.java`
  - `src/main/java/com/example/easybooking/availability/repository/ShopOperatingTimeRepository.java`
  - `src/test/java/com/example/easybooking/availability/domain/ShopOperatingTimeTest.java`
  - `src/test/java/com/example/easybooking/availability/repository/ShopOperatingTimeRepositoryTest.java`
- 적용 내용:
  - 생성/시간 검증 로직 및 `findByShopId`, `findByShopIdAndDayOfWeek`, `deleteByShopId` 리포지토리 메서드 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.domain.ShopOperatingTimeTest" --tests "com.example.easybooking.availability.repository.ShopOperatingTimeRepositoryTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### GREEN 실행 결과 (`4-1` ~ `4-4`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/domain/StaffOperatingTime.java`
  - `src/main/java/com/example/easybooking/availability/repository/StaffOperatingTimeRepository.java`
  - `src/test/java/com/example/easybooking/availability/domain/StaffOperatingTimeTest.java`
  - `src/test/java/com/example/easybooking/availability/repository/StaffOperatingTimeRepositoryTest.java`
- 적용 내용:
  - 시간 오버라이드/요일 off 생성 로직 및 `findByStaffIdAndDayOfWeek` 조회 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.domain.StaffOperatingTimeTest" --tests "com.example.easybooking.availability.repository.StaffOperatingTimeRepositoryTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### GREEN 실행 결과 (`5-1` ~ `5-5`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/domain/HolidayType.java`
  - `src/main/java/com/example/easybooking/availability/domain/ShopHoliday.java`
  - `src/main/java/com/example/easybooking/availability/repository/ShopHolidayRepository.java`
  - `src/test/java/com/example/easybooking/availability/domain/ShopHolidayTest.java`
  - `src/test/java/com/example/easybooking/availability/repository/ShopHolidayRepositoryTest.java`
- 적용 내용:
  - WEEKLY/BIWEEKLY/MONTHLY/CUSTOM 생성 로직 및 `findByShopId` 조회 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.domain.ShopHolidayTest" --tests "com.example.easybooking.availability.repository.ShopHolidayRepositoryTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### GREEN 실행 결과 (`6-1` ~ `6-2`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/domain/PublicHoliday.java`
  - `src/main/java/com/example/easybooking/availability/repository/PublicHolidayRepository.java`
  - `src/test/java/com/example/easybooking/availability/repository/PublicHolidayRepositoryTest.java`
- 적용 내용:
  - 날짜 존재 여부/범위 조회 리포지토리 메서드 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.repository.PublicHolidayRepositoryTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### 7-1 산출물 점검 및 7-2 준비

- 점검 파일:
  - src/main/java/com/example/easybooking/availability/ShopSettingsWriter.java
  - src/test/java/com/example/easybooking/availability/ShopSettingsWriterTest.java
  - .gitignore
- 점검 결과:
  - ShopSettingsWriter/테스트는 7-1 목적(저장 동작 검증)에 부합
  - .gitignore에 docs/agent/log/temp 계열 제외가 반영되어 비코드 아티팩트 노출 리스크 감소
- 다음 실행 단위:
  - 7-2. ShopSettingsReader 조회 — 존재 RED부터 진행

### RED 실행 결과 (`7-2`)

- 추가 파일:
  - src/test/java/com/example/easybooking/availability/ShopSettingsReaderTest.java
- 실행 명령:
  - ./gradlew test --tests "com.example.easybooking.availability.ShopSettingsReaderTest"
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: ShopSettingsReader 클래스 미구현

### GREEN 실행 결과 (`7-2`)

- 구현 파일:
  - src/main/java/com/example/easybooking/availability/ShopSettingsReader.java
- 적용 내용:
  - readByShopId(Long shopId) 추가
  - ShopSettingsRepository.findByShopId(shopId) 기반 조회 구현
- 실행 명령:
  - ./gradlew test --tests "com.example.easybooking.availability.ShopSettingsReaderTest"
- 결과:
  - 테스트 통과(GREEN 확인)

### 7-3 준비

- 다음 실행 단위: `7-3. ShopSettingsReader 조회 — 미존재 시 예외`
- RED 목표: 미존재 조회 시 도메인 예외(`ShopSettingsNotFoundException`) 강제

### RED 실행 결과 (`7-3`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/ShopSettingsReaderTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.ShopSettingsReaderTest"`
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: `ShopSettingsNotFoundException` 미구현

### GREEN 실행 결과 (`7-3`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/ShopSettingsReader.java`
  - `src/main/java/com/example/easybooking/availability/exception/ShopSettingsNotFoundException.java`
  - `src/main/java/com/example/easybooking/errors/errorcode/AvailabilityErrorCode.java`
  - `src/main/java/com/example/easybooking/errors/exception/AvailabilityException.java`
- 적용 내용:
  - `ShopSettingsReader.readByShopId()`에서 미존재 시 `ShopSettingsNotFoundException` 발생
  - Availability 전용 에러 코드/예외 계층 최소 도입
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.ShopSettingsReaderTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### 8-1 준비

- 다음 실행 단위: `8-1. ShopOperatingTimeWriter replaceAll — 기존 삭제 후 새로 저장`
- RED 확인 상태: `UnsupportedOperationException` 발생

### RED 실행 결과 (`8-1`)

- 확인 파일:
  - `src/main/java/com/example/easybooking/availability/ShopOperatingTimeWriter.java`
  - `src/test/java/com/example/easybooking/availability/ShopOperatingTimeWriterTest.java`
- 확인 내용:
  - `replaceAll()`이 `UnsupportedOperationException("TODO")` 상태
  - RED 실패 원인과 테스트 의도 일치

### GREEN 실행 결과 (`8-1`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/ShopOperatingTimeWriter.java`
- 적용 내용:
  - `deleteByShopId(shopId)` 수행 후 `saveAllAndFlush(shopOperatingTimes)`로 교체 저장
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.ShopOperatingTimeWriterTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### 8-2 준비

- 다음 실행 단위: `8-2. ShopOperatingTimeReader shopId + dayOfWeek 조회`
- RED 목표: Reader 미구현 상태에서 테스트 실패 확인

### RED 실행 결과 (`8-2`)

- 추가 파일:
  - `src/test/java/com/example/easybooking/availability/ShopOperatingTimeReaderTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.ShopOperatingTimeReaderTest"`
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: `ShopOperatingTimeReader` 클래스 미구현

### GREEN 실행 결과 (`8-2`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/ShopOperatingTimeReader.java`
- 적용 내용:
  - `readByShopIdAndDayOfWeek(shopId, dayOfWeek)` 메서드 추가
  - `shopOperatingTimeRepository.findByShopIdAndDayOfWeek(...)` 위임 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.ShopOperatingTimeReaderTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### 정책 변경 반영 (운영 시간 endTime 의미)

- 변경 요청:
  - 운영 시간 입력의 `endTime`을 "점주가 마지막으로 예약을 받는 시간"으로 사용
  - 마지막 슬롯이 `endTime`과 동일하게 생성되도록(inclusive) 문서 정책 수정
- 반영 문서:
  - `docs/issues/#101/04-tdd/plan.md` (12, 13, 14, 18 시나리오 기대값/정책 문구 수정)
  - `docs/issues/#101/02-analysis/analysis-options.md` (`end_time` 의미 및 슬롯 생성 규칙 보강)
  - `docs/issues/#101/03-adr/adr-101-availability.md` (결정사항 보강)
  - `docs/issues/#101/03-adr/discussion-log.md` (논의 12 추가, 합의 표 업데이트)

### 9-1 준비

- 다음 실행 단위: `9-1. StaffOperatingTimeWriter 저장`
- RED 목표: Writer 미구현 상태에서 테스트 실패 확인

### RED 실행 결과 (`9-1`)

- 추가 파일:
  - `src/test/java/com/example/easybooking/availability/StaffOperatingTimeWriterTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.StaffOperatingTimeWriterTest"`
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: `StaffOperatingTimeWriter` 클래스 미구현

### GREEN 실행 결과 (`9-1`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/StaffOperatingTimeWriter.java`
- 적용 내용:
  - `save(staffOperatingTime)` 메서드 추가
  - `StaffOperatingTimeRepository.save(...)` 위임 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.StaffOperatingTimeWriterTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### 정책 변경 코드 반영 (운영 시간 endTime inclusive)

- 변경 파일:
  - `src/main/java/com/example/easybooking/availability/domain/ShopOperatingTime.java`
  - `src/main/java/com/example/easybooking/availability/domain/StaffOperatingTime.java`
  - `src/test/java/com/example/easybooking/availability/domain/ShopOperatingTimeTest.java`
  - `src/test/java/com/example/easybooking/availability/domain/StaffOperatingTimeTest.java`
- 적용 내용:
  - 시간 범위 검증을 `startTime > endTime`일 때만 거부하도록 수정
  - `startTime == endTime` 허용 테스트 추가
- 검증:
  - `./gradlew test --tests "com.example.easybooking.availability.domain.ShopOperatingTimeTest" --tests "com.example.easybooking.availability.domain.StaffOperatingTimeTest"`
  - `./gradlew test --tests "com.example.easybooking.availability.*" --tests "com.example.easybooking.availability.domain.*" --tests "com.example.easybooking.availability.repository.*"`

### 9-2 준비

- 다음 실행 단위: `9-2. StaffOperatingTimeReader staffId + dayOfWeek 조회 — 존재`
- RED 목표: Reader 미구현 상태에서 테스트 실패 확인

### RED 실행 결과 (`9-2`)

- 추가 파일:
  - `src/test/java/com/example/easybooking/availability/StaffOperatingTimeReaderTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.StaffOperatingTimeReaderTest"`
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: `StaffOperatingTimeReader` 클래스 미구현

### GREEN 실행 결과 (`9-2`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/StaffOperatingTimeReader.java`
- 적용 내용:
  - `findByStaffIdAndDayOfWeek(staffId, dayOfWeek)` 메서드 추가
  - `StaffOperatingTimeRepository.findByStaffIdAndDayOfWeek(...)` 위임 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.StaffOperatingTimeReaderTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### 9-3 준비

- 다음 실행 단위: `9-3. StaffOperatingTimeReader staffId + dayOfWeek 조회 — 미존재 (fallback 필요)`
- RED 목표: 미존재 시 `Optional.empty()` 계약을 테스트로 확인

### 10 준비

- 다음 실행 단위: `10. HolidayChecker (휴무일 판정 서비스)`
- RED 목표: `HolidayChecker` 미구현 상태에서 8개 휴무 판정 테스트 실패 확인

### GREEN 실행 결과 (`9-3`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/StaffOperatingTimeReaderTest.java`
- 적용 내용:
  - 미존재 조회 시 `Optional.empty()`를 반환하는 테스트 추가
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.StaffOperatingTimeReaderTest"`
- 결과:
  - 테스트 통과(GREEN 확인, 구현 변경 불필요)

### RED 실행 결과 (`10`)

- 추가 파일:
  - `src/test/java/com/example/easybooking/availability/HolidayCheckerTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.HolidayCheckerTest"`
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: `HolidayChecker` 클래스 미구현

### GREEN 실행 결과 (`10`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/HolidayChecker.java`
- 적용 내용:
  - `isHoliday(shopId, date)` 구현
  - WEEKLY/BIWEEKLY/MONTHLY/CUSTOM 및 공휴일 on/off 판정 로직 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.HolidayCheckerTest"`
- 결과:
  - 테스트 통과(GREEN 확인)


### 11 준비

- 다음 실행 단위: `11. OperatingTimeResolver (운영 시간 결정 서비스)`
- RED 목표: `OperatingTimeResolver` 미구현 상태에서 11-1~11-5 테스트 실패 확인

### RED 실행 결과 (`11`)

- 추가 파일:
  - `src/test/java/com/example/easybooking/availability/OperatingTimeResolverTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.OperatingTimeResolverTest"`
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: `OperatingTimeResolver` 클래스 미구현

### GREEN 실행 결과 (`11`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/OperatingTimeResolver.java`
- 적용 내용:
  - `resolve(staffId, dayOfWeek)` 구현
  - 직원 오버라이드 우선, 없으면 매장 기본 운영 시간 fallback
  - `isOff=true`면 빈 목록 반환
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.OperatingTimeResolverTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### 12 준비

- 다음 실행 단위: `12. SlotGenerator (슬롯 생성 유틸)`
- RED 목표: `SlotGenerator` 미구현 상태에서 12-1~12-4 테스트 실패 확인

### RED 실행 결과 (`12`)

- 추가 파일:
  - `src/test/java/com/example/easybooking/availability/SlotGeneratorTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.SlotGeneratorTest"`
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: `SlotGenerator` 클래스 미구현

### GREEN 실행 결과 (`12`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/SlotGenerator.java`
- 적용 내용:
  - `generate(timeBlocks, intervalMinutes)` 구현
  - `endTime` inclusive 규칙으로 마지막 슬롯 포함
  - 단일/복수 블록, 빈 블록 처리
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.SlotGeneratorTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### 문서 보강 (월 단위 캘린더 조회 요구 반영)

- 변경 요청:
  - 월 단위 캘린더에서 예약 가능 날짜 표시를 위한 계획 보강
- 반영 내용:
  - `plan.md`에 월 단위 서비스/월별 API/응답 스펙/월 경계 규칙 테스트 항목 추가
  - `analysis-options.md`, `adr-101-availability.md`, `discussion-log.md`에 월 조회 정책/결정/합의 반영



### 13-1 준비

- 다음 실행 단위: `13-1. 기본 조회 — 운영 시간 내 슬롯 반환`
- RED 목표: `AvailabilityService` 미구현 상태에서 테스트 실패 확인

### RED 실행 결과 (`13-1`)

- 추가 파일:
  - `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest"`
- 결과:
  - 컴파일 실패(RED 확인)
  - 원인: `AvailabilityService` 클래스 미구현

### GREEN 실행 결과 (`13-1`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/AvailabilityService.java`
- 적용 내용:
  - `getAvailableSlots(staffId, date)` 구현
  - 휴무일이면 빈 목록 반환
  - `ShopSettings.intervalMinutes`와 운영 시간 블록을 사용해 슬롯 생성
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### RED 실행 결과 (`13-2`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableSlots_excludesOccupiedSlot_whenReservationTimeBlocksExist"`
- 결과:
  - 테스트 실패(RED 확인)
  - 원인: 점유 블록 필터링 미구현으로 `10:30` 슬롯이 제거되지 않음

### GREEN 실행 결과 (`13-2`)

- 구현 파일:
  - `src/main/java/com/example/easybooking/availability/AvailabilityService.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationTimeBlockRepository.java`
- 적용 내용:
  - 일자 기준 예약 점유 블록 조회 메서드 추가
  - 슬롯 범위(`[slot, slot+interval)`)에 점유 블록이 있으면 해당 슬롯 제외 로직 구현
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest"`
- 결과:
  - 테스트 통과(GREEN 확인)

### RED/GREEN 실행 결과 (`13-3`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableSlots_returnsEmpty_whenDateIsHoliday"`
- 결과:
  - 테스트 통과
  - 비고: 휴무일 빈 목록 로직이 이미 구현되어 있어 테스트 추가만으로 GREEN 확인

### RED/GREEN 실행 결과 (`13-4`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
  - `src/main/java/com/example/easybooking/availability/AvailabilityService.java`
  - `src/main/java/com/example/easybooking/availability/exception/BookingWindowExceededException.java`
  - `src/main/java/com/example/easybooking/errors/errorcode/AvailabilityErrorCode.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableSlots_throwsException_whenDateExceedsBookingWindow"`
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest"`
- 결과:
  - 테스트 통과
  - bookingWindowDays 초과/과거 날짜에 대해 `BookingWindowExceededException` 예외 발생 계약 반영

### RED/GREEN 실행 결과 (`13-5`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
  - `src/main/java/com/example/easybooking/availability/AvailabilityService.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableSlots_appliesMinBookingLead_forSameDay"`
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest"`
- 결과:
  - RED: 당일 리드타임 필터 미적용으로 테스트 실패
  - GREEN: `now + minBookingLeadMinutes` 이전 슬롯 제외 로직 추가 후 테스트 통과

### RED/GREEN 실행 결과 (`13-6`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableSlots_returnsStaffOverrideSlots_whenOverrideExists"`
- 결과:
  - 테스트 통과
  - 비고: 직원 오버라이드 우선 로직(`OperatingTimeResolver`)이 이미 반영되어 테스트 추가만으로 GREEN 확인

### RED/GREEN 실행 결과 (`13-7`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableSlots_returnsEmpty_whenStaffIsOff"`
- 결과:
  - 테스트 통과
  - 비고: `isOff=true` 빈 목록 반환 로직이 이미 반영되어 테스트 추가만으로 GREEN 확인

### RED/GREEN 실행 결과 (`13-8` ~ `13-11`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/AvailabilityServiceTest.java`
  - `src/main/java/com/example/easybooking/availability/AvailabilityService.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableDatesInMonth_returnsAvailableDayOfMonthList"`
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableDatesInMonth_excludesDatesOutsideBookingWindow"`
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableDatesInMonth_excludesToday_whenNoSlotsAfterMinLead"`
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest.getAvailableDatesInMonth_usesRequestedMonthBoundaryInAsiaSeoul"`
  - `./gradlew test --tests "com.example.easybooking.availability.AvailabilityServiceTest"`
- 결과:
  - RED: `getAvailableDatesInMonth` 미구현으로 컴파일 실패
  - GREEN: 월 단위 예약 가능 날짜 계산 구현 후 모든 월 단위 시나리오 테스트 통과

### RED/GREEN 실행 결과 (`14-1`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/presentation/ShopScheduleControllerIntegrationTest.java`
  - `src/main/java/com/example/easybooking/availability/presentation/ShopScheduleController.java`
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/ShopScheduleSettingsResponse.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.getScheduleSettings_returnsShopSettings_whenOwnerAuthenticated"`
- 결과:
  - RED: 컨트롤러 미구현으로 404 실패
  - GREEN: 점주 소유 검증 + 설정 조회 API 구현 후 테스트 통과

### RED/GREEN 실행 결과 (`14-2` ~ `14-7`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/presentation/ShopScheduleControllerIntegrationTest.java`
  - `src/main/java/com/example/easybooking/availability/presentation/ShopScheduleController.java`
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java`
  - `src/main/java/com/example/easybooking/availability/domain/ShopSettings.java`
  - `src/main/java/com/example/easybooking/availability/ShopOperatingTimeReader.java`
  - `src/main/java/com/example/easybooking/availability/dto/request/UpdateShopScheduleSettingsRequest.java`
  - `src/main/java/com/example/easybooking/availability/dto/request/ShopTimeRangeRequest.java`
  - `src/main/java/com/example/easybooking/availability/dto/request/UpdateShopOperatingTimesRequest.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/ShopOperatingTimesResponse.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/ShopTimeRangeResponse.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.updateScheduleSettings_updatesIntervalMinutes_whenOwnerAuthenticated"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.updateOperatingTimes_savesRowsForAllDays_whenScheduleTypeDaily"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.updateOperatingTimes_savesWeekdayWeekendRows_whenScheduleTypeWeekdayWeekend"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.updateOperatingTimes_savesRowsByDay_whenScheduleTypeByDay"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.getOperatingTimes_returnsScheduleTypeAndDayTimes_whenOwnerAuthenticated"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.scheduleApis_returnForbidden_whenUserIsNotShopOwner"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest"`
- 결과:
  - `14-2` RED(405) -> GREEN
  - `14-3` RED(404) -> GREEN
  - `14-4` GREEN (기구현 분기 로직으로 통과)
  - `14-5` GREEN (기구현 분기 로직으로 통과)
  - `14-6` RED(405) -> GREEN
  - `14-7` GREEN (소유자 불일치 403 확인)

### RED/GREEN 실행 결과 (`15-1` ~ `15-3`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/presentation/StaffOperatingTimesControllerIntegrationTest.java`
  - `src/main/java/com/example/easybooking/availability/presentation/StaffOperatingTimesController.java`
  - `src/main/java/com/example/easybooking/availability/StaffOperatingTimesService.java`
  - `src/main/java/com/example/easybooking/availability/repository/StaffOperatingTimeRepository.java`
  - `src/main/java/com/example/easybooking/availability/dto/request/UpdateStaffOperatingTimesRequest.java`
  - `src/main/java/com/example/easybooking/availability/dto/request/StaffOperatingTimeOverrideRequest.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/StaffOperatingTimesResponse.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/StaffOperatingTimeOverrideResponse.java`
  - `src/main/java/com/example/easybooking/errors/errorcode/AvailabilityErrorCode.java`
  - `src/main/java/com/example/easybooking/availability/exception/StaffOverrideOutOfShopRangeException.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.StaffOperatingTimesControllerIntegrationTest.putStaffOperatingTimes_savesOverrides_whenOwnerAuthenticated"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.StaffOperatingTimesControllerIntegrationTest.getStaffOperatingTimes_returnsOverrides_whenOwnerAuthenticated"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.StaffOperatingTimesControllerIntegrationTest.putStaffOperatingTimes_returnsBadRequest_whenOverrideExceedsShopOperatingRange"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.StaffOperatingTimesControllerIntegrationTest"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest" --tests "com.example.easybooking.availability.presentation.StaffOperatingTimesControllerIntegrationTest"`
- 결과:
  - `15-1` RED(404) -> GREEN
  - `15-2` RED(405) -> GREEN
  - `15-3` RED(200) -> GREEN(400)
  - 과정 이슈:
    - `isOff` 직렬화/역직렬화 키를 `@JsonProperty("isOff")`로 고정
    - 한 번의 Gradle 파일 잠금(`build/test-results/.../binary`)은 동일 명령 재실행으로 해결

### RED/GREEN 실행 결과 (`16-1`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/presentation/ShopScheduleControllerIntegrationTest.java`
  - `src/main/java/com/example/easybooking/availability/presentation/ShopScheduleController.java`
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/ShopHolidayResponse.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/ShopHolidaysResponse.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.getHolidays_returnsAllConfiguredHolidays_whenOwnerAuthenticated"`
- 결과:
  - RED: API 미구현으로 404 실패
  - GREEN: 점주 소유 검증 후 휴무일 목록 반환 API 구현, 테스트 통과

### RED/GREEN 실행 결과 (`16-2` ~ `16-6`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/presentation/ShopScheduleControllerIntegrationTest.java`
  - `src/main/java/com/example/easybooking/availability/presentation/ShopScheduleController.java`
  - `src/main/java/com/example/easybooking/availability/ShopScheduleService.java`
  - `src/main/java/com/example/easybooking/availability/repository/ShopHolidayRepository.java`
  - `src/main/java/com/example/easybooking/availability/dto/request/CreateShopHolidayRequest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.createHoliday_savesWeeklyHoliday_whenTypeIsWeekly" --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.createHoliday_savesBiweeklyHoliday_whenTypeIsBiweekly" --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.createHoliday_savesMonthlyHoliday_whenTypeIsMonthly" --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.createHoliday_savesCustomHoliday_whenTypeIsCustom" --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest.deleteHoliday_removesConfiguredHoliday_whenHolidayExists"`
- 결과:
  - RED: POST/DELETE 미구현으로 `405/404` 실패
  - GREEN: 휴무일 추가/삭제 API 구현 후 5개 테스트 통과

### RED/GREEN 실행 결과 (`17-1` ~ `17-8`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/presentation/AvailabilityControllerIntegrationTest.java`
  - `src/main/java/com/example/easybooking/availability/presentation/AvailabilityController.java`
  - `src/main/java/com/example/easybooking/availability/CustomerAvailabilityService.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/AvailabilitySlotsResponse.java`
  - `src/main/java/com/example/easybooking/availability/dto/response/AvailabilityMonthlyResponse.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.AvailabilityControllerIntegrationTest"`
- 결과:
  - RED: 라우트 미구현으로 전 케이스 404 실패
  - GREEN: 일별/월별 예약 가능 조회 API 구현 후 8개 테스트 통과

### RED/GREEN 실행 결과 (`18-1` ~ `18-2`)

- 변경 파일:
  - `src/test/java/com/example/easybooking/availability/presentation/AvailabilityControllerIntegrationTest.java`
- 실행 명령:
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.AvailabilityControllerIntegrationTest.fullFlow_returnsSlotsOnMondayAndEmptyOnSundayAfterScheduleSetup" --tests "com.example.easybooking.availability.presentation.AvailabilityControllerIntegrationTest.availabilityExcludesOccupiedTimeBlocks_afterReservationConfirmed"`
  - `./gradlew test --tests "com.example.easybooking.availability.presentation.ShopScheduleControllerIntegrationTest" --tests "com.example.easybooking.availability.presentation.StaffOperatingTimesControllerIntegrationTest" --tests "com.example.easybooking.availability.presentation.AvailabilityControllerIntegrationTest"`
- 결과:
  - `18-1` GREEN: 설정 API(간격/운영시간/휴무일) 이후 고객용 조회에서 월요일 슬롯·일요일 휴무 응답 확인
  - `18-2` GREEN: 예약 확정에 따른 `reservation_time_blocks` 반영 후 10:00/10:30 슬롯 제외 확인
