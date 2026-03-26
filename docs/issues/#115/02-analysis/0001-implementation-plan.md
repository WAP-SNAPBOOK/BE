# `#115` Implementation Plan

작성일: `2026-03-25`
최종 갱신: `2026-03-25 21:58:55 +09:00`

## 목표

링크 진입과 채팅 예약 버튼 진입이 공통 예약 시작 컨텍스트를 조회하게 만들어, 프론트가 예약 시작 전에 `staffId`를 확보할 수 있게 한다.

## 포함 범위

- 예약 진입 응답 DTO 추가
- `slugOrCode` 공통 조회 메서드 추가
- 직원 정렬 조회 메서드 추가
- 예약 진입 서비스/컨트롤러 추가
- 공개 경로 allowlist 추가
- 레거시 `shopId` 기반 availability API deprecated 표시
- 예약 진입 테스트 추가

## 제외 범위

- availability 계산 로직 변경
- 예약 생성 로직 변경
- 직원 공개 여부/표시 순서 정책 추가

## 단계별 구현 순서

1. `docs(issue): create #115 issue baseline [Structural]`
- 이슈 초안, 설계 메모, 구현 계획, worklog를 생성한다.

2. `refactor(shop): add slug-or-code shop resolver [Structural]`
- `ShopReader`에 `readBySlugOrPublicCode`를 추가하고 `ShopService`, `LinkService`가 재사용하게 정리한다.

3. `refactor(staff): add ordered staff lookup by shop [Structural]`
- `StaffRepository`, `StaffReader`에 `id ASC` 기준 조회 메서드를 추가한다.

4. `refactor(bookingentry): add booking entry response assembly [Structural]`
- `bookingentry` 패키지에 DTO와 서비스만 추가해 응답 조립 구조를 만든다.

5. `feat(bookingentry): expose public booking entry endpoint [Behavioral]`
- `GET /api/public/shops/{slugOrCode}/booking-entry`를 추가하고 공개 allowlist를 연결한다.

6. `feat(bookingentry): expose authenticated booking entry endpoint [Behavioral]`
- `GET /api/v1/shops/{shopId}/booking-entry`를 추가한다.

7. `chore(reservation): deprecate legacy shop availability endpoint [Structural]`
- 레거시 `GET /api/reservations/shop/{shopId}/availability`에 deprecated와 cleanup TODO를 표시한다.

8. `test(bookingentry): add booking entry coverage [Behavioral]`
- 공개 링크, 공개 코드 fallback, `shopId` 경로, allowlist 테스트를 추가한다.

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

- `staff` 모델에 정렬/공개 정책 필드가 없다.
- 공개 엔드포인트 추가 시 보안 설정 누락 가능성이 있다.

## 완료 기준

- 링크/채팅 예약 진입에서 공통 `booking entry` 응답을 조회할 수 있다.
- 응답만으로 `staffId`를 선택해 기존 availability API 호출이 가능하다.
- 관련 테스트가 통과한다.
