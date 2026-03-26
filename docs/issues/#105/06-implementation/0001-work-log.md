# #105 Work Log

## 목적
- `formData` 제거 전환 중 발생한 컴파일/테스트 실패를 우선 복구하고, 레거시 파생 응답 필드를 DTO 계약에서 제거한다.

## 진행 로그

### 2026-03-02 23:30 (Structural)
- 변경 요약:
  - 테스트 코드의 구 시그니처 사용(`setFormData`, `Reservation.createReservation(..., formJson, ...)`)을 신규 계약으로 정리.
  - 자동 치환 과정에서 생긴 UTF-8 BOM 문제(`\ufeff`)를 테스트 파일들에서 제거.
  - `ReservationServiceGetDetailMenuResponseTest`의 불필요 stubbing 제거.
- 테스트 결과:
  - `./gradlew compileTestJava` 성공.
  - `ReservationChatPublishIntegrationTest`의 `form_data_json NOT NULL` 오류는 엔티티 기본값 `"{}"` 세팅으로 해소.
- 리스크/메모:
  - DB 컬럼이 아직 `NOT NULL`이라 완전 제거 전까지 임시 호환값 유지 필요.

### 2026-03-02 23:36 (Behavioral)
- 변경 요약:
  - `ReservationCustomerResponse`, `ReservationOwnerResponse`, `ReservationDetailResponse`, `ReservationResponse`에서 `formData` 파생 필드(`part/removal/extend/wrapping` 계열) 제거.
  - `ReservationService`의 DTO 변환 호출을 신규 시그니처로 정리(`Map.of()` 전달 제거).
  - 신규 필드 단위테스트의 레거시 기대값(`formDataJson` 내용 검증) 제거.
- 테스트 결과:
  - `./gradlew compileJava compileTestJava` 성공.
  - 예약 조회/상세/신규필드 생성 관련 테스트 묶음 성공:
    - `ReservationServiceGetMyReservationsUnitTest`
    - `ReservationServiceGetShopReservationUnitTest`
    - `ReservationServiceGetCustomerReservationInChatUnitTest`
    - `ReservationServiceGetReservationsByCustomerInShopUnitTest`
    - `ReservationServiceGetDetailMenuResponseTest`
    - `ReservationServiceCreateReservationNewFieldsUnitTest`
- 리스크/메모:
  - 엔티티의 `formDataJson` 필드/DB 컬럼은 아직 남아 있음(다음 섹션에서 Flyway 포함 정리 필요).

## 다음 할 일
- 엔티티 `formDataJson` 필드 제거 + Flyway `V6` 마이그레이션 추가(`reservations.form_data_json` drop).

### 2026-03-02 23:39 (Behavioral)
- 변경 요약:
  - `Reservation` 엔티티에서 `formDataJson` 필드/초기화 제거.
  - Flyway 추가: `V6__drop_reservations_form_data_json.sql`로 컬럼 제거.
  - 관련 테스트의 잔존 참조(`getFormDataJson`) 및 레거시 주석 정리.
- 테스트 결과:
  - `./gradlew compileJava compileTestJava` 성공.
  - 핵심 테스트 성공:
    - `ReservationServiceGetDetailMenuResponseTest`
    - `ReservationServiceCreateReservationNewFieldsUnitTest`
    - `ReservationServiceDualWriteTest`
    - `ReservationChatPublishIntegrationTest`
- 리스크/메모:
  - 테스트 프로파일은 `flyway.enabled=false`이므로 컬럼 drop은 런타임 DB(Flyway 적용 환경)에서 반영됨.
  - 다음 단계는 API 문서(`api/03`, `api/04`) 최종 정합화.
