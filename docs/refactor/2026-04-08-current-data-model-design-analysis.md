# 현재 데이터 모델 설계 과정 분석

작성일: `2026-04-08`

## 목적

이 문서는 현재 저장소의 데이터 모델과 관련 문서, 마이그레이션, 엔티티 코드를 함께 읽고
"왜 이런 식으로 설계되었는가", "어떤 대안을 두고 어떤 선택을 했는가", "지금 모델이 드러내는 설계 성향은 무엇인가"를 역추적해 정리한다.

중요한 전제:

- 이 문서는 작성자의 머릿속 의도를 직접 들은 기록이 아니라,
  저장소에 남아 있는 산출물로부터 설계 의도를 추론한 분석 문서다.
- 따라서 일부 내용은 "사실"이 아니라 "근거 기반 해석"이다.

## 분석 근거

### 핵심 문서

- `docs/issues/#97/reservation-erd-v2.md`
- `docs/issues/#97/reservation-erd-v2-modeling-process.md`
- `docs/issues/#97/02-analysis/0001-root-cause-and-options.md`
- `docs/issues/#97/03-adr/0001-adr-occupancy-granularity-10min.md`
- `docs/issues/#97/03-adr/0002-adr-confirm-reschedule-at-confirm.md`
- `docs/issues/#97/03-adr/0004-adr-time-granularity-10m-vs-30m.md`
- `docs/issues/#99/02-design/design-plan.md`
- `docs/issues/#101/03-adr/adr-101-availability.md`
- `docs/issues/#109/0001-github-issue-draft.md`
- `docs/issues/#109/work-log.md`
- `docs/issues/#103/02-analysis/0001-domain-model-change-and-flyway-adoption-summary.md`
- `docs/issues/#999/0001-root-cause-and-options.md`

### 핵심 마이그레이션

- `src/main/resources/db/migration/V1__reservation_v2_phase1_add_tables_and_columns.sql`
- `src/main/resources/db/migration/V2__reservation_v2_phase2_backfill_start_at_and_staff.sql`
- `src/main/resources/db/migration/V3__reservation_v2_phase3_constraints_and_indexes.sql`
- `src/main/resources/db/migration/V4__rename_shop_services_to_shop_menus.sql`
- `src/main/resources/db/migration/V5__shop_availability_settings.sql`
- `src/main/resources/db/migration/V6__shop_tags.sql`
- `src/main/resources/db/migration/V7__shop_menu_tags_add_shop_tag_id.sql`
- `src/main/resources/db/migration/V8__backfill_shop_tags.sql`
- `src/main/resources/db/migration/V9__drop_reservations_form_data_json.sql`
- `src/main/resources/db/migration/V10__add_reservations_requirements.sql`

### 핵심 코드

- `src/main/java/com/example/easybooking/reservation/domain/Reservation.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationTimeBlock.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationMenuItem.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationMenuInputValue.java`
- `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
- `src/main/java/com/example/easybooking/reservation/ReservationTimeBlockWriter.java`
- `src/main/java/com/example/easybooking/reservation/TimeBlockGenerator.java`
- `src/main/java/com/example/easybooking/shop/domain/ShopMenu.java`
- `src/main/java/com/example/easybooking/shop/domain/ShopMenuInputField.java`
- `src/main/java/com/example/easybooking/shop/domain/ShopTag.java`
- `src/main/java/com/example/easybooking/shop/domain/ShopMenuTag.java`
- `src/main/java/com/example/easybooking/shop/service/TagService.java`
- `src/main/java/com/example/easybooking/availability/domain/ShopSettings.java`
- `src/main/java/com/example/easybooking/availability/domain/ShopOperatingTime.java`
- `src/main/java/com/example/easybooking/availability/domain/StaffOperatingTime.java`
- `src/main/java/com/example/easybooking/availability/AvailabilityService.java`
- `src/main/java/com/example/easybooking/availability/OperatingTimeResolver.java`
- `src/main/java/com/example/easybooking/slot/Slot.java`

## 한눈에 보는 결론

현재 모델은 "처음부터 이상적인 최종 ERD를 한 번에 구현한 모델"이라기보다,
아래 원칙에 따라 점진적으로 진화한 모델로 읽힌다.

1. 가장 먼저 보호해야 할 것은 예약 무결성이다.
2. 무결성은 가능하면 DB 제약으로 막는다.
3. 레거시 데이터를 가진 상태에서 빅뱅 전환은 피한다.
4. 과거 예약의 의미는 원본 데이터와 분리해 스냅샷으로 보존한다.
5. 실제 제품 의미가 generic 모델과 충돌하면, generic 모델보다 제품 의미를 택한다.

즉 이 설계는 "우아한 정규형만 추구한 결과"가 아니라
"운영 중 깨지면 안 되는 규칙"과 "점진 전환의 현실"이 강하게 반영된 결과다.

## 설계 진화 과정

### 1. 초기 상태: 레거시 예약 모델

초기 모델의 흔적은 아직 코드에 남아 있다.

- `Reservation`은 여전히 `date`, `time`, `startAt`를 함께 가진다.
- 과거에는 예약 입력이 `formDataJson` 같은 비정형 구조에 많이 기대고 있었다.
- `Slot` 엔티티도 별도로 남아 있다.

이 상태는 다음 특징을 가졌던 것으로 보인다.

- 예약 자체는 단순히 저장되지만,
  직원 단위 스케줄 무결성은 강하게 보장하지 못했다.
- 폼/메뉴/추가 입력이 구조화되지 않아
  조회, 검증, 필터링, 이력 보존이 약했다.
- "예약 가능 시간"도 물리 슬롯 모델과 실제 예약 모델이 분리될 위험이 있었다.

### 2. `#97`: 예약 ERD v2 도입

