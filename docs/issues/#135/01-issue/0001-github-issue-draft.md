# `[REFACTOR]: 예약 사진과 이력 스냅샷 저장 정책을 정리`

Issue: `#135`  
Created: `2026-07-03`  
Repository: `WAP-SNAPBOOK/BE`  
Issue URL: `https://github.com/WAP-SNAPBOOK/BE/issues/135`  
Branch: `refactor/jiseob/#135`

---

## Background

예약 도메인은 사진, 상태 이력, 변경 이력, 채팅 시스템 메시지 스냅샷을 함께 사용한다. 현재 `reservation_photos`는 단순 element collection 구조이고, 변경 이력은 JSON 문자열, 채팅 메시지는 변경 스냅샷 컬럼들을 직접 보관한다.

관련 코드:

- `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationStatusHistory.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationChangeHistory.java`
- `src/main/java/com/example/easybooking/chat/domain/Message.java`
- `src/main/java/com/example/easybooking/chat/domain/ReservationChangeSnapshot.java`

## Problem

예약 사진에는 별도 PK, 정렬, 생성 시각이 없어 사진 순서/개별 삭제/감사 추적이 어렵다. 상태/변경 이력은 감사 데이터 성격이 강하지만 FK와 JSON 타입 정책이 명확하지 않다. 채팅 메시지의 예약 변경 스냅샷 컬럼도 예약 변경 이력과 책임 경계가 명확해야 한다.

## Goal

예약 첨부 사진과 예약 변경 이력의 저장 책임, 정합성 제약, 조회 요구를 정리한다.

## Scope

- `reservation_photos`에 별도 `id`, `sort_order`, `created_at`이 필요한지 검토한다.
- 사진 개별 삭제/순서 보존 요구가 있는지 확인하고 DB 모델에 반영한다.
- `reservation_status_histories`와 `reservation_change_histories`의 FK와 인덱스를 재검토한다.
- `before_json`, `after_json`을 `LONGTEXT`로 유지할지 MySQL `JSON` 타입으로 전환할지 결정한다.
- 예약 변경 이력과 채팅 시스템 메시지 스냅샷의 책임 경계를 문서화한다.
- 감사 데이터 삭제 정책을 회원/샵 삭제 정책과 함께 정리한다.

## Out Of Scope

- S3 파일 업로드/삭제 구현 변경
- 채팅 메시지 표시 UI 변경
- 예약 변경 이력 조회 API 신규 추가

## Acceptance Criteria

- [ ] 예약 사진의 정렬/개별 삭제/감사 요구가 명확히 결정된다.
- [ ] `reservation_photos` 구조 변경 여부가 결정되고 필요한 경우 Flyway 마이그레이션 계획이 있다.
- [ ] 상태/변경 이력 테이블의 FK, 인덱스, JSON 저장 타입 정책이 문서화된다.
- [ ] 채팅 메시지 스냅샷과 예약 변경 이력의 역할 차이가 코드 또는 문서에 명확하다.
- [ ] 삭제 정책이 사용자/샵 강제 삭제 흐름과 충돌하지 않는다.

## Risks

- `@ElementCollection`에서 엔티티 테이블로 전환하면 코드 변경 범위가 커질 수 있다.
- JSON 타입 변경은 DB 버전과 기존 데이터 형식 검증이 필요하다.
- 감사 데이터 보존 정책은 개인정보 삭제 요구와 충돌할 수 있다.

## References

- `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationStatusHistory.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationChangeHistory.java`
- `src/main/java/com/example/easybooking/chat/domain/Message.java`
- `src/main/java/com/example/easybooking/user/service/UserCleanupService.java`
