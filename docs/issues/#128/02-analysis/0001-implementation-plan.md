# `#128 점주/직원용 캘린더 조회 API` 구현 계획

작성일: `2026-06-17`  
기준 이슈: `#128`  
관련 브랜치: `feature/#128`

## 목적

점주가 매장 예약을 모바일 캘린더 형태로 관리할 수 있도록, 선택 날짜 기준 7일 날짜 스트립과 선택일 하루 타임라인을 한 번에 조회하는 캘린더 전용 API를 추가한다.

이번 이슈는 조회 API에만 집중한다. 예약 확정/수정/거절/취소, 변경 이력, 고객 자동 메시지, 직원 로그인 권한은 후속 백로그로 분리한다.

## 구현 원칙

- 기존 `GET /api/reservations/shop` 목록 API는 변경하지 않는다.
- 특정 `shopId` 기준의 점주 전용 조회 API로 추가한다.
- 응답은 프론트가 날짜 스트립과 직원별 타임라인을 바로 렌더링할 수 있는 구조로 제공한다.
- 캘린더 조회 응답은 요약 정보만 포함하고, 사진/요청사항/가격/전체 메뉴는 기존 예약 상세 조회로 분리한다.

## 현재 문제

- 기존 점주 예약 목록 API는 캘린더 렌더링에 필요한 날짜 스트립, 직원 컬럼, 운영시간 범위, 비활성 구간을 제공하지 않는다.
- 예약은 `staffId`를 가지고 있지만, 전체 직원 컬럼 기반 일간 타임라인 응답이 없다.
- `PENDING`/`CONFIRMED` 예약을 캘린더 상태 라벨과 블록 높이로 구분할 전용 요약 DTO가 없다.

## 목표 범위

### 포함

- `GET /api/owner/shops/{shopId}/calendar?date=YYYY-MM-DD&staffId={staffId}` 추가
- 점주 소유 매장 검증
- 선택 날짜 기준 일요일 시작 7일 날짜 스트립 반환
- 날짜 스트립의 `hasPending`, `hasConfirmed`, `isHoliday`, `isSelected` 반환
- 선택일 타임라인 범위 반환
- 전체 보기에서 활성 직원 전체 컬럼 반환
- 특정 `staffId` 필터 시 해당 직원 1개 컬럼 반환
- 전체 보기에서 미지정 예약이 있으면 맨 오른쪽 `미지정` 컬럼 반환
- `PENDING`/`CONFIRMED` 예약 요약 반환
- 대표 메뉴명과 메뉴 개수 반환
- 휴무일/운영시간 없는 날에도 예약이 있으면 예약 범위 기준으로 표시 가능하게 반환

### 제외

- 예약 상세 바텀시트 보강
- 예약 확정/수정/거절/취소 액션
- 예약 변경 이력 테이블
- 고객 자동 채팅 메시지
- 직원 로그인 및 직원 권한 분기
- 빈 시간대 탭 신규 예약 생성

## 최소 변경 단위

### 변경 단위 1

- 목표: `#128` 조회 계약 문서와 프론트 전달 문서를 만든다.
- 분류: `Structural`
- 수정 대상:
  - `docs/issues/#128/02-analysis/0001-implementation-plan.md`
  - `docs/issues/#128/frontend-api-handoff.md`
- 검증: 문서 범위가 백로그 분리 결정과 충돌하지 않는지 확인한다.
- 완료 조건: 프론트가 API 경로, 파라미터, 응답 필드, 정책을 문서만 보고 이해할 수 있다.

### 변경 단위 2

- 목표: 캘린더 조회 API의 DTO, 컨트롤러, 서비스 골격을 추가한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/presentation/OwnerCalendarController.java`
  - `src/main/java/com/example/easybooking/reservation/service/OwnerCalendarService.java`
  - `src/main/java/com/example/easybooking/reservation/dto/calendar/*`
- 검증: 컴파일 기준으로 신규 타입 의존성이 깨지지 않는다.
- 완료 조건: API 진입점과 응답 타입이 생성된다.

### 변경 단위 3

- 목표: 선택 주 7일과 선택일 타임라인 데이터를 실제 예약/직원/운영시간 기준으로 조립한다.
- 분류: `Behavioral`
- 수정 대상:
  - `ReservationRepository`
  - `ReservationReader`
  - `OwnerCalendarService`
- 검증: 캘린더 서비스 단위 테스트 또는 최소 Gradle 테스트 실행
- 완료 조건: `PENDING`/`CONFIRMED` 예약만 날짜 스트립과 컬럼에 반영된다.

## 리스크와 대응

### 리스크 1. 직원별 운영시간 오버라이드 표현 한계

현재 `StaffOperatingTime`은 직원/요일당 단일 오버라이드 구조다. 복수 근무 구간은 매장 운영시간에는 가능하지만 직원 오버라이드는 단일 구간 중심이다.

대응: `#128`에서는 기존 `OperatingTimeResolver` 정책을 따른다.

### 리스크 2. 캘린더 요약에 메뉴 대표명이 필요함

예약 메뉴가 없는 과거 데이터가 있을 수 있다.

대응: 메뉴가 없으면 `representativeMenuName`은 `null`, `menuCount`는 `0`으로 내려준다.

### 리스크 3. 휴무일 예약 표시

운영시간이 없는 날에도 과거/예외 예약이 존재할 수 있다.

대응: 운영시간이 없고 예약이 있으면 예약 최소 시작~최대 종료에 앞뒤 30분 여백을 둔다.

## 완료 기준

- [ ] 캘린더 전용 조회 API가 추가된다.
- [ ] 선택 주 날짜 스트립과 선택일 직원 컬럼이 반환된다.
- [ ] 전체 보기/직원 필터/미지정 컬럼 정책이 반영된다.
- [ ] 프론트 전달 문서가 작성된다.
- [ ] 가능한 범위의 테스트를 통과한다.