`#97`은 현재 데이터 모델의 가장 큰 분기점이다.

이 시점에 도입된 핵심은 아래다.

- `staff`
- `start_at`
- `duration_minutes`
- `reservation_time_blocks`
- 메뉴/입력값/이력용 신규 테이블군

중요한 점은 이 도입 방식이 `add-only -> backfill -> constraints` 순서라는 것이다.

#### 이 순서를 택한 이유

- 운영 중인 기존 데이터와 API를 바로 깨지 않기 위해
- 먼저 새 구조를 추가하고
- 그 다음 기존 데이터를 옮기고
- 마지막에 제약을 강화하는 것이 가장 안전하기 때문

이 방식은 설계자가 "정합성"뿐 아니라 "전환 절차"를 설계 대상으로 보고 있었다는 신호다.

### 3. `#99`: 메뉴/입력값을 JSON에서 정형 모델로 이동

`#99`에서 메뉴, 추가 입력, 태그 필터링이 구조화된다.

핵심 변화:

- `shop_services` 계열이 `shop_menus` 계열로 리네이밍
- `reservation_services`가 `reservation_menu_items`로 전환
- 예약 시점 메뉴/입력값 스냅샷 도입
- 기존 `formDataJson` 의존을 단계적으로 축소

즉 `#97`이 예약 시간축의 무결성을 잡았다면,
`#99`는 예약 내용의 의미를 정형 데이터로 끌어올린 작업이다.

### 4. `#101`: 가용성을 계산형 모델로 분리

`#101`에서는 "예약 가능 시간"이 별도 도메인으로 분리된다.

핵심 변화:

- `shop_settings`
- `shop_operating_times`
- `staff_operating_times`
- `shop_holidays`
- `public_holidays`

중요한 결정은 "슬롯을 저장하지 않고 계산한다"는 것이다.

이는 `reservation_time_blocks`를 이미 점유 SSOT로 둔 구조와 맞물린다.

### 5. `#109`: 태그의 정체성을 전역에서 매장 로컬로 재정의

처음에는 `Tag`가 전역 dictionary처럼 설계돼 있었다.
하지만 실제 제품 의미는 "매장 내부 분류값"에 더 가까웠다.

그래서 나온 변화가 아래다.

- `shop_tags` 도입
- `shop_menu_tags.shop_tag_id` 추가
- backfill
- 과도기 fallback 유지

이 변화는 단순 정렬 추가가 아니라,
태그의 정체성 자체를 바꾸는 모델 전환이다.

### 6. `#105`: 과도기 계약 정리

이 단계에서는 `form_data_json` 제거, `requirements` 영속화 같은 정리 작업이 들어간다.

이것은 앞선 큰 구조 전환 이후,
남아 있던 계약 불일치를 정리하는 성격이 강하다.

## 이 설계가 드러내는 핵심 원칙

## 1. 예약 무결성은 애플리케이션보다 DB 제약이 최후 방어선이다

