# #101 교차 이슈 기준 User Flow 감사 보고서

작성일: 2026-02-14  
기준 브랜치: 현재 워크트리 기준  
검토 범위: `docs/issues/#97`, `docs/issues/#99`, `docs/issues/#101`, 관련 구현 코드

---

## 1. 목적

- 이슈별로 흩어진 문서(#97/#99/#101)를 한 번에 묶어 실제 사용자 플로우(점주/고객) 커버리지를 점검한다.
- 문서 기준 요구사항과 현재 구현(API/서비스) 간 차이를 식별한다.
- 다음 개발 우선순위를 플로우 기준으로 정리한다.

---

## 2. 이슈별 책임 범위

| 이슈 | 핵심 책임 | 플로우 내 위치 |
|---|---|---|
| #97 | 예약 코어(확정 시 점유 블록, staff 연계, 상태 전이) | 예약 생성/확정/거절, 충돌 방지 |
| #99 | 메뉴/태그/입력필드 + 예약 시 메뉴 선택 저장(dual-write) | 예약 메뉴 선택 화면/검증/조회 |
| #101 | 운영시간/휴무일/직원 오버라이드 + 일/월 가용 조회 | 캘린더/시간 선택 화면 |

근거 문서:
- `docs/issues/#97/reservation-erd-v2.md`
- `docs/issues/#97/04-tdd/plan.md`
- `docs/issues/#99/01-issue/0001-github-issue-draft.md`
- `docs/issues/#99/04-tdd/plan.md`
- `docs/issues/#101/04-tdd/plan.md`
- `docs/issues/#101/08-analysis/0001-current-app-feature-flow-analysis.md`

---

## 3. 통합 User Flow 커버리지

### 3.1 점주 온보딩/설정 플로우

| 단계 | 현재 상태 | 근거 |
|---|---|---|
| 점주 회원가입 | 완전 커버 | `src/main/java/com/example/easybooking/user/presentation/UserController.java` (`POST /user/owner/signup`) |
| 매장 생성 | 완전 커버 | `src/main/java/com/example/easybooking/shop/presentation/ShopController.java` (`POST /shop`) |
| 매장 생성 시 기본 Staff 자동 생성 | 완전 커버 | `src/main/java/com/example/easybooking/shop/service/ShopService.java` |
| 운영시간 타입 설정(DAILY/WEEKDAY_WEEKEND/BY_DAY) | 완전 커버 | `src/main/java/com/example/easybooking/availability/presentation/ShopScheduleController.java` |
| 운영시간 복수 블록(휴게시간) | 완전 커버 | `UpdateShopOperatingTimesRequest` + `ShopScheduleService` row 저장 로직 |
| 운영시간 end를 마지막 예약 가능 시각으로 사용 | 완전 커버 | `docs/issues/#101/04-tdd/plan.md` 12/14 섹션, `SlotGenerator` 정책 |
| 간격 설정(30/60분) | 부분 커버 | interval 변경 가능. 허용값 화이트리스트(30/60 고정)는 없음 |
| 휴무일(매주/격주/매달/특정일) 설정 | 완전 커버 | `ShopScheduleController` holidays API + `HolidayChecker` |
| 공휴일 휴무 on/off | 부분 커버 | 도메인/판정(`HolidayChecker`)은 존재. 설정 업데이트 API는 현재 interval만 반영 |
| 직원 추가/수정/삭제 | 미지원 | Staff CRUD 전용 Controller 없음 |
| 직원별 정기휴무/특정일휴무 | 미지원 | 직원은 요일 오버라이드(`isOff`/start/end)만 존재 |

### 3.2 고객 예약 플로우

| 단계 | 현재 상태 | 근거 |
|---|---|---|
| 채팅 진입 후 예약 화면 이동 | 부분 커버 | 채팅방/링크 API 존재 (`/chat/rooms/shop/{shopId}`, `/link/chat/{slugOrCode}`) |
| 월 캘린더 조회(예약 가능일 표시) | 완전 커버 | `src/main/java/com/example/easybooking/availability/presentation/AvailabilityController.java` (`/availability/monthly`) |
| 월 캘린더에서 휴무/운영없음 구분 | 완전 커버 | `availableDates`, `holidayDates`, `closedDates` 반환 |
| 공휴일을 별도 색상으로 분리 표시 | 부분 커버 | 현재 `holidayDates`에 정기휴무+공휴일이 합쳐짐(분리 필드 없음) |
| 날짜 선택 후 시간 조회 | 완전 커버 | `/availability?date=...` |
| 점유된 시간 회색 표시 | 부분 커버 | 현재 API는 가능한 슬롯만 반환. 점유 슬롯 자체 리스트는 미반환 |
| 메뉴 목록/태그 필터 | 완전 커버 | `ShopMenuController`, `TagController` |
| 메뉴별 입력값 제출 | 완전 커버 | `ReservationCreateRequest.menuSelections` + 저장 로직 |
| 요청사항/사진 포함 예약 | 부분 커버 | `formData`로 지원(사진은 `photo` JSON 관례 의존) |
| 예약 접수/확정/거절 시스템 메시지 | 완전 커버 | `ReservationChatEventListener`, `SystemMessageWriter` |
| 점주/고객 메시지 표현 분기 | 부분 커버 | 메시지는 reservationId+type 중심, 역할별 문구 템플릿 분리는 클라이언트 책임 |

---

## 4. 플로우 기준 핵심 갭

### P0 (핵심 기능 완결성)

1. 매장 설정 업데이트 불완전
- `UpdateShopScheduleSettingsRequest`는 `bookingWindowDays/minBookingLeadMinutes/publicHolidayOff`를 받지만,  
  `ShopScheduleService.updateSettings()`는 `intervalMinutes`만 갱신한다.
- 근거: `src/main/java/com/example/easybooking/availability/ShopScheduleService.java`

2. 직원 관리 플로우 미완성
- "직원 추가/이름 설정"에 해당하는 API가 없다.
- 현재는 매장 생성 시 기본 Staff 자동 생성만 동작한다.

### P1 (예약 UX 완성도)

1. 공휴일 분리 표현 미지원
- 월 API에서 공휴일과 정기휴무가 `holidayDates`로 통합되어 내려온다.
- 공휴일 빨간색/정기휴무 회색 같은 UI 정책은 서버 응답만으로 직접 구현하기 어렵다.

2. 점유 슬롯 상태 표시 미지원
- 일 API는 가능한 슬롯만 반환한다.
- "예약 불가 슬롯 회색 표시"를 위해서는 전체 슬롯+상태 또는 점유 슬롯 목록이 필요하다.

### P2 (권한/정합성)

1. 메뉴/태그/입력필드 API의 소유권 검증 공백
- `shopId` path variable이 서비스 검증에 활용되지 않는 경로가 있다.
- 근거:
  - `src/main/java/com/example/easybooking/shop/service/ShopMenuManagementService.java`
  - `src/main/java/com/example/easybooking/shop/service/TagService.java`
  - `src/main/java/com/example/easybooking/shop/service/ShopMenuInputFieldService.java`

---

## 5. 문서 간 드리프트(정합성 점검)

1. #97 문서 내 시간 단위 충돌
- 일부 문서는 30분 정책을 서술하지만, 실제 계획/구현은 10분 단위로 수렴되어 있다.
- 영향: 온보딩/운영 문서 해석 혼선.

2. #97/#99 TDD plan의 "진행 상태 표"와 본문 체크 불일치
- 하단 진행 상태 표는 `대기`가 남아 있으나, 본문 테스트 항목은 다수 완료(`[x]`) 상태.
- 영향: 실제 완료도 파악 어려움.

---

## 6. 결론

- 현재 구조는 "점주 운영 설정(#101) + 예약 코어(#97) + 메뉴 입력(#99)"를 연결해 기본 예약 플로우를 실행 가능한 수준까지 커버한다.
- 다만 실제 서비스 플로우 완성 관점에서는 아래 3개가 우선 보강 대상이다.
  1. 매장 설정 업데이트 필드 전체 반영
  2. 직원 CRUD 및 직원 단위 휴무 정책
  3. 캘린더/슬롯 응답 스펙 확장(공휴일 분리, 점유 상태 노출)

