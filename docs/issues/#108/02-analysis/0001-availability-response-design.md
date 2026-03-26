# 이슈 108 설계 문서

## 주제

예약 화면에서 시간 간격(`intervalMinutes`)과 예약 가능 슬롯을 어떤 API 경계에서 제공할지 정리한다.

## 결론

- 프론트가 `ShopScheduleController`의 `intervalMinutes`를 별도로 받아 예약 가능/불가를 직접 계산하는 구조보다,
  서버가 `AvailabilityController.getAvailability()`에서 최종 판단을 내려주고 프론트는 그 결과를 그대로 사용하는 구조가 더 적절하다.
- 다만 응답을 더 자기완결적으로 만들기 위해 `AvailabilitySlotsResponse`에 `intervalMinutes`를 함께 포함하는 것은 권장한다.
- 즉 `intervalMinutes`는 프론트의 비즈니스 계산 근거가 아니라, UI 표현 보조용 메타데이터로 제공한다.

## 현재 구조

### 관련 API

- 고객용 가용 슬롯 조회
  - `GET /api/v1/shops/{shopId}/staff/{staffId}/availability`
  - 구현 위치: `src/main/java/com/example/easybooking/availability/presentation/AvailabilityController.java`
- 샵 스케줄 설정 조회
  - `GET /api/v1/shops/{shopId}/schedule/settings`
  - 구현 위치: `src/main/java/com/example/easybooking/availability/presentation/ShopScheduleController.java`

### 현재 응답 특징

- `AvailabilitySlotsResponse`는 현재 `date`, `slots`, `holiday`만 제공한다.
- `intervalMinutes`는 `ShopScheduleSettingsResponse`를 통해 별도 API에서만 조회할 수 있다.

### 서버가 이미 책임지는 계산

`AvailabilityService.getAvailableSlots()`는 이미 아래 규칙을 조합해 최종 슬롯을 계산한다.

- `intervalMinutes`
- 예약 가능 기간(`bookingWindowDays`)
- 당일 최소 리드타임(`minBookingLeadMinutes`)
- 휴무일 여부
- 운영시간
- 예약 시간 블록 점유 여부

즉 현재도 `가용성 판단 책임`은 서버 쪽에 있다.

## 왜 프론트 계산 방식이 맞지 않는가

### 1. 책임이 잘못 분산된다

프론트가 `intervalMinutes`를 받아 예약 가능/불가를 직접 계산하기 시작하면,
백엔드가 이미 알고 있는 가용성 규칙을 프론트가 다시 구현하게 된다.

이 구조는 아래 문제를 만든다.

- 서버와 프론트가 같은 규칙을 중복 구현한다.
- 규칙 변경 시 수정 범위가 양쪽으로 퍼진다.
- 클라이언트별 계산 결과가 달라질 수 있다.

### 2. 현재 예약 API만으로는 정확한 복원이 어렵다

`ReservationService.getShopAvailability()`가 내려주는 `bookedTimes`는 예약 시작 시각 목록이다.
하지만 실제 점유는 `ReservationTimeBlock`과 `TimeBlockGenerator`를 통해 10분 블록 단위로 관리된다.

예를 들어 60분 예약 1건은 시작 시각 1개만으로 표현되더라도 실제로는 여러 블록을 점유한다.
따라서 프론트가 `bookedTimes + intervalMinutes`만으로 어떤 슬롯이 막혀야 하는지 정확히 복원하기 어렵다.

### 3. API의 축이 다르다

- `AvailabilityController.getAvailability()`는 `샵 + 스태프 + 날짜` 기준의 최종 가용 슬롯 API다.
- `ReservationService.getShopAvailability()`는 `샵 + 날짜` 기준의 예약 시간 조회 API다.

이 둘을 프론트에서 조합해 하나의 예약 가능성 규칙으로 재구성하면,
스태프 단위 가용성과 샵 단위 예약 정보를 혼합하는 설계가 된다.

## 객체 설계 기준과의 정합성

`study/study-object/project-guideline.md` 기준으로도 서버가 최종 판단을 가지는 쪽이 자연스럽다.

- `데이터보다 행동을 먼저 본다`
  - 프론트는 설정값을 받아 계산하기보다, 서버에 `예약 가능한 슬롯을 달라`고 요청하는 편이 맞다.
- `객체는 자기 일을 스스로 하게 만든다`
  - 가용성 규칙을 아는 쪽은 서버 도메인과 서비스 계층이다.
- `메시지가 객체를 결정하게 한다`
  - 이 유스케이스의 메시지는 `가용 슬롯 조회`이지 `설정값 전달 후 프론트 계산`이 아니다.