가장 강한 신호는 `reservation_time_blocks`와
`UNIQUE(staff_id, block_start_at)`이다.

관련 근거:

- `ReservationTimeBlock`
- `ReservationTimeBlockWriter`
- `TimeBlockGenerator`
- `V1`, `V3`
- `#97` ADR 문서들

설계 의도는 명확하다.

- `PENDING`은 접수일 뿐 점유가 아니다.
- 실제 충돌 금지는 `CONFIRMED`에서만 발생한다.
- 충돌 금지는 "조회 후 저장" 수준에서 끝내지 않고
  최종적으로 DB 유니크가 막아야 한다.

이 설계가 나온 이유는 아마 아래와 같다.

- 예약 겹침은 비즈니스 핵심 오류다.
- 동시성 상황에서 앱 레벨 검사만으로는 불충분하다.
- MySQL 환경에서 범위 겹침을 깔끔하게 유니크로 강제하기 어려우니,
  범위를 10분 블록 집합으로 바꿔 유니크 문제로 치환했다.

### 이때 고려되었을 가능성이 높은 대안

#### 대안 A. 범위 겹침 쿼리 + 트랜잭션 락

예:

- 확정 전에 겹치는 예약이 있는지 조회
- 있으면 실패
- 없으면 저장

장점:

- 블록 테이블이 없어도 됨
- 데이터 양이 적어 보임

단점:

- 동시성에서 race condition 방어가 어려움
- 락 전략이 복잡해짐
- DB별 구현 편차가 큼

기각 이유로 추정되는 점:

- "절대 안 겹쳐야 하는 규칙"을 앱 코드만으로 맡기기 싫었던 것

#### 대안 B. 물리 슬롯 테이블을 미리 생성

예:

- 10분 또는 30분 슬롯 row를 미리 만들어 두고 예약 시 점유

장점:

- 점유 상태를 눈으로 보기 쉬움
- 조회 모델이 단순해질 수 있음

단점:

- 운영시간/휴무/직원별 일정과 별도의 이중 소스가 생김
- 생성/정리 배치가 필요
- 현재 `Slot` 엔티티가 보여주듯 오히려 모델이 분산될 수 있음

기각 이유로 추정되는 점:

- `reservation_time_blocks` 하나면 충분한데,
  슬롯까지 저장하면 진실원이 두 개가 되기 때문

#### 대안 C. PostgreSQL exclusion constraint 같은 DB 특화 기능

장점:

- 시간 범위 겹침을 더 자연스럽게 표현 가능

단점:

- 현재 스택과 맞지 않음
- MySQL 호환성이 떨어짐

## 2. 전환 과정 자체가 설계의 일부다

`V1 -> V2 -> V3`의 순서는 우연이 아니다.

- `V1`: 새 컬럼/테이블 추가
- `V2`: 기존 데이터 백필
- `V3`: 제약/인덱스 강화

이 방식은 "최종 구조"보다 "안전한 이동 경로"를 먼저 설계한 것이다.

즉, 작성자는 아래를 중요하게 본다.

- 기존 데이터를 버리지 않는다.
- 현재 API를 한 번에 부수지 않는다.
- 과도기 중에도 시스템은 계속 동작해야 한다.

이는 `ShopMenuTag`의 `tag_id + shop_tag_id` 공존,
`Reservation`의 `date/time + startAt` 공존,
`formDataJson` 제거 전 additive 계약 유지에서도 반복된다.

## 3. 정규화는 하되, 이력 보존이 필요한 곳은 스냅샷으로 끊는다

메뉴/입력값 쪽이 대표적이다.

- `ReservationMenuItem.menuNameSnapshot`
- `ReservationMenuInputValue.fieldLabelSnapshot`
- `ReservationMenuInputValue.inputTypeSnapshot`

이 구조는 "원본 메뉴를 나중에 바꾸더라도 예약의 의미는 바뀌지 않는다"는 원칙을 구현한다.

이는 아래 현실을 반영한 선택으로 보인다.

- 메뉴 이름은 바뀔 수 있다.
- 입력 필드 라벨도 바뀔 수 있다.
- 비활성화나 soft delete도 가능하다.
- 하지만 과거 예약 상세는 당시 선택 내용을 그대로 보여줘야 한다.

### 가능한 대안

#### 대안 A. 원본 메뉴/입력필드를 항상 live join

