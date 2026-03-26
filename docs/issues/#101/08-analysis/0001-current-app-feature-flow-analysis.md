# #101 현재 앱 기능/플로우 상세 분석

## 0. 문서 목적
- 점주/고객 User Flow 관점에서 현재 구현 API가 어디까지 커버하는지 점검한다.
- 기존 `docs/issues/#101` 산출물을 한 번에 참조할 수 있도록 정리한다.
- 구현 갭을 기능 우선순위로 분류해 다음 개발 입력으로 사용한다.

기준 시점:
- 코드 기준: `feature/jiseob/#101` 브랜치
- 분석 범위: 예약 가능성(availability) + 예약/채팅 + 메뉴/태그 연계 플로우

---

## 1. 기존 문서 정리

| 구분 | 파일 | 역할 | 현재 유효성 |
|---|---|---|---|
| 문제 스캔 | `docs/issues/#101/01-scan/problem-scan.md` | 기존 구조/부재 기능 정의 | 유효 |
| 대안 분석 | `docs/issues/#101/02-analysis/analysis-options.md` | 계산 기반/테이블 구조/월 조회 정책 | 유효 |
| ADR | `docs/issues/#101/03-adr/adr-101-availability.md` | 아키텍처 결정 기록 | 유효(상태 문자열은 추후 갱신 가능) |
| TDD 계획 | `docs/issues/#101/04-tdd/plan.md` | 테스트/구현 순서 SSOT | 완료 반영됨 |
| Green 설계 | `docs/issues/#101/05-green/green-design.md` | 구현 전략/설계 메모 | 유효 |
| 작업 로그 | `docs/issues/#101/06-worklog/worklog.md` | RED/GREEN 이력 | 유효 |
| PR 초안 | `docs/issues/#101/07-pr/0001-pr-draft.md` | 변경 요약/리스크/테스트 | 유효 |

---

## 2. 현재 앱 기능 지도 (요약)

### 2.1 인증/접근
- 공개 허용 경로는 제한적이며 대부분 엔드포인트는 인증 필요.
- 보안 설정상 `allowUrls` 외에는 인증이 필요하다.
- 따라서 고객 예약 플로우도 기본적으로 로그인 사용자 기준이다.

참고:
- `src/main/java/com/example/easybooking/auth/SecurityConfig.java`

### 2.2 점주 예약 설정
- 매장 설정 조회/수정: `/api/v1/shops/{shopId}/schedule/settings`
- 운영시간 조회/수정: `/api/v1/shops/{shopId}/schedule/operating-times`
- 휴무일 조회/추가/삭제: `/api/v1/shops/{shopId}/schedule/holidays`

참고:
- `src/main/java/com/example/easybooking/availability/presentation/ShopScheduleController.java`

### 2.3 직원 예약 설정
- 직원 운영시간 오버라이드 조회/수정:
  - `/api/v1/shops/{shopId}/staff/{staffId}/operating-times`
- 요일별 `isOff` + 시간 오버라이드 지원.

참고:
- `src/main/java/com/example/easybooking/availability/presentation/StaffOperatingTimesController.java`

### 2.4 고객 가용 조회
- 일별 슬롯 조회:
  - `/api/v1/shops/{shopId}/staff/{staffId}/availability?date=...`
- 월별 캘린더 조회:
  - `/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly?yearMonth=YYYY-MM`

참고:
- `src/main/java/com/example/easybooking/availability/presentation/AvailabilityController.java`

### 2.5 예약/채팅 연계
- 예약 생성/확정/거절 API 존재.
- 예약 이벤트 발생 시 채팅방 시스템 메시지 발행(`reservationId` 포함).

참고:
- `src/main/java/com/example/easybooking/reservation/presentation/ReservationController.java`
- `src/main/java/com/example/easybooking/reservation/event/ReservationChatEventListener.java`

---

## 3. 요청 User Flow 대비 커버리지

상태 기준:
- 완전 커버: 현재 API/모델만으로 바로 구현 가능
- 부분 커버: 핵심은 가능하나 UX 요구를 직접 충족하려면 추가 구현 필요
- 미커버: 전용 API/모델 부재

### 3.1 점주 가입/설정 플로우

| 요구 단계 | 현재 상태 | 근거/비고 |
|---|---|---|
| 점주 회원가입 | 완전 커버 | `POST /user/owner/signup` |
| 매장 생성 | 완전 커버 | `POST /shop` |
| 운영시간 타입(매일/평일주말/요일별) | 완전 커버 | `scheduleType` + `times/weekdayTimes/weekendTimes/dayTimes` |
| 운영시간 복수 블록(휴게시간) | 완전 커버 | 요일당 다중 row 저장 |
| 끝시간=마지막 예약 슬롯 | 완전 커버 | 슬롯 생성 시 `endTime` inclusive |
| 간격 설정(30분/1시간) | 부분 커버 | `intervalMinutes` 설정은 가능하나 30/60 강제 검증 없음 |
| 정기 휴무(매주/격주/매달+n번째주) | 완전 커버 | `holidayType` + `dayOfWeek/weekOfMonth/referenceDate` |
| 휴무일 특정 날짜 추가 | 완전 커버 | `holidayType=CUSTOM`, `specificDate` |
| 공휴일 휴무 on/off | 부분 커버(실질 미적용) | DTO 필드는 있으나 settings 업데이트 로직이 `interval`만 반영 |
| 직원 추가(이름) | 미커버(전용 API) | 매장 생성 시 기본 staff 자동생성만 있음 |
| 직원 운영시간(매장 기본과 동일 체크) | 부분 커버 | 오버라이드 비우면 fallback 가능, 전용 `sameAsShop` 모델 없음 |
| 직원 정기휴무(매주/격주/매달) | 미커버 | 직원 휴무는 요일 `isOff`만 지원 |
| 직원 공휴일 휴무 on/off | 미커버 | 직원 단위 공휴일 정책 없음 |
| 직원 특정일 휴무 | 미커버 | 직원 단위 custom holiday 모델 없음 |
| 태그 추가 | 완전 커버 | 태그 생성/메뉴 태그 매핑 API 존재 |
| 메뉴 추가(이름/설명/순서) | 완전 커버 | 메뉴 생성/수정/조회/비활성화 API |
| 메뉴 카테고리(=태그) | 완전 커버 | 태그 기반 분류/필터 API로 동일 개념 처리 가능 |
| 메뉴 가격 | 미커버 | `ShopMenu`에 price 필드 없음 |
| 입력필드(텍스트/수량) | 부분~완전 커버 | `InputType.TEXT/NUMBER` 지원 |

