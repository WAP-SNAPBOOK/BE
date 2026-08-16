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
    - `ReservationCustomerResponse`, `ReservationOwnerResponse`, `ReservationDetailResponse`, `ReservationResponse`에서
      `formData` 파생 필드(`part/removal/extend/wrapping` 계열) 제거.
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

### 2026-03-26 17:14 (Structural)

- 변경 요약:
    - `#105` 프론트 전달용 문서 `frontend-api-handoff.md` 초안 작성.
    - `docs/api/03-api-spec.md`, `docs/api/04-api-spec-by-flow.md`를 현재 코드 기준으로 정합화.
    - 생성 요청/응답 필드명 차이(`requirements` -> `requests`, `imageUrls` -> `photoUrls`)와 레거시 `formData` 제거 사실을 문서에 명시.
    - `docs/specs/reservation/tech-api-spec.md`에 상세/목록 응답 한계(`requirements` 미포함, `formData` 파생 필드 제거)를 반영.
- 검증:
    - `git diff origin/develop...HEAD` 기준 실제 변경 파일/DTO/서비스 계약과 문서 내용을 대조.
    - `ReservationCreateRequest`, `ReservationResponse`, `ReservationDetailResponse`, `ReservationCustomerResponse`,
      `ReservationOwnerResponse`, `ReservationService`, `ReservationCreateRequestContractIntegrationTest` 기준 문서 교차 확인.
- 리스크/메모:
    - 현재 백엔드는 생성 응답에서만 `requests`를 내려주고 상세/목록 응답에는 `requirements`를 내려주지 않는다.
    - 응답 필드명 개편은 이번 문서 작업 범위에서 제외하고, 프론트 문서에서 과도기 계약으로 안내한다.

### 2026-03-26 17:24 (Structural)

- 변경 요약:
    - `requirements` 미저장과 요청/응답 필드명 불일치를 후속 설계 이슈로 분석.
    - `docs/issues/#105/02-analysis/0002-contract-gap-and-fix-plan.md`에 대안 A/B/C와 권장 롤아웃을 정리.
    - 권장안은 "`requirements` 영속화 + 응답 필드 additive 정렬"로 문서화.
- 검증:
    - `Reservation`, `ReservationCreateRequest`, `ReservationResponse`, `ReservationDetailResponse`,
      `ReservationCustomerResponse`, `ReservationOwnerResponse`, `ReservationService` 기준으로 현재 계약 갭 확인.
- 리스크/메모:
    - 현재 후속 범위는 "의미가 다른 데이터를 같은 예약 모델로 정렬하는 것"에 집중한다.

### 2026-03-26 17:58 (Structural)

- 변경 요약:
    - 별도 호환성 검토가 필요한 포맷 논점을 `#105` 활성 설계 범위에서 제외했다.
    - `docs/issues/#105/02-analysis/0002-contract-gap-and-fix-plan.md`에서 관련 논점과 롤아웃 항목을 제거했다.
    - 공용 backlog 문서 1건을 추가해 후속 검토 대상으로 분리했다.
- 검증:
    - `#105` 설계 문서와 worklog에서 해당 포맷 논점이 활성 과제로 남아 있지 않은지 확인했다.
- 리스크/메모:
    - 현재 코드 계약은 바뀌지 않았으므로 런타임 동작과 응답 예시는 그대로 유지된다.

### 2026-03-26 18:08 (Structural)

- 변경 요약:
    - `Reservation` 엔티티에 nullable `requirements` 매핑과 setter를 추가했다.
    - Flyway 마이그레이션 `V9__add_reservations_requirements.sql`로 `reservations.requirements` 컬럼을 추가했다.
    - `ReservationRequirementsJpaMappingTest`를 추가해 JPA 영속화 경로를 보강했다.
- 검증:
    - `./gradlew test --tests "com.example.easybooking.reservation.domain.ReservationRequirementsJpaMappingTest"` 성공.
- 리스크/메모:
    - 테스트 프로파일은 `ddl-auto=create-drop`이므로, 실제 Flyway 적용 환경에서는 `V9`가 배포 순서대로 반영되어야 한다.

### 2026-03-26 18:08 (Behavioral)

- 변경 요약:
    - 예약 생성 시 `request.requirements`를 실제 엔티티에 저장하도록 변경했다.
    - `ReservationResponse`, `ReservationDetailResponse`, `ReservationCustomerResponse`, `ReservationOwnerResponse`에 표준
      필드 `requirements`, `imageUrls`, `imageCount`를 추가했다.
    - 레거시 필드 `requests`, `photoUrls`, `photoCount`는 유지해 additive 방식으로 확장했다.
    - 프론트 전달 문서와 API 문서를 현재 구현 기준으로 다시 정합화했다.
- 검증:
    -
    `./gradlew test --tests "com.example.easybooking.reservation.domain.ReservationRequirementsJpaMappingTest" --tests "com.example.easybooking.reservation.service.ReservationServiceCreateReservationNewFieldsUnitTest" --tests "com.example.easybooking.reservation.presentation.ReservationCreateRequestContractIntegrationTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetDetailMenuResponseTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetMyReservationsUnitTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetShopReservationUnitTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetCustomerReservationInChatUnitTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetReservationsByCustomerInShopUnitTest"`
    성공.
- 리스크/메모:
    - `time` 직렬화 포맷은 이번 범위에서 바꾸지 않았고, 별도 backlog로 유지한다.

### 2026-03-29 15:06 (Structural)

- 변경 요약:
    - `develop`에 이미 적용된 `V6__shop_tags.sql`, `V7__shop_menu_tags_add_shop_tag_id.sql`, `V8__backfill_shop_tags.sql`와
      충돌하지 않도록 `#105` 런타임 Flyway 파일명을 재배치했다.
    - `V6__drop_reservations_form_data_json.sql`를 `V9__drop_reservations_form_data_json.sql`로 옮겼다.
    - `V9__add_reservations_requirements.sql`를 `V10__add_reservations_requirements.sql`로 옮겼다.
    - `#105` 구현 계획/설계/PR 문서의 배포 기준 마이그레이션 번호도 함께 정합화했다.
- 검증:
    - 운영 마이그레이션 디렉터리에서 duplicate version이 없는지 확인했다.
    -
    `./gradlew test --tests "com.example.easybooking.reservation.domain.ReservationRequirementsJpaMappingTest" --tests "com.example.easybooking.shop.ShopTagMigrationTablesTest" --tests "com.example.easybooking.shop.ShopTagBackfillMigrationTest"`
    성공.
- 리스크/메모:
    - 테스트 기본 프로필은 Flyway를 끄므로, 실제 배포 검증은 `dev` 프로필 기동 또는 배포 후 `flyway_schema_history` 확인으로 마무리해야 한다.