장점:

- 저장 중복이 적음
- 모델이 더 정규형처럼 보임

단점:

- 과거 예약의 의미가 현재 메뉴 상태에 오염됨
- 이름 변경, 삭제, 비활성화에 취약

#### 대안 B. 예약 전체를 JSON으로 저장

장점:

- 매우 유연함
- 구조 변경에 강해 보임

단점:

- 검증이 약함
- 필터링/조회가 약함
- 입력 필드별 무결성 강제가 어려움

현재 설계는 이 둘의 절충이 아니라,
"정의는 정규화, 이력은 스냅샷"을 택한 셈이다.

## 4. generic 모델보다 제품 의미를 우선한다

태그 모델이 가장 분명한 사례다.

처음에는 전역 `Tag`가 있었지만,
실제 요구사항은 전역 taxonomy가 아니라 "매장 내부 분류"였다.

그래서 `shop_tags`가 도입된다.

핵심 의미:

- 같은 이름 태그가 다른 매장에서 공존 가능
- 정렬 순서는 매장 내부 의미만 가짐
- 태그 수정/정렬/연결은 매장 경계 안에서 닫혀야 함

이건 단순 구현 변경이 아니라
"태그란 무엇인가"를 다시 정의한 것이다.

### 가능한 대안

#### 대안 A. 전역 태그 + 매장별 순서 보조 테이블

장점:

- 기존 전역 태그 모델을 많이 재사용 가능

단점:

- 태그의 소속성 문제를 해결하지 못함
- 같은 이름 태그의 교차 매장 독립성이 깨짐

#### 대안 B. 처음부터 전역 태그 완전 제거

장점:

- 모델이 더 깔끔함

단점:

- 기존 데이터/코드와의 전환 비용이 큼
- backfill과 fallback 없이 바로 전환하기 어려움

그래서 현재는 `tag_id`와 `shop_tag_id`가 함께 있는 과도기 모델이 됐다.

## 5. 계산 가능한 것은 저장하지 않는다

가용성 모델이 이 원칙을 보여준다.

- 운영시간, 휴무일, 간격, 예약 가능 기간 같은 규칙을 저장
- 실제 일별/월별 가용 슬롯은 조회 시 계산
- 점유 여부는 `reservation_time_blocks`에서 차감

이는 `ADR-101`의 핵심 결정과 일치한다.

### 가능한 대안

#### 대안 A. 물리 슬롯 저장

장점:

- 조회는 단순
- 디버깅이 직관적일 수 있음

단점:

- 점유 SSOT와 중복
- 운영시간 변경 시 대량 갱신 필요
- 휴무/오버라이드 반영 비용 큼

#### 대안 B. JSON 일정 저장

장점:

- 구현이 빨라 보임

단점:

- 무결성, 질의, 확장성, 검증이 약함

현재 선택은 "규칙은 정규화된 row로 저장, 결과는 계산"이다.

## 도메인별 상세 해석

## A. 예약 핵심 모델

### 왜 `Reservation`과 `ReservationTimeBlock`을 분리했는가

`Reservation`은 업무 수명주기를 나타내고,
`ReservationTimeBlock`은 점유 사실을 나타낸다.

둘을 분리한 이유는 아래 두 성격이 다르기 때문이다.

- 예약:
  - 접수, 확정, 거절, 취소의 흐름
- 점유:
  - 특정 직원의 특정 시각이 막혔는지 여부

이 분리가 없으면 `PENDING` 중복 허용과 `CONFIRMED` 충돌 금지를
같은 row에서 표현하기 어려워진다.

### 왜 10분 단위인가

`#97` ADR에서 직접 드러나는 이유는 다음과 같다.

- 1분 단위는 너무 비쌈
- 30분 단위는 정확도가 부족함
- 10분은 정확도와 비용의 중간점

즉 "모델의 아름다움"보다 "운영 가능한 해상도"가 선택 기준이었다.

### 왜 `date/time`를 아직 남겨두었는가

이는 최종 모델 관점에서는 덜 깔끔하다.
하지만 전환 과정 관점에서는 이해 가능하다.

가능한 이유:

- 기존 API/응답이 `date`, `time`에 기대고 있었음
- 기존 쿼리/테스트/프론트 계약이 한 번에 바뀌기 어려웠음
- 그래서 `startAt`를 추가하되, 기존 필드와 일정 기간 공존시킴

