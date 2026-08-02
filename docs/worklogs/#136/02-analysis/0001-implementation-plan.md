# `#136 예약 취소 메타데이터 저장` 구현 계획

작성일: `2026-06-18`  
기준 이슈: `#136`  
관련 브랜치: `feature/#136`

## 목적

예약 취소 시 취소 사유와 취소 메타데이터를 예약 레코드에 저장한다.

이번 이슈는 취소 메타데이터 저장만 다룬다. 취소 정책 세부 조정, 고객 셀프 취소, 취소 이력 조회는 후속 이슈로 분리한다.

## 구현 원칙

- 취소는 `PUT /api/reservations/{id}/cancel`에 사유를 함께 전달한다.
- 예약 엔티티에 취소 관련 컬럼을 추가한다.
- `CANCELED` 상태와 취소 메타데이터는 같은 트랜잭션에서 저장한다.
- 테스트는 이번 이슈 범위에서 추가하지 않는다.

## 현재 문제

- 취소 API는 상태만 바꾸고 사유를 저장하지 않는다.
- 취소 시각, 환불 가능 여부, 취소 주체를 추적할 수 없다.
- 예약 레코드에서 취소 이력을 바로 볼 수 없다.

## 목표 범위

### 포함

- `reservations` 테이블에 취소 메타데이터 컬럼 추가
- `ReservationCancelRequest` 추가
- 취소 API가 사유를 받도록 수정
- 예약 엔티티에 취소 메타데이터 저장
- 취소 시 `cancelTiming`과 `refundEligible` 계산

### 제외

- 취소 이력 테이블
- 고객 셀프 취소
- 취소 정책 상세 조정
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 취소 메타데이터 컬럼과 요청 DTO를 추가한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/resources/db/migration/V17__add_reservation_cancel_metadata.sql`
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationCancelRequest.java`
  - `src/main/java/com/example/easybooking/reservation/dto/ReservationStatusResponse.java`
  - `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
  - `src/main/java/com/example/easybooking/reservation/presentation/ReservationController.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: 컴파일 시 새 계약과 필드가 참조 가능해야 한다.
- 완료 조건: 취소 사유를 받아 저장할 구조가 생긴다.

### 변경 단위 2

- 목표: 취소 시 메타데이터를 실제로 채운다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
- 검증: 컴파일 및 흐름 확인
- 완료 조건: 취소 시각/사유/취소 시점/환불 가능 여부가 저장된다.

## 완료 기준

- [ ] 취소 메타데이터 컬럼이 추가된다.
- [ ] 취소 사유 요청이 저장된다.
- [ ] 취소 시점이 저장된다.
- [ ] 환불 가능 여부가 저장된다.
- [ ] 컴파일이 통과한다.
