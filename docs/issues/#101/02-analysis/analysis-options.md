# 대안 분석 및 추천안 - #101 예약 가능 시간 관리

## 1. 문제 정의

고객이 특정 매장/직원의 예약 가능한 날짜와 시간을 조회할 수 있어야 하고, 점주가 운영 시간과 휴무일을 설정할 수 있어야 한다.

---

## 2. 핵심 결정: 슬롯 생성 방식

### 대안 A: 물리적 슬롯 (slot 테이블에 row 미리 생성)

- 운영 시간 설정 시 slot 테이블에 row를 미리 생성
- 고객 조회 시 해당 row를 읽어서 반환
- 단점: `reservation_time_blocks`와 이중 관리 리스크, 설정 변경 시 row 재생성 필요, 배치 작업 필요

### 대안 B: 계산 기반 (규칙만 저장, 조회 시 실시간 계산)

- 운영 시간/휴무일 규칙만 저장
- 고객 조회 시: 운영시간에서 슬롯 생성 -> 휴무일 체크 -> 점유 블록 제외 -> 반환
- 장점: 이중 소스 제거, 설정 변경 즉시 반영, 배치 불필요
- 단점: 특정 시간만 개별 차단하려면 별도 테이블 필요 (나중에 추가 가능)

**결정: 대안 B (계산 기반)** — `reservation_time_blocks`가 이미 점유의 SSOT이므로 물리적 슬롯은 이중 소스가 된다. 네일샵 규모에서 계산 비용은 무시할 수준.

---

## 3. 핵심 결정: 운영 시간 저장 구조

### 대안 A: 정규화된 테이블 (요일별 row)

- `shop_operating_times` 테이블에 요일별 start_time/end_time 저장
- 같은 요일에 복수 row 가능 (휴게시간)
- 장점: 데이터 무결성, 인덱스 효율, 기존 JPA 패턴과 일관성
- 단점: 구현 복잡도 약간 높음

### 대안 B: JSON 컬럼

- 단일 JSON에 요일별 시간대 저장
- 단점: 데이터 무결성 제약 어려움, 쿼리 성능

### 대안 C: 캘린더 기반 미리 생성

- 날짜별로 row 사전 생성
- 단점: 데이터 중복, 배치 필요

**결정: 대안 A (정규화된 테이블)** — 기존 codebase와 일관성, 데이터 무결성 보장.

---

## 4. 핵심 결정: 직원 운영 시간

### 대안 A: 전체 대체

- `staff_operating_times`에 row가 하나라도 있으면 직원 전체 스케줄을 해당 row로 대체
- 단점: 월요일만 다를 때도 월~일 전부 입력 필요, 매장 시간 변경이 직원에게 반영 안 됨

### 대안 B: 요일별 오버라이드

- 해당 요일 row가 있으면 직원 것 사용, 없으면 매장 기본 fallback
- `is_off` 플래그로 특정 요일 안 함 표현 가능
- 장점: 변경 요일만 입력, 매장 시간 변경이 미오버라이드 요일에 자동 반영

**결정: 대안 B (요일별 오버라이드)** — "매장 범위 안에서 조금씩 다르다"는 요구사항에 적합.

---

## 5. 최종 데이터 모델

### 5.1 `shop_settings` (매장 예약 설정)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `shop_id` | BIGINT, PK, FK | shops.id (1:1) |
| `interval_minutes` | INT | 30 or 60 |
| `schedule_type` | VARCHAR | DAILY / WEEKDAY_WEEKEND / BY_DAY (UI 렌더링 힌트) |
| `booking_window_days` | INT | 예약 가능 기간 (기본 30) |
| `min_booking_lead_minutes` | INT | 예약 마감 시간 (기본 60) |
| `public_holiday_off` | BOOLEAN | 공휴일 전체 휴무 여부 |

- `schedule_type`은 프론트 UI 힌트 + 요청 해석용. 저장은 항상 요일별 row.
- `schedule_type` 변경 시 기존 `shop_operating_times` 전부 삭제 후 교체.

### 5.2 `shop_operating_times` (매장 기본 운영 시간)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT, PK | |
| `shop_id` | BIGINT, FK | |
| `day_of_week` | VARCHAR | MONDAY ~ SUNDAY |
| `start_time` | TIME | |
| `end_time` | TIME | 마지막 예약 가능 슬롯 시각 (포함) |