즉 이 부분은 이상적 설계보다 전환 비용을 택한 흔적이다.

## B. 메뉴 / 입력값 모델

### 왜 `ShopMenuInputField`가 별도 테이블인가

입력 필드는 단순 메타데이터가 아니라 검증 규칙을 가진다.

- `NUMBER`
  - `min/max/step`
- `TEXT`
  - `maxLength`

이를 별도 테이블로 둔 것은 아래 목적 때문이다.

- 메뉴별 추가 입력 규칙을 서버가 이해하고 검증
- 예약 시점에 해당 규칙의 의미를 스냅샷으로 복사
- 향후 메뉴 관리 UI와 예약 입력 UI를 같은 모델 위에 올림

### 왜 `key`를 버리고 `label` 중심으로 단순화했는가

`V4`에서 `key`, `field_key_snapshot`이 제거된다.

이 결정은 아래 해석이 가능하다.

- 지나치게 generic한 식별자 체계보다
  현재 제품에 필요한 단순성을 택했다.
- 실제 운영에서 label 중심으로 충분하다고 판단했다.
- 모델을 너무 "폼 엔진"처럼 만들지 않으려 했다.

대신 이 결정의 비용도 있다.

- 장기적으로 label 변경과 안정 식별자 문제를 다시 만나기 쉽다.

즉 이 부분은 확장성보다 단순성을 택한 지점이다.

## C. 가용성 모델

### 왜 `shop_settings`와 `shop_operating_times`를 분리했는가

설정과 시간 규칙의 성격이 다르기 때문이다.

- `shop_settings`
  - interval
  - booking window
  - lead time
  - schedule type
- `shop_operating_times`
  - 실제 요일별 운영 범위

이 분리는 조회와 수정 경계를 나누는 데도 유리하다.

### 왜 직원 운영시간은 "요일별 단일 override"부터 시작했는가

`StaffOperatingTime`은 `(staff_id, day_of_week)` 유니크다.
`OperatingTimeResolver`에는 "복수 운영 시간을 아직 반영 못함" TODO도 남아 있다.

이 구조는 아래 판단을 드러낸다.

- 직원 오버라이드는 필요하지만,
  매장 기본 운영시간만큼 복잡한 모델이 당장 필수는 아니었다.
- 우선은 "해당 요일 off" 또는 "단일 override 시간대" 정도면 충분하다고 봤다.

즉 이 영역은 처음부터 완전 모델을 만든 것이 아니라,
가장 작은 실용 모델로 시작한 상태다.

### 왜 예약 단위는 10분인데 가용 간격은 30/60분인가

이 부분은 처음 보면 모순처럼 보인다.
하지만 실제로는 역할이 다르다.

- 10분:
  - 점유의 최소 해상도
  - 충돌 방지의 단위
- 30/60분:
  - 고객에게 노출하는 예약 슬롯 단위
  - 운영 정책 단위

즉 내부 무결성 해상도와 외부 UX 간격을 분리한 것이다.

## D. 태그 모델

### 왜 `ShopTag`를 따로 두었는가

이는 "태그가 전역 지식인가, 매장 로컬 분류인가"라는 질문에 대한 답이다.

현재 설계는 후자를 택했다.

이 선택의 장점:

- 매장 간 독립성 확보
- 정렬을 태그 자체 속성으로 보관 가능
- 소유권 검증이 자연스러움

이 선택의 비용:

- 전환 기간 동안 bridge가 필요
- `tag_id`, `shop_tag_id` 공존으로 복잡도 증가

### 왜 fallback을 남겨두었는가

`TagService`는 여전히 legacy 경로를 관측 가능 상태로 남겨둔다.

이는 cleanup을 늦춘 게 아니라,
cleanup 전에 안전장치를 둔 것이다.

가능한 이유:

- 실제 프론트/기존 데이터가 legacy 경로를 아직 쓸 수 있음
- backfill 후에도 모든 호출이 새 경로로 정렬됐다고 바로 가정하기 어려움
- 따라서 "바로 삭제"보다 "deprecated + warn log + fallback"이 안전함

## 설계 과정 관점에서 본 특징

## 1. 문서가 구현을 끌고 가는 편이다

`#97`, `#99`, `#101`, `#109` 모두
ERD, ADR, TDD plan, worklog가 강하게 남아 있다.

