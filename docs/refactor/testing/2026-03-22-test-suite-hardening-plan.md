# 테스트 스위트 정비 계획

작성일: `2026-03-22`
기준 브랜치: `develop`

## 목적

`study/study-tdd` 기준으로 현재 프로젝트 테스트를 다시 정렬한다.

이번 정비의 1차 목표는 새 테스트를 늘리는 것이 아니라 아래 세 가지다.

1. 테스트 스위트가 실제로 실행 가능한 상태를 회복한다.
2. 구조적으로 brittle 하거나 저가치인 테스트를 식별하고 분리한다.
3. 이후 `Behavioral` 개선을 안전하게 진행할 기준선을 만든다.

## 왜 이 순서로 진행하는가

현재는 `./gradlew test` 가 `compileTestJava` 단계에서 실패한다.

이 상태에서는 다음 문제가 있다.

- 어떤 테스트가 실제로 프로젝트를 보호하는지 신뢰할 수 없다.
- RED/GREEN 사이클을 시작해도 기존 실패와 신규 실패가 섞인다.
- 실제 버그 테스트를 추가하더라도 기존 stale 테스트에 가려 우선순위 판단이 흐려진다.

따라서 이번 라운드는 `Structural` 에만 집중한다.

## 현재 관찰 요약

테스트 자산 분포:

- 전체 테스트 파일: `68`
- `@DataJpaTest`: `42`
- `@SpringBootTest`: `8`
- `MockitoExtension`: `11`
- `ReflectionTestUtils` 사용 테스트: `3`

현재 직접 확인한 문제:

1. `ReservationUnitTest` 가 최신 `Reservation.createReservation(...)` 시그니처와 어긋나 `compileTestJava` 를 깨뜨린다.
2. `AvailabilityControllerIntegrationTest` 는 `@MockBean` deprecation warning 이 있다.
3. `ReservationConfirmRequestValidationTest`, `EasybookingApplicationTests`, `ReservationCreateStartAtDualWriteRedTest` 처럼 유지 가치 재평가가 필요한 테스트가 남아 있다.
4. `AvailabilityServiceTest` 와 일부 통합 테스트는 `ReflectionTestUtils` 로 내부 필드를 직접 조작한다.

## 이번 라운드 작업 범위

### Structural

- stale 테스트 시그니처 정리
- 테스트 이름/의도/가치가 어긋난 항목 정리
- deprecated 테스트 어노테이션 치환
- 전체 테스트 재실행

### 이번 라운드에서 하지 않을 것

- 서비스/도메인 동작 변경
- 권한/경계 버그 수정
- 새 비즈니스 규칙 추가

이 항목들은 기준선 복구 후 `Behavioral` 라운드에서 진행한다.

## 작업 순서

1. 깨진 테스트를 최신 프로덕션 계약에 맞춘다.
2. 저가치 테스트를 삭제 대신 우선 "승격 가능 여부" 기준으로 분류한다.
3. 경고 수준에서 바로 정리 가능한 항목은 함께 정리한다.
4. 전체 테스트를 다시 실행해 남은 실패를 수집한다.
5. 다음 `Behavioral` 우선순위를 문서에 기록한다.

## 진행 로그

### 2026-03-22 1차 스캔

- `study/study-tdd/프로젝트_TDD_적용_가이드라인.md` 를 기준 문서로 사용하기로 결정
- 기존 분석 문서들은 방향은 유효하지만, 현재 실패 지점은 다시 확인 필요하다고 판단
- 실제 실행으로 `ReservationUnitTest` compile failure 를 확인
- 이번 라운드는 `Structural` 기준선 복구를 먼저 수행하기로 결정

### 2026-03-22 Structural 기준선 복구

왜 이렇게 진행했는가:

- 전체 테스트가 깨진 상태에서는 의미 있는 `Behavioral` 테스트 추가가 어렵다.
- 기존 실패와 신규 실패가 섞이면 우선순위 판단이 흐려진다.
- 그래서 먼저 "테스트 스위트가 다시 실행되는 상태" 자체를 산출물로 삼았다.

어떻게 진행했는가:

1. `ReservationUnitTest` 를 최신 `Reservation.createReservation(...)` 시그니처에 맞게 수정했다.
2. `ReservationConfirmRequestValidationTest` 를 assertion 없는 역직렬화 시도에서 실제 필드 검증 테스트로 승격했다.
3. `ReservationCreateStartAtDualWriteRedTest` 의 `Red` 잔재 이름을 `ReservationCreateStartAtJpaMappingTest` 로 정리했다.
4. `AvailabilityControllerIntegrationTest` 의 `@MockBean` 을 `@MockitoBean` 으로 교체해 deprecation warning 을 줄였다.
5. 전체 테스트 재실행 중 `ObjectMapper` 의 Java Time 모듈 미등록으로 `ReservationConfirmRequestValidationTest` 가 실패해, 테스트 `ObjectMapper` 에 `findAndRegisterModules()` 를 적용했다.

결과:

- `./gradlew test` 통과
- `compileTestJava` 실패 해소
- 기존 분석 문서의 "현재 기준 실패 지점" 은 최신화가 필요하다는 점을 다시 확인

### 2026-03-22 Behavioral 1차 보강: `shopId` 경계

왜 이 항목을 첫 `Behavioral` 로 골랐는가:

