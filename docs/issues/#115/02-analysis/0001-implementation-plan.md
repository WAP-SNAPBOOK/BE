# `#115` Implementation Plan

작성일: `2026-03-25`
최종 갱신: `2026-03-25 17:44:43 +09:00`

## 목표

링크 진입과 채팅 예약 버튼 진입이 공통 예약 시작 컨텍스트를 조회하게 만들어, 프론트가 예약 시작 전에 `staffId`를 확보할 수 있게 한다.

## 포함 범위

### Structural

- `booking entry` 전용 DTO 추가
- `slugOrCode` 공통 조회 메서드 추가
- 직원 정렬 조회 메서드 추가
- `booking entry` 서비스 구조 추가

### Behavioral

- `GET /api/public/shops/{slugOrCode}/booking-entry` 추가
- `GET /api/v1/shops/{shopId}/booking-entry` 추가
- 공개 경로 allowlist 추가
- 레거시 `GET /api/reservations/shop/{shopId}/availability` deprecated 표시
- 예약 진입 통합 테스트 추가

## 제외 범위

- availability 계산 로직 변경
- 예약 생성 로직 변경
- 직원 공개 여부/표시 순서 정책 추가

## 단계별 구현 순서

1. 문서 기준선 생성
2. Structural 변경
3. Behavioral 변경
4. 테스트 추가 및 실행
5. 규칙에 맞춰 커밋 정리

## 예상 파일

- `src/main/java/com/example/easybooking/shop/ShopReader.java`
- `src/main/java/com/example/easybooking/link/LinkService.java`
- `src/main/java/com/example/easybooking/shop/service/ShopService.java`
- `src/main/java/com/example/easybooking/staff/StaffReader.java`
- `src/main/java/com/example/easybooking/staff/repository/StaffRepository.java`
- `src/main/java/com/example/easybooking/bookingentry/...`
- `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
- `src/main/java/com/example/easybooking/reservation/presentation/ReservationController.java`
- `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- `src/test/java/com/example/easybooking/bookingentry/...`
- `src/test/java/com/example/easybooking/auth/...`

## 리스크

- `staff` 모델에 정렬/공개 정책이 없다.
- 공개 엔드포인트 추가 시 보안 설정 누락 가능성이 있다.

## 완료 기준

- 링크/채팅 예약 진입에서 공통 `booking entry` 응답을 조회할 수 있다.
- 응답만으로 `staffId`를 선택해 기존 availability API 호출이 가능하다.
- 관련 테스트가 통과한다.
