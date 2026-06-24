# `#132 예약 상태 이력 저장` 구현 계획

작성일: `2026-06-18`  
기준 이슈: `#132`  
관련 브랜치: `feature/#132`

## 목적

예약 확정, 거절, 취소 같은 상태 전이를 `reservation_status_histories` 테이블에 기록한다.  
이번 이슈는 상태 이력 저장만 다루고, 조회 API와 변경 이력은 후속 이슈로 분리한다.

## 구현 원칙

- 상태 전이 기록은 예약 트랜잭션과 같은 단위로 저장한다.
- 예약 생성은 이번 이슈 범위에 포함하지 않는다.
- 이력 조회 API는 만들지 않는다.
- 테스트는 이번 이슈 범위에서 추가하지 않는다.

## 현재 문제

- `confirm`, `reject`, `cancel` 상태 전이가 DB에 남지 않는다.
- 사후에 어떤 사용자가 어떤 상태로 바꿨는지 추적할 수 없다.
- `reservation_status_histories` 테이블은 문서상 정의되어 있지만 실제 코드에 없다.

## 목표 범위

### 포함

- `reservation_status_histories` 테이블 추가
- 상태 이력 엔티티/리포지토리/기록 writer 추가
- 예약 확정 시 상태 이력 기록
- 예약 거절 시 상태 이력 기록
- 예약 취소 시 상태 이력 기록

### 제외

- 상태 이력 조회 API
- 변경 이력(`reservation_change_histories`)
- 취소 메타데이터
- 자동 메시지 문구 확장
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 상태 이력 테이블과 엔티티를 추가한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/resources/db/migration/V15__add_reservation_status_histories.sql`
  - `src/main/java/com/example/easybooking/reservation/domain/ReservationStatusHistory.java`
  - `src/main/java/com/example/easybooking/reservation/domain/repository/ReservationStatusHistoryRepository.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationStatusHistoryWriter.java`
- 검증: 컴파일 시 신규 엔티티와 리포지토리 참조가 가능해야 한다.
- 완료 조건: 상태 이력 저장 구조가 생긴다.

### 변경 단위 2

- 목표: 확정/거절/취소에서 상태 이력을 기록한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: 컴파일 및 흐름 확인
- 완료 조건: 상태 전이 시 이력 레코드가 함께 저장된다.

## 완료 기준

- [ ] 상태 이력 테이블이 추가된다.
- [ ] 확정/거절/취소 시 상태 이력이 저장된다.
- [ ] 컴파일이 통과한다.