즉 이 저장소의 설계는
"코드를 짠 뒤 문서화"보다
"문서로 정책을 먼저 굳히고 코드로 옮김"에 가깝다.

특히 모호한 정책은 ADR로 먼저 고정된다.

예:

- 10분 vs 30분
- confirm 시 reschedule
- 계산형 availability
- 매장 로컬 태그

## 2. Structural과 Behavioral을 의식적으로 나누려는 경향이 있다

문서와 마이그레이션 흐름을 보면,
구조를 먼저 깔고 동작을 뒤에서 따라오게 하는 패턴이 반복된다.

예:

- `V1`: 새 구조 추가
- `V2`: 데이터 이동
- `V3`: 제약 강화
- 그 뒤 코드 전환

즉 모델 설계가 단순 도메인 모델링이 아니라
"이행 순서 설계"와 붙어 있다.

## 3. 객체 그래프보다 명시적 FK + 서비스 조합을 선호한다

현재 엔티티는 JPA 관계를 풍부하게 맺기보다,
대부분 ID를 직접 들고 서비스 계층이 Reader/Writer를 조합한다.

이 선택은 다음과 연결된다.

- 과도기 컬럼/테이블 공존이 많음
- 스냅샷과 원본이 분리됨
- 마이그레이션과 롤백을 자주 의식해야 함

즉 "풍부한 aggregate 모델"보다
"전환기에도 다루기 쉬운 persistence model"을 선호한 것으로 보인다.

## 현재 모델이 가진 강점

1. 예약 충돌 방지가 DB 레벨까지 내려가 있다.
2. 과거 예약의 의미를 스냅샷으로 보존한다.
3. 메뉴/입력값/태그/가용성이 서로 분리된 하위 도메인으로 정리돼 있다.
4. 레거시와 신규 모델 사이 이동 경로가 문서와 마이그레이션에 비교적 선명하다.
5. 실제 제품 의미가 바뀌었을 때 모델도 같이 수정한다.

## 현재 모델이 가진 약점과 기술부채

## 1. 전환기 상태가 아직 남아 있다

대표 사례:

- `Reservation.date/time/startAt` 공존
- `ShopMenuTag.tagId/shopTagId` 공존
- deprecated legacy API와 fallback 경로 잔존

이는 의도된 과도기 설계지만,
장기적으로는 읽기 비용과 버그 가능성을 높인다.

## 2. 스키마가 코드보다 앞서간 부분이 있다

`V1`에는 `reservation_status_histories`, `reservation_change_histories`가 들어갔지만,
현재 코드에서 이력이 도메인 핵심 흐름으로 완전히 쓰이고 있지는 않다.

즉 일부 구조는 미래 필요를 보고 먼저 들어갔고,
애플리케이션은 아직 그만큼 따라오지 않았다.

## 3. 메뉴 가격 모델은 비어 있는데 가격 스냅샷 슬롯은 있다

`#999` 문서가 지적하듯,
`ReservationMenuItem.priceSnapshot`은 있는데 `ShopMenu.price` 원본 모델은 없다.

이는 "스냅샷 사고방식"은 먼저 도입됐지만
"원본 가격 모델"은 아직 비어 있는 불균형 사례다.

## 4. generic 모델과 제품 모델이 섞여 남아 있다

예:

- `Slot` 엔티티 잔존
- `Form` 도메인 잔존
- 메뉴 입력 필드는 제품 모델로 정리됐지만
  과거 generic form 모델의 흔적이 완전히 사라지진 않음

즉 일부 영역은 이미 제품 맞춤 모델로 옮겨갔고,
일부 영역은 레거시 generic 모델이 잔존한다.

## 5. 가용성 모델도 아직 완전한 최종형은 아니다

`OperatingTimeResolver`의 TODO가 보여주듯,
직원 복수 운영시간 같은 요구는 아직 모델이 충분히 흡수하지 못했다.

즉 이 영역은 방향은 맞지만,
아직 1차 practical model 수준이다.

## 만약 처음부터 다시 설계했다면 가능한 대안들

