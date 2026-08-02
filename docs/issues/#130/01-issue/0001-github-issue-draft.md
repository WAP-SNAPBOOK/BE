# `[REFACTOR]: 예약 시간축과 시간 단위 정책을 start_at 기준으로 정리`

Issue: `#130`  
Created: `2026-07-03`  
Repository: `WAP-SNAPBOOK/BE`  
Issue URL: `https://github.com/WAP-SNAPBOOK/BE/issues/130`  
Branch: `refactor/jiseob/#130`

---

## Background

현재 예약 모델은 `date`, `time`, `start_at`을 함께 저장한다. 문서와 코드에는 내부 점유 블록 10분 정책이 남아 있지만, 확정/수정 검증은 30분 단위를 사용해 시간 정책이 완전히 수렴되어 있지 않다.

관련 코드:

- `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
- `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- `src/main/java/com/example/easybooking/reservation/TimeBlockGenerator.java`
- `docs/issues/#97/reservation-erd-v2.md`

## Problem

`date/time/start_at`이 중복 진실원으로 남아 있으면 예약 생성, 확정, 수정, 캘린더 조회에서 값 불일치가 발생할 수 있다. 또한 고객 예약 생성은 10분 검증, 점유 블록은 10분 생성인데 확정/수정 검증은 30분 단위라 정책 설명과 실제 동작이 어긋난다.

## Goal

예약 시간축을 `start_at` 중심으로 수렴시키고, 시작 시간/소요 시간/점유 블록 단위를 하나의 정책으로 정렬한다.

## Scope

- 예약 시작 시간 정책을 10분 또는 30분 중 하나로 명시적으로 확정한다.
- `ReservationService`의 생성/확정/수정 시간 검증을 동일한 정책으로 통일한다.
- `TimeBlockGenerator`의 점유 블록 단위와 검증 정책을 정렬한다.
- `date`, `time`, `start_at` 공존 구간의 정합성 검증 또는 제거 계획을 수립한다.
- 레거시 호환이 끝났다면 `start_at`, `staff_id`의 `NOT NULL` 전환 가능성을 검토한다.
- 정책 확정 후 DB `CHECK` 제약 추가 여부를 검토한다.

## Out Of Scope

- 예약 UI의 시간 선택 UX 변경
- 신규 예약 상태 추가
- 결제/환불 정책 변경

## Acceptance Criteria

- [ ] 예약 생성, 확정, 수정에서 동일한 시간 단위 정책을 사용한다.
- [ ] `start_at`과 `date/time` 중 어떤 값이 기준인지 코드와 문서에 명확히 드러난다.
- [ ] `duration_minutes` 검증이 점유 블록 생성 단위와 불일치하지 않는다.
- [ ] 정책과 맞지 않는 요청은 일관된 도메인 예외로 실패한다.
- [ ] 필요한 경우 Flyway 마이그레이션으로 `CHECK` 또는 `NOT NULL` 제약을 단계적으로 추가한다.

## Risks

- 기존 프론트가 30분 단위 입력을 전제로 하고 있다면 10분 정책 전환 시 계약 확인이 필요하다.
- `date/time/start_at` 제거 또는 `NOT NULL` 전환은 기존 데이터 백필 검증 없이 진행하면 마이그레이션 실패 가능성이 있다.

## References

- `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
- `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- `src/main/java/com/example/easybooking/reservation/TimeBlockGenerator.java`
- `docs/issues/#97/03-adr/0004-adr-time-granularity-10m-vs-30m.md`
