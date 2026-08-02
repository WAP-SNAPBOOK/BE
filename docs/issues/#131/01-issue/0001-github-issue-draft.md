# `[REFACTOR]: 핵심 도메인 참조 무결성을 DB 제약으로 보강`

Issue: `#131`  
Created: `2026-07-03`  
Repository: `WAP-SNAPBOOK/BE`  
Issue URL: `https://github.com/WAP-SNAPBOOK/BE/issues/131`  
Branch: `refactor/jiseob/#131`

---

## Background

현재 엔티티는 JPA 관계보다 `Long id` 참조를 직접 보관하는 구조가 많다. 이 선택은 서비스 조합과 점진 마이그레이션에는 단순하지만, DB FK와 CHECK 제약이 부족하면 잘못된 참조 데이터가 저장되어도 DB가 막지 못한다.

관련 영역:

- `reservations`, `reservation_time_blocks`, `reservation_menu_items`, `reservation_menu_input_values`
- `reservation_status_histories`, `reservation_change_histories`
- `shop`, `staff`, `shop_settings`, `shop_menus`, `shop_menu_input_fields`
- `chat_room`, `message`

## Problem

예약, 채팅, 매장, 직원, 메뉴, 이력 테이블의 핵심 참조가 코드 검증에 주로 의존하고 있다. 운영 중 수동 데이터 수정, 배치, 삭제 순서 오류, 코드 버그가 발생하면 orphan row 또는 잘못된 참조가 생길 수 있다.

## Goal

도메인 핵심 참조 무결성을 DB 제약으로 보강하고, 삭제 정책을 명시해 운영 데이터의 신뢰성을 높인다.

## Scope

- 핵심 테이블의 FK 후보를 목록화하고 적용 순서를 정한다.
- `reservation_time_blocks.reservation_id -> reservations.id`, `reservation_time_blocks.staff_id -> staff.id` FK를 우선 검토한다.
- `reservations.shop_id/customer_id/owner_user_id/staff_id` FK 적용 가능성을 검토한다.
- 예약 메뉴/입력값 테이블의 FK와 삭제 순서를 정리한다.
- 예약 상태/변경 이력의 `reservation_id`, `changed_by_user_id` FK를 검토한다.
- `shop`, `staff`, `shop_settings`, `shop_menus`, `shop_menu_input_fields`의 매장 경계 FK를 검토한다.
- FK 추가 전 기존 데이터 정합성 점검 쿼리를 작성한다.

## Out Of Scope

- JPA 엔티티를 `@ManyToOne` 중심 객체 그래프로 전환하는 작업
- 회원 탈퇴 정책 자체 변경
- 대량 데이터 정리 자동화 배치 구현

## Acceptance Criteria

- [ ] FK 적용 대상과 제외 대상이 문서화된다.
- [ ] FK 추가 전 깨진 참조를 찾는 검증 쿼리 또는 테스트가 준비된다.
- [ ] 핵심 예약 점유 테이블은 DB FK로 예약/직원 참조 무결성을 보장한다.
- [ ] 삭제 정책이 `RESTRICT`, `CASCADE`, 애플리케이션 수동 삭제 중 무엇인지 테이블별로 명확하다.
- [ ] Flyway 마이그레이션이 기존 데이터 검증과 단계적 적용을 고려한다.

## Risks

- 기존 데이터에 orphan row가 있으면 FK 마이그레이션이 실패한다.
- 애플리케이션에서 수동으로 삭제 순서를 관리하는 코드와 DB cascade 정책이 충돌할 수 있다.
- 모든 FK를 한 번에 추가하면 롤백 범위가 커진다.

## References

- `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationTimeBlock.java`
- `src/main/java/com/example/easybooking/user/service/UserCleanupService.java`
- `src/main/resources/db/migration/`