- 같은 요일에 복수 row 가능 (휴게시간: 10:00-13:00, 14:00-19:00)
- 해당 요일 row 없으면 = 그날 안 함
- 슬롯 생성 시 `end_time`도 마지막 슬롯으로 포함 (inclusive)

### 5.3 `staff_operating_times` (직원별 요일 오버라이드)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT, PK | |
| `staff_id` | BIGINT, FK | |
| `day_of_week` | VARCHAR | MONDAY ~ SUNDAY |
| `is_off` | BOOLEAN | true면 해당 요일 안 함 |
| `start_time` | TIME | is_off=false일 때만 |
| `end_time` | TIME | is_off=false일 때만, 마지막 예약 가능 슬롯 시각 (포함) |

- row 없으면 -> 매장 기본 fallback
- row 있으면 -> is_off 체크 후 직원 시간 사용
- 매장 범위 내에서만 설정 가능 (앱에서 검증)

### 5.4 `shop_holidays` (매장 휴무일)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT, PK | |
| `shop_id` | BIGINT, FK | |
| `holiday_type` | VARCHAR | WEEKLY / BIWEEKLY / MONTHLY / CUSTOM |
| `day_of_week` | VARCHAR | WEEKLY/BIWEEKLY/MONTHLY용 |
| `week_of_month` | INT(1~5) | MONTHLY용 |
| `reference_date` | DATE | BIWEEKLY 기준 날짜 |
| `specific_date` | DATE | CUSTOM용 (특정 날짜) |

### 5.5 `public_holidays` (공휴일 정적 데이터)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT, PK | |
| `holiday_date` | DATE | |
| `name` | VARCHAR | 설날, 추석 등 |

---

## 6. 예약 가능 시간 조회 계산 흐름

```
입력: staff_id, date

1. staff -> shop_id 조회
2. shop_settings 조회 (interval, booking_window, lead_time, public_holiday_off)
3. date가 booking_window 안인지 확인
4. 휴무일 체크:
   a. shop_holidays 매칭 (WEEKLY/BIWEEKLY/MONTHLY/CUSTOM)
   b. public_holiday_off=true이면 -> public_holidays 테이블 확인
   -> 휴무일이면 빈 목록 반환
5. 운영 시간 결정 (해당 요일 기준):
   a. staff_operating_times에 해당 요일 row 있으면:
      - is_off=true -> 빈 목록 반환
      - is_off=false -> 직원 시간 사용
   b. 없으면 -> shop_operating_times에서 해당 요일 조회
   c. 매장에도 없으면 -> 빈 목록 반환
6. 운영 시간 블록에서 interval 단위로 슬롯 생성 (`end_time` 포함)
7. reservation_time_blocks에서 해당 staff/date 점유 제거
8. 오늘이면 min_booking_lead_minutes 적용
9. 반환
```

---

## 7. 월 단위 캘린더 조회 정책

월 캘린더 렌더링을 위해 "특정 날짜 슬롯 목록"과 별도로 "해당 월의 예약 가능 날짜 목록" 조회가 필요하다.

### 7.1 서비스 계약

- `AvailabilityService.getAvailableDatesInMonth(staffId, yearMonth)`
- 반환 대상: 해당 월의 `dayOfMonth` 단위 상태

### 7.2 응답 스펙(권장)

- `yearMonth`: `"2026-03"` 형식
- `availableDates`: 실제 예약 가능한 날짜(dayOfMonth) 목록
- `holidayDates`: 휴무일 날짜(dayOfMonth) 목록 (정기휴무/공휴일)
- `closedDates`: 운영시간 없음 또는 직원 off로 예약 불가한 날짜(dayOfMonth) 목록

### 7.3 월 경계 규칙

- 서버 기준 타임존(기본 Asia/Seoul)으로 월 시작일~월 마지막일을 판정
- `booking_window_days`를 초과하는 날짜는 결과에서 제외
- 당일은 `min_booking_lead_minutes` 적용 후 슬롯이 없으면 `availableDates`에서 제외

---

## 8. 기타 정책

| 정책 | 결정 |
|------|------|
| 직원 휴무 | 매장 레벨에서만 관리 (직원 개인 휴무 제외) |
| 예약 간격 | 매장 단위 통일 |
| 공휴일 데이터 | 정적 데이터로 시작, 필요시 API 연동 전환 |
