# 예약 가능 시간 API를 `slot + status` 응답으로 정리

Issue: `#108`  
Created: 2026-03-17

---

## 배경

현재 고객용 예약 가능 시간 조회 API인 `GET /api/v1/shops/{shopId}/staff/{staffId}/availability`는 서버에서 이미 최종 가용 시간을 계산하지만, 응답은 사실상 `가능한 시간 목록` 중심으로만 제공된다.

예약 페이지는 다음 화면 요구를 가진다.

- 시간순으로 예약 가능 시간대를 나열한다
- 예약 가능한 시간은 선택 가능하게 보여준다
- 예약이 불가능한 시간은 회색 비활성화로 보여준다

이 요구를 프론트가 쉽게 처리하려면 단순 `availableSlots`보다 화면용으로 바로 순회 가능한 응답이 필요하다.

논의 결과, `timeRanges + availableSlots` 방식도 가능하지만 이번 이슈에서는 `slot + status` 방식으로 정리한다.

선택 이유는 다음과 같다.

- 프론트가 시간축을 재구성할 필요 없이 그대로 렌더링할 수 있다
- 운영시간 구간, 휴게 구간, 예약 블록, 리드타임 등으로 빠진 슬롯을 프론트가 다시 추론하지 않아도 된다
- 서버가 최종 판단한 결과를 화면 계약으로 그대로 노출할 수 있어 해석 차이가 줄어든다
- 이후 상태가 늘어나더라도(`AVAILABLE`, `UNAVAILABLE`, `BOOKED`, `LEAD_TIME_BLOCKED` 등) 확장성이 좋다

## Goals

- `availability` 응답을 예약 페이지가 바로 렌더링할 수 있는 형태로 정리한다
- 슬롯 단위로 선택 가능/불가 상태를 함께 제공한다
- 프론트가 별도 계산 없이 순회와 스타일링만으로 화면을 그릴 수 있게 한다
- 서버가 최종 판단한 가용성 결과를 API 응답에 명확히 드러낸다

## Non-goals

- 예약 생성/확정 로직 자체 변경
- 월 단위 가용성 응답 구조 변경
- 점주용 스케줄 설정 API 구조 전면 개편
- 프론트의 컴포넌트 구조나 스타일링 방식 확정

## 요구사항

### 기능

- `GET /api/v1/shops/{shopId}/staff/{staffId}/availability`는 날짜 기준 슬롯 목록을 시간순으로 응답한다
- 각 슬롯은 최소한 `time`, `status`를 포함한다
- 응답에는 슬롯 생성 기준인 `intervalMinutes`를 포함한다
- 휴무일 여부는 기존처럼 함께 응답한다
- 프론트는 `slots[]`를 그대로 순회하여 활성/비활성 UI를 구성할 수 있어야 한다
- 서버는 기존 availability 계산 결과를 기반으로 슬롯 상태를 결정한다

### 비기능

- 응답은 프론트가 추가 계산 없이 사용할 수 있을 정도로 자기완결적이어야 한다
- 상태 값은 이후 확장 가능해야 한다
- 기존 availability 계산 책임은 서버에 유지되어야 한다
- 응답 변경에 따른 영향 범위는 일 단위 availability API와 해당 소비 화면으로 제한한다

## 수용 기준 (AC)

- AC-1: 일 단위 availability API 응답에 `intervalMinutes`가 포함된다
- AC-2: 응답의 `slots`는 시간순 정렬된 슬롯 목록이며, 각 항목은 최소 `time`, `status`를 포함한다
- AC-3: 예약 가능한 슬롯은 `AVAILABLE` 상태로 내려간다
- AC-4: 예약이 불가능한 슬롯은 비활성 상태로 내려가며, 프론트는 이 값을 기반으로 회색 처리할 수 있다
- AC-5: 프론트는 별도의 시간 간격 계산 없이 `slots[]` 순회만으로 예약 화면을 렌더링할 수 있다
- AC-6: 기존 서버 availability 계산 결과와 프론트 표시 결과 사이에 해석 차이가 없어야 한다

## 범위/의존성

- 대상 API
  - `AvailabilityController.getAvailability()`
  - `CustomerAvailabilityService.getDailyAvailability()`
  - `AvailabilitySlotsResponse`
- 관련 도메인/서비스
  - `AvailabilityService`
  - `OperatingTimeResolver`
  - `SlotGenerator`
- 소비 주체
  - 예약 페이지 프론트엔드

## 리스크

- 상태값 설계를 너무 단순하게 시작하면 이후 사유 구분이 필요할 때 다시 변경이 발생할 수 있다
- 기존 `availableSlots` 중심 응답을 사용하던 프론트 코드가 있다면 DTO 변경 영향이 생길 수 있다
- `status` 의미를 명확히 합의하지 않으면 백엔드/프론트 해석 차이가 생길 수 있다

## 운영 메모

- 1차 구현에서는 `AVAILABLE` / `UNAVAILABLE`처럼 단순 상태로 시작할 수 있다
- 이후 필요 시 `BOOKED`, `LEAD_TIME_BLOCKED`, `OUTSIDE_OPERATING_RANGE` 등으로 세분화할 수 있다
- `timeRanges + availableSlots` 방식은 프론트 유연성은 있지만, 화면 계약 관점에서는 `slot + status`보다 해석 비용이 높아 이번 이슈에서는 채택하지 않는다

## 작업 체크리스트

- [ ] `availability` 응답 구조를 `slot + status` 기준으로 확정
- [ ] `AvailabilitySlotsResponse` DTO 초안 작성
- [ ] `CustomerAvailabilityService` 응답 조립 방식 정리
- [ ] 슬롯 상태 표현 규칙 정의 (`AVAILABLE`, `UNAVAILABLE` 등)
- [ ] 예약 페이지 프론트와 응답 계약 합의
- [ ] 기존 분석 문서와 결정 문서에 최종 방향 반영

## 관련 문서

- 분석 문서: `docs/issues/#108/02-analysis/0001-availability-response-design.md`
