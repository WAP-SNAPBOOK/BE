## Summary

- 문제/목표:
    - 예약 생성 API가 레거시 `formData`에 의존하던 구조에서 벗어나, 명시 필드(`date`, `time`, `requirements`, `imageUrls`) 기반 계약으로 일원화한다.
    - `formData` 누락 시 발생 가능한 500/NPE 위험을 제거하고, 문서/코드/테스트 계약을 동일하게 맞춘다.
- 대안:
    - A안: `formData` 유지 + null-safe 보완
    - B안(선택): `formData` 제거 + 신규 명시 필드 계약으로 즉시 전환
- 선택 이유:
    - 레거시 경로를 남기면 계약 이중화로 회귀 포인트가 늘어나므로, 단일 계약으로 전환하는 편이 운영/연동 리스크가 낮다.

## Changes

- 예약 생성 계약을 `formData`에서 명시 필드(`date/time/requirements/imageUrls`) 중심으로 전환.
- 레거시 경로 정리: 서비스/DTO/엔티티의 `formData`/`formDataJson` 의존 제거 및 응답 계약 정리.
- DB/문서 정합화: Flyway 컬럼 제거(`V6`) 및 API 명세(`api/03`, `api/04`)를 신규 스키마로 동기화.

## Test plan

- TDD 문서:
    - `docs/issues/#105/04-tdd/plan.md`
- 확인한 테스트 (체크리스트):
    - [x] 레거시 `formData` 요청 거부(4xx) 계약 고정
    - [x] `date/time` 필수값 검증(4xx) 고정
    - [x] 신규 필드 기반 예약 생성 성공 경로 검증
    - [x] `formData` 의존 제거 후 회귀(500) 방지 확인
    - [x] 문서 정합화 항목(6-1, 6-2) 완료 반영
- 실행 결과 (work-log 기준):
    - `./gradlew compileJava compileTestJava` 성공
    - `ReservationServiceCreateReservationNewFieldsUnitTest` 성공
    - `ReservationServiceDualWriteTest` 성공
    - `ReservationChatPublishIntegrationTest` 성공
    - 예약 조회/상세 관련 단위테스트 묶음 성공
- 엣지 케이스/회귀 포인트:
    - 레거시 payload 재유입 시 500이 아닌 4xx 유지
    - Flyway 적용 환경에서 `form_data_json` 제거 후 런타임 영향 점검
    - 클라이언트 요청 계약(`date/time`) 누락 회귀 방지