| 영역 | 현재 선택 | 가능한 대안 | 대안의 장점 | 대안의 큰 문제 |
|------|-----------|-------------|-------------|----------------|
| 예약 충돌 | `reservation_time_blocks` + DB unique | 범위 겹침 쿼리 + 락 | 구조가 적어 보임 | 동시성 방어가 어려움 |
| 예약 충돌 | `reservation_time_blocks` + 10분 블록 | 물리 슬롯 선생성 | 조회가 직관적 | 진실원 이중화 |
| 예약 입력 | 정규화 + 스냅샷 | JSON 중심 모델 | 유연함 | 검증/필터링/이력 약함 |
| 메뉴 입력 필드 | 제품 맞춤 테이블 | generic form/EAV | 범용성 높음 | 읽기/검증 복잡도 큼 |
| availability | 규칙 저장 + 계산 | 슬롯 저장 | 조회 단순 | 배치/동기화 비용 큼 |
| 태그 | 매장 로컬 태그 | 전역 태그 + order table | 초기 변경 적음 | 제품 의미와 어긋남 |
| 전환 방식 | additive -> backfill -> constraints | big-bang migration | 최종 모델이 빠르게 깔끔해짐 | 운영 리스크 큼 |

## 이 설계가 말해주는 작성자의 설계 스타일

저장소 흔적만 보고 요약하면, 현재 설계 스타일은 아래에 가깝다.

### 1. 무결성 우선

특히 예약 시간 충돌처럼 사업 핵심 규칙은
서비스 코드가 아니라 DB 제약으로 내리려는 성향이 강하다.

### 2. 점진 전환 선호

한 번에 갈아엎기보다,
레거시를 잠시 공존시키더라도 이동 경로를 확보하는 쪽을 택한다.

### 3. 과도한 generic화 경계

실제품 의미가 명확해지면
generic form, global tag 같은 구조보다
`shop_menu_input_fields`, `shop_tags`처럼 제품 의미가 드러나는 모델로 옮긴다.

### 4. 문서 기반 결정

정책이 흔들릴 수 있는 지점은
ADR, design plan, worklog로 먼저 고정하는 편이다.

### 5. 완벽한 최종형보다 현재 필요한 최소 실전형

예:

- 직원 운영시간 override는 단일 row부터 시작
- 태그는 fallback을 남긴 채 전환
- `date/time/startAt`도 즉시 제거하지 않음

즉 "처음부터 가장 아름다운 완성형"보다
"지금 운영과 구현에 필요한 가장 작은 안정 구조"를 선호한다.

## 권장 정리 우선순위

이 분석을 바탕으로 현재 모델을 더 정리하려면,
우선순위는 아래 순서가 자연스럽다.

### 1. 전환 완료가 가까운 bridge 제거

- `ShopMenuTag.tagId` cleanup 시점 결정
- legacy tag API 제거
- `Reservation.date/time`와 `startAt` 정리 시점 결정

### 2. 스키마 선도입 영역을 실제 동작과 정렬

- 상태 이력 / 변경 이력 실제 사용 여부 확정
- 사용하지 않을 구조는 문서상 보류로 명시

### 3. 모델 불균형 해소

- `ShopMenu.price` 원본 모델 도입 여부 결정
- snapshot 필드와 원본 필드의 책임을 정렬

### 4. availability 보강

- 직원 복수 운영시간 필요 여부 확정
- interval 정책과 내부 10분 해상도 설명을 문서에 더 명확히 남김

## 최종 결론

현재 데이터 모델은 단순히 "내가 떠오르는 대로 엔티티를 늘린 결과"로 보이지 않는다.
오히려 다음 질문들에 대한 반복된 답변의 축적에 가깝다.

- 예약 충돌을 어떻게 절대 안 나게 할 것인가
- 레거시 데이터를 가진 상태에서 어떻게 안전하게 옮길 것인가
- 과거 예약 의미를 원본 변경과 분리해 어떻게 보존할 것인가
- 실제 제품 의미가 generic 모델과 다를 때 무엇을 기준으로 삼을 것인가

그래서 이 모델의 본질은 "정규화된 예쁜 ERD"가 아니라
"운영 무결성 + 단계적 전환 + 도메인 의미 보존"을 우선한 실전형 진화 모델이라고 정리할 수 있다.

이 문서를 한 문장으로 압축하면 아래와 같다.

> 현재 설계는 이상적인 최종형을 한 번에 구현한 것이 아니라,
> 예약 무결성을 DB 제약으로 먼저 고정하고,
> 레거시와 신규 모델을 안전하게 연결하면서,
> 제품 의미가 분명해지는 부분부터 점진적으로 정형화한 결과다.
