# `#134 예약 변경 이력 저장` 구현 계획

작성일: `2026-06-18`  
기준 이슈: `#134`  
관련 브랜치: `feature/#134`

## 목적

예약 수정 시 before/after JSON을 `reservation_change_histories` 테이블에 기록한다.

이번 이슈는 변경 이력 저장만 다룬다. 이력 조회 API, 수정 자동 메시지, 취소 메타데이터는 후속 이슈로 분리한다.

## 구현 원칙

- 예약 수정(`PATCH /api/reservations/{id}`)과 같은 트랜잭션에서 저장한다.
- 변경 이력은 `CONFIRMED` 수정에서만 기록한다.
- before/after는 예약 수정 대상 필드 중심의 JSON으로 저장한다.
- 테스트는 이번 이슈 범위에서 추가하지 않는다.

## 현재 문제

- 예약 수정은 동작하지만 변경 내역이 DB에 남지 않는다.
- 사후에 무엇이 바뀌었는지 확인할 수 없다.
- `reservation_change_histories` 테이블은 문서에만 있고 코드에는 없다.

## 목표 범위

### 포함

- `reservation_change_histories` 테이블 추가
- 변경 이력 엔티티/리포지토리/저장 writer 추가
- 예약 수정 시 before/after JSON 저장

### 제외

- 변경 이력 조회 API
- 수정 자동 메시지
- 취소 메타데이터
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 변경 이력 테이블과 저장 구조를 추가한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/resources/db/migration/V16__add_reservation_change_histories.sql`
  - `src/main/java/com/example/easybooking/reservation/domain/ReservationChangeHistory.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationChangeHistoryRepository.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationChangeHistoryWriter.java`
- 검증: 컴파일 시 신규 엔티티와 writer 참조가 가능해야 한다.
- 완료 조건: 변경 이력 저장 구조가 생긴다.

### 변경 단위 2

- 목표: 예약 수정 시 before/after JSON을 저장한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: 컴파일 및 흐름 확인
- 완료 조건: 예약 수정 시 변경 이력이 기록된다.

## 완료 기준

- [ ] 변경 이력 테이블이 추가된다.
- [ ] 예약 수정 시 before/after JSON이 저장된다.
- [ ] 컴파일이 통과한다.