### 3.2 고객 예약 플로우

| 요구 단계 | 현재 상태 | 근거/비고 |
|---|---|---|
| 채팅방 입장 후 예약 페이지 이동 | 완전 커버(인증 사용자) | `/chat/rooms/shop/{shopId}`, `/link/chat/{slugOrCode}` |
| 월 단위 캘린더 조회 | 완전 커버 | 월 API에서 `availableDates/holidayDates/closedDates` 제공 |
| 예약 불가/휴무/공휴일 색상 분리 | 부분 커버 | `holidayDates`에 정기휴무+공휴일이 함께 포함되어 공휴일 별도 색 분리 어려움 |
| 날짜 선택 후 시간 조회 | 완전 커버(가능 슬롯 기준) | 일 API가 가능한 슬롯 목록 반환 |
| 점유 슬롯 회색 표시 | 부분 커버 | 현재는 가능 슬롯만 반환, 점유 슬롯 목록 별도 제공 없음 |
| 예약 메뉴 조회(기본 태그 선택) | 부분 커버 | 태그 필터 조회 지원, “첫 태그 자동선택”은 프론트 정책 |
| 태그 선택 시 메뉴 필터 | 완전 커버 | `GET /api/shops/{shopId}/menus?tagIds=...` |
| 메뉴 선택 + 입력값 + 다음 | 부분~완전 커버 | `menuSelections` + 입력값 구조 지원 |
| 요구사항/사진 첨부 예약 | 부분~완전 커버 | `formData`(requests/photo) 기반 처리 |
| 예약 접수 시 시스템 메시지 | 완전 커버 | 예약 생성 이벤트 -> 시스템 메시지 전송 |
| 점주 메시지 상세, 고객은 접수완료 문구 | 부분 커버 | 시스템 메시지는 `reservationId` 중심. 역할별 상세 텍스트 분기 미구현 |

---

## 4. 핵심 갭 분석 (우선순위)

### P0 (플로우 완결에 즉시 영향)
1. 매장 설정 API에서 `publicHolidayOff`, `bookingWindowDays`, `minBookingLeadMinutes` 실제 반영 필요
2. 직원 생성 API(이름 입력) 필요
3. 메뉴 도메인에 `price` 모델/DTO/API 확장 필요

### P1 (캘린더 UX 완성도)
1. 월 API 응답에 공휴일 분리 필드 추가 필요
   - 예: `publicHolidayDates`, `regularHolidayDates`
2. 일 API 응답에 점유 슬롯/상태 확장 필요
   - 예: `slots: [{time, status}]` 또는 `occupiedSlots` 추가

### P2 (직원 정책 고도화)
1. 직원 단위 정기휴무/특정일휴무 모델 추가
2. 직원 단위 공휴일 휴무 on/off 정책 추가

---

## 5. 권장 다음 액션

1. `ShopScheduleService.updateSettings` 확장
   - interval 외 `bookingWindowDays/minBookingLeadMinutes/publicHolidayOff` 반영
2. Staff 관리 API 추가
   - 생성/조회/수정/삭제 + 샵 소유 검증
3. Menu 도메인 확장
   - `ShopMenu`에 `price` 추가 및 API/검증 보강
4. Availability 응답 스펙 고도화
   - 월: 공휴일 분리
   - 일: 점유 상태 표현
5. Role별 시스템 메시지 정책 명세화
   - 점주/고객 표시 차이를 서버 또는 클라이언트 계약으로 확정

---

## 6. 참고 코드 경로
- 보안/인증: `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
- 점주 스케줄: `src/main/java/com/example/easybooking/availability/presentation/ShopScheduleController.java`
- 직원 오버라이드: `src/main/java/com/example/easybooking/availability/presentation/StaffOperatingTimesController.java`
- 고객 가용 조회: `src/main/java/com/example/easybooking/availability/presentation/AvailabilityController.java`
- 예약: `src/main/java/com/example/easybooking/reservation/presentation/ReservationController.java`
- 예약 이벤트/시스템 메시지:
  - `src/main/java/com/example/easybooking/reservation/event/ReservationChatEventListener.java`
  - `src/main/java/com/example/easybooking/chat/SystemMessageWriter.java`
