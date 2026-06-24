# `#133 예약 수정 API` 구현 계획

작성일: `2026-06-18`  
기준 이슈: `#133`  
관련 브랜치: `feature/#133`

## 목적

점주가 확정된 예약의 시작 시간, 소요시간, 담당 직원, 점주 전달 메시지를 수정할 수 있도록 예약 수정 API를 추가한다.

이번 이슈는 수정 동작 자체만 다룬다. 수정 자동 메시지, 변경 이력, 사유 저장은 후속 이슈로 분리한다.

## 구현 원칙

- 수정은 `CONFIRMED` 예약에만 허용한다.
- 점주 전용으로 먼저 제공한다.
- 수정 후에는 최신 예약 상세 응답을 반환한다.
- 기존 점유 블록은 제거 후 새 값 기준으로 다시 생성한다.
- 테스트는 이번 이슈 범위에서 추가하지 않는다.

## 현재 문제

- `CONFIRMED` 예약을 사후에 수정하는 API가 없다.
- 점주가 시간/담당자/소요시간을 조정해도 백엔드가 반영하지 못한다.
- 수정 후 프론트가 최신 상태를 다시 그릴 수 있는 API가 없다.

## 목표 범위

### 포함

- `PATCH /api/reservations/{id}` 추가
- `CONFIRMED` 상태 검증
- 시작 시간 변경
- 소요시간 변경
- 담당 직원 변경
- 점주 전달 메시지 변경
- 점유 블록 재생성
- 최신 `ReservationDetailResponse` 반환

### 제외

- 수정 자동 메시지
- 변경 이력 저장
- 취소/거절 메타데이터
- 고객 셀프 수정
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 예약 수정 요청/응답 계약과 엔드포인트를 추가한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationUpdateRequest.java`
  - `src/main/java/com/example/easybooking/reservation/presentation/ReservationController.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: 컴파일 시 새 계약이 참조 가능해야 한다.
- 완료 조건: 수정 API 진입점이 생긴다.

### 변경 단위 2

- 목표: 확정 예약을 실제로 갱신하고 점유 블록을 재생성한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationTimeBlockWriter.java`
- 검증: 컴파일 및 흐름 확인
- 완료 조건: 수정 후 최신 예약 상세가 반환된다.

## 완료 기준

- [ ] 예약 수정 API가 추가된다.
- [ ] `CONFIRMED` 예약만 수정된다.
- [ ] 점유 블록이 재생성된다.
- [ ] 최신 상세 응답이 반환된다.
- [ ] 컴파일이 통과한다.
