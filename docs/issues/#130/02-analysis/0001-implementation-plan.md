# `#130 예약 취소 API` 구현 계획

작성일: `2026-06-18`  
기준 이슈: `#130`  
관련 브랜치: `feature/#130`

## 목적

점주가 캘린더 상세 바텀시트에서 예약을 취소할 수 있도록 예약 취소 API를 추가한다.  
이번 이슈는 취소 상태 전환과 `CONFIRMED` 예약의 점유 블록 해제까지만 다룬다. 취소 사유 저장, 취소 메타데이터, 자동 메시지는 후속 이슈로 분리한다.

## 구현 원칙

- 기존 예약 상세/확정/거절 API는 유지한다.
- 취소는 점주 전용으로 먼저 제공한다.
- `CONFIRMED` 예약 취소 시 예약 점유 블록을 해제한다.
- `PENDING` 예약도 취소 가능하다.
- 테스트는 이번 이슈 범위에서 추가하지 않는다.

## 현재 문제

- `Reservation` 엔티티에는 `cancel()` 메서드가 있지만, 이를 노출하는 API가 없다.
- `CONFIRMED` 예약을 캘린더에서 취소해도 점유 블록을 삭제하는 경로가 없다.
- 캘린더 운영 화면에서 취소 버튼을 눌러도 백엔드가 처리하지 못한다.

## 목표 범위

### 포함

- `PUT /api/reservations/{id}/cancel` 추가
- 점주 권한 검증
- 예약 소유 샵 검증
- `Reservation.cancel()` 호출
- `CONFIRMED` 예약의 점유 블록 삭제

### 제외

- 취소 사유 저장
- 취소 메타데이터 저장
- 자동 채팅 메시지 발송
- 고객 셀프 취소
- 취소 이력 테이블
- 테스트 추가

## 최소 변경 단위

### 변경 단위 1

- 목표: 예약 취소 API를 노출한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/presentation/ReservationController.java`
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- 검증: 컴파일 시 새 엔드포인트와 서비스 메서드가 참조 가능해야 한다.
- 완료 조건: 취소 API 진입점이 생긴다.

### 변경 단위 2

- 목표: 취소 시 예약 상태를 변경하고 점유 블록을 제거한다.
- 분류: `Behavioral`
- 수정 대상:
  - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
  - `src/main/java/com/example/easybooking/reservation/ReservationTimeBlockWriter.java`
- 검증: 컴파일 및 수동 흐름 확인
- 완료 조건: 취소 후 예약 상태가 `CANCELED`로 바뀌고, 확정 예약의 블록이 제거된다.

## 리스크와 대응

### 리스크 1. 취소 사유가 없으면 운영 정보가 빈약함

대응: 이번 이슈는 상태 전환과 블록 해제만 수행하고, 사유와 메타데이터는 다음 이슈에서 다룬다.

### 리스크 2. 이미 취소/거절된 예약 재취소

대응: `Reservation.cancel()`의 기존 상태 검증을 그대로 사용한다.

## 완료 기준

- [ ] 점주 취소 API가 추가된다.
- [ ] 취소 시 예약 상태가 `CANCELED`로 바뀐다.
- [ ] 확정 예약의 점유 블록이 삭제된다.
- [ ] 컴파일이 통과한다.