- `Controller`는 요청 파싱, 응답 변환, 서비스 호출까지만 책임진다.
  - 실제 규칙 계산은 서비스/도메인 협력에서 끝나야 한다.

## 변경 방향

### 권장안

- 예약 화면은 `AvailabilityController.getAvailability()`를 단일 진실 공급원으로 사용한다.
- 프론트는 `slots`를 그대로 선택 가능 시간으로 사용한다.
- `AvailabilitySlotsResponse`에 `intervalMinutes`를 추가해 화면 렌더링 보조 정보로 제공한다.

### 권장하지 않는 방식

- 프론트가 `GET /schedule/settings`와 예약 API 응답을 조합해 예약 가능 여부를 직접 계산하는 방식
- `intervalMinutes`를 비즈니스 판단의 주 근거로 사용하는 방식

## 대안 비교

### 대안 A. 프론트가 `schedule/settings`의 `intervalMinutes`를 받아 직접 계산

장점

- 단기적으로는 기존 API를 거의 건드리지 않을 수 있다.

단점

- 규칙 중복 구현
- 정확도 저하 가능성
- 변경 전파 범위 확대

판단

- 채택하지 않음

### 대안 B. 서버가 최종 `slots`만 내려주고 프론트는 그대로 사용

장점

- 책임이 가장 명확하다.
- 프론트 구현이 단순해진다.

단점

- 프론트가 UI 그리드 표시나 간격 배치용 메타데이터가 필요할 때 별도 정보가 부족할 수 있다.

판단

- 기본 방향으로 적절함

### 대안 C. 서버가 최종 `slots`를 내려주고 `intervalMinutes`도 함께 제공

장점

- 서버 책임을 유지하면서 응답이 자기완결적이 된다.
- 프론트가 별도 설정 API에 덜 의존한다.
- UI 표현과 배치에 필요한 최소 메타데이터를 함께 줄 수 있다.

단점

- 프론트가 이 값을 오용하지 않도록 팀 내 규칙이 필요하다.

판단

- 최종 추천안

## 코드 수정 예시

### DTO 예시

```java
@Getter
@Builder
public class AvailabilitySlotsResponse {

    private String date;
    private int intervalMinutes;
    private List<String> slots;
    private boolean holiday;

    public static AvailabilitySlotsResponse of(
            LocalDate date,
            int intervalMinutes,
            List<LocalTime> slots,
            boolean holiday
    ) {
        return AvailabilitySlotsResponse.builder()
                .date(date.toString())
                .intervalMinutes(intervalMinutes)
                .slots(slots.stream().map(slot -> slot.format(SLOT_FORMATTER)).toList())
                .holiday(holiday)
                .build();
    }
}
```

### 서비스 조립 방향 예시

```java
public AvailabilitySlotsResponse getDailyAvailability(Long shopId, Long staffId, LocalDate date) {
    validateStaffBelongsToShop(shopId, staffId);

    ShopSettings settings = shopSettingsRepository.findByShopId(shopId)
            .orElseThrow(ShopSettingsNotFoundException::new);

    List<LocalTime> slots = availabilityService.getAvailableSlots(staffId, date);
    boolean holiday = holidayChecker.isHoliday(shopId, date);

    return AvailabilitySlotsResponse.of(date, settings.getIntervalMinutes(), slots, holiday);
}
```

## 변경 후 예상 결과

### 동작 측면

- 예약 화면은 `availability` 응답 하나만으로 핵심 렌더링이 가능해진다.
- 프론트는 `slots`를 그대로 사용하므로 예약 가능 여부 판단이 단순해진다.
- `intervalMinutes`는 UI 표시와 배치 보조 정보로만 사용된다.

### 성능 측면

- 예약 화면 진입 시 별도 `settings` 호출 의존을 줄일 수 있다.
- 응답 필드 1개 추가 수준이라 서버 부하는 사실상 무시 가능하다.

### 영향 범위

- `AvailabilitySlotsResponse`
- `CustomerAvailabilityService.getDailyAvailability()`
- 해당 API를 소비하는 프론트 예약 화면

## 추가 정리 필요 사항

- 프론트가 이미 예약된 시간을 별도로 시각화해야 한다면,
  `intervalMinutes`만 추가할지 `occupiedSlots` 또는 `slotStatuses` 같은 구조까지 확장할지 별도 설계가 필요하다.
- 현재 예약 생성 시 서버가 실제 availability 규칙을 충분히 재검증하는지 별도 점검이 필요하다.
  프론트 계산에 의존하는 구조는 이 문제를 더 악화시킬 수 있다.