- `ShopMenuManagementService.update()` 와 `deactivate()` 는 `shopId` 를 인자로 받지만 실제 조회에는 사용하지 않았다.
- 이 상태에서는 다른 매장의 메뉴도 `menuId` 만 알면 수정/비활성화가 가능하다.
- 테스트 품질 문제가 아니라 실제 경계 버그 가능성이므로 우선순위가 높다.

어떻게 진행했는가:

1. `ShopMenuManagementServiceTest` 에 "다른 `shopId` 로 수정/비활성화 시 실패" 테스트를 추가했다.
2. `ShopMenuReaderTest` 에 `shopId + menuId` 경계 조회 테스트를 추가했다.
3. RED 확인: `getByIdAndShopId(...)` 미존재로 컴파일 실패를 확인했다.
4. 최소 구현으로 `ShopMenuRepository.findByIdAndShopId(...)`, `ShopMenuReader.getByIdAndShopId(...)` 를 추가했다.
5. `ShopMenuManagementService.update()` / `deactivate()` 가 `menuId` 단독 조회 대신 `shopId + menuId` 조회를 사용하도록 바꿨다.
6. 관련 테스트만 먼저 실행한 뒤, 마지막에 전체 테스트를 다시 실행했다.

결과:

- 관련 테스트 통과
- `./gradlew test` 전체 통과 유지
- 메뉴 수정/비활성화에 매장 경계가 실제로 반영됨

## 이번 라운드 변경 요약

### Structural

- stale 테스트 시그니처 수정
- assertion 없는 테스트 보강
- `Red` 잔재 테스트명 정리
- deprecated test annotation 치환

### Behavioral

- `ShopMenuManagementService` 의 `shopId` 경계 보강
- 경계 회귀 방지 테스트 추가

## 다음 우선순위 후보

1. `ReflectionTestUtils` 제거
2. 저가치 테스트(`EasybookingApplicationTests` 등) 삭제 또는 승격
3. `shop` 계열 broad exception 정리
4. `reservation.service` 조회 테스트의 과도한 interaction coupling 완화
5. 긴 controller integration test 분해

### 2026-03-23 Structural 2차 정리: 리플렉션 제거와 저가치 테스트 정리

왜 이렇게 진행했는가:

- `ReflectionTestUtils` 는 내부 필드명에 테스트를 묶어 두기 때문에 리팩터링 저항이 크다.
- 비즈니스 설정 객체인 `ShopSettings` 는 테스트 우회를 허용하는 대신, 정상 메서드로 상태 변경을 표현하는 편이 더 낫다.
- 빈 `contextLoads()` 는 현재 스위트에서 보호하는 계약이 없어 유지 가치가 낮다.

어떻게 진행했는가:

1. `ShopSettings` 에 `updateBookingWindowDays(...)`, `updatePublicHolidayOff(...)` 메서드를 추가했다.
2. `AvailabilityServiceTest`, `HolidayCheckerTest`, `AvailabilityControllerIntegrationTest` 에서 `ReflectionTestUtils` 사용을 제거하고 정상 메서드 호출로 치환했다.
3. `ShopSettingsTest` 에 새 메서드에 대한 단위 테스트를 추가했다.
4. `EasybookingApplicationTests` 는 삭제했다.

중간에 새로 드러난 블로커:

- 전체 테스트 재실행 후 `shop/tag` 영역에서 4건이 실패했다.
- 이 실패는 이번 리플렉션 제거와 직접 관련된 문제가 아니라, 기존 로컬 변경이 반영된 `tag` 기능 테스트가 전체 스위트에 편입되면서 드러난 것이었다.

### 2026-03-23 후속 정리: `shop/tag` 테스트 블로커 해소

왜 이어서 처리했는가:

- 전체 스위트를 다시 녹색으로 만들지 못하면 이번 라운드 완료 기준이 불분명해진다.
- 실패 원인이 명확했고, 기존 변경을 되돌리지 않고도 최소 수정으로 해결 가능했다.

어떻게 진행했는가:

1. `TagControllerIntegrationTest`
   - `SpringBootTest` 공유 DB에서 전역 `Tag` 이름이 겹쳐 중복 제약이 터지는 문제를 확인했다.
   - 테스트별로 서로 다른 태그 이름을 사용하도록 조정했다.
2. `TagServiceTest`
   - `ShopTagRepository` 의 bulk update 후 정렬 조회가 stale 상태를 볼 수 있는 문제를 의심했다.
   - `@Modifying(flushAutomatically = true, clearAutomatically = true)` 를 적용해 정렬 갱신 직후 조회 일관성을 맞췄다.
3. `ShopTagBackfillMigrationTest`
   - Flyway 마이그레이션 직후 Hibernate `create-drop` 이 스키마를 다시 만들면서 backfill 데이터가 사라지는 것을 확인했다.
   - 해당 테스트에만 `spring.jpa.hibernate.ddl-auto=none` 을 줘서 Flyway 결과를 그대로 검증하게 바꿨다.

결과:

- 관련 `tag` 테스트 통과
- `./gradlew test` 전체 통과 유지

## 현재 상태

- `ReflectionTestUtils` 제거 완료
- 저가치 smoke test 제거 완료
- `shop/tag` 영역 전체 스위트 블로커 해소
- `./gradlew test` 전체 통과

## 다음 예상 산출물

- 테스트 스위트가 다시 실행되는 상태
- 바로 이어서 다룰 `Behavioral` 후보 목록
- 테스트 정비 우선순위가 반영된 후속 작업 메모
