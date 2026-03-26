# `#107` `shop.domain` 테스트 분석

작성일: `2026-03-09`

## Context

분석 범위는 `src/test/java/com/example/easybooking/shop/domain` 이다.

검토 목적:

* `study/study-tdd/프로젝트_TDD_적용_가이드라인.md` 기준으로 현재 테스트가 계약 중심인지 확인
* 누락된 경계값/행위 테스트가 있는지 식별
* 별도 보강이 필요한 테스트 후보를 우선순위와 함께 정리

분석 대상:

* `src/test/java/com/example/easybooking/shop/domain/ShopMenuInputFieldTest.java`
* `src/test/java/com/example/easybooking/shop/domain/ShopMenuJpaMappingTest.java`
* `src/test/java/com/example/easybooking/shop/domain/ShopMenuTagJpaMappingTest.java`
* `src/test/java/com/example/easybooking/shop/domain/ShopMenuUniqueConstraintTest.java`
* `src/test/java/com/example/easybooking/shop/domain/TagJpaMappingTest.java`

실행 확인:

```bash
./gradlew test --tests "com.example.easybooking.shop.domain.*"
```

현재 대상 테스트는 모두 통과한다.

## 증상 / 징후

### 징후 1. JPA 슬라이스 테스트와 도메인 규칙 테스트가 한 클래스에 섞여 있다

근거:

* `ShopMenuInputFieldTest` 는 `@DataJpaTest` 클래스인데 매핑/unique 제약뿐 아니라 `create()` 의 경계값 예외까지 함께 검증한다.
* 가이드라인은 Domain 규칙은 DB 없이 먼저 테스트할 수 있으면 단위 테스트로 작성하고, JPA 테스트는 매핑/제약/쿼리에 집중하라고 정리한다.

영향:

* 실패 원인이 JPA 매핑 문제인지 도메인 규칙 문제인지 분리가 약하다.
* 리팩토링 시 테스트 이름과 실제 범위가 어긋나 해석 비용이 커진다.

### 징후 2. 허용 케이스 일부가 "예외 없음" 수준에서 끝난다

근거:

* `ShopMenuUniqueConstraintTest.save_allowsSameNameInDifferentShop()`
* `ShopMenuTagJpaMappingTest.save_allowsSameMenuDifferentTag()`
* `ShopMenuTagJpaMappingTest.save_allowsSameTagDifferentMenu()`

위 테스트들은 허용 여부는 보지만, 실제 저장된 row의 핵심 필드를 검증하지 않는다.

영향:

* 잘못된 row가 저장되거나 필드가 엉켜도 예외만 없으면 통과할 수 있다.
* 가이드라인의 "결과 개수뿐 아니라 핵심 필드까지 단언" 기준과 거리가 있다.

### 징후 3. 공개 행위 메서드 테스트가 비어 있다

근거:

* `ShopMenu.update()` / `ShopMenu.deactivate()` 테스트 부재
* `ShopMenuInputField.update()` / `ShopMenuInputField.deactivate()` 테스트 부재
* `Shop.create()` / `Shop.assignPublicCode()` / `Shop.updateSlug()` 가 `shop/domain` 테스트 폴더에서 아예 다뤄지지 않음

영향:

* 생성 시점 규칙만 보호되고, 실제 운영 중 자주 쓰일 수정/비활성화 행위는 회귀 방지망이 없다.
* 작은 리팩토링이나 검증 로직 추가 시 의도치 않은 상태 오염을 놓칠 수 있다.

### 징후 4. `ShopMenuInputField.update()` 는 실패 후 상태 오염 가능성이 있다

근거:

* `src/main/java/com/example/easybooking/shop/domain/ShopMenuInputField.java`
* `update()` 는 값을 먼저 대입하고 마지막에 `validate()` 를 호출한다.

이 구조에서는 잘못된 `minValue/maxValue`, `stepValue`, `maxLength` 입력이 들어오면 예외는 던지더라도 중간 상태가 이미 바뀌었을 가능성이 있다.

영향:

* "실패하면 상태가 유지된다"는 도메인 계약이 깨질 수 있다.
* 현재 테스트는 이 리스크를 전혀 잡지 못한다.

## 영향 범위

도메인:

* `ShopMenu`, `ShopMenuInputField`, `ShopMenuTag`, `Tag`, `Shop`

테스트 유지보수:

* JPA 문제와 도메인 규칙 문제가 한 클래스에 섞여 있어 실패 해석 비용 증가
* 허용 케이스 결과 검증 부족으로 회귀 탐지력 약화

추가 구현 리스크:

* `update()` / `deactivate()` 리팩토링 시 보호 장치 부족
* `Shop` 공개 API에 대한 회귀 탐지 공백

## 리팩토링 / 테스트 보강 후보

### 1순위. 공개 행위 계약을 먼저 고정

대상:

* `ShopMenuInputField.update()` / `deactivate()`
* `ShopMenu.update()` / `deactivate()`
* `Shop.create()` / `assignPublicCode()` / `updateSlug()`

추가 권장 테스트:

* `update_숫자필드의값을변경하면_관련필드만갱신된다`
* `update_텍스트필드의maxLength를변경하면_관련필드만갱신된다`
* `update_잘못된숫자범위면_예외`
* `update_실패하면_기존상태를유지한다`
* `deactivate_호출시_isActive가false가된다`
* `update_이름만변경하면_설명과정렬순서는유지된다`
* `update_null값은_기존값을유지한다`
* `create_요청값으로상점을생성한다`
* `assignPublicCode_호출시_publicCode가변경된다`
* `updateSlug_호출시_slug가변경된다`

우선순위 근거:

* JPA 매핑보다 실제 비즈니스 회귀를 막는 효과가 크다.
* 특히 `ShopMenuInputField.update()` 는 현재 구현 결함 가능성이 있어 즉시 가치가 있다.

### 2순위. 허용 케이스를 결과 검증으로 강화

대상:

* `ShopMenuUniqueConstraintTest`
* `ShopMenuTagJpaMappingTest`
* `TagJpaMappingTest`

추가 권장 테스트:

* `save_allowsSameNameInDifferentShop` 이후 `extracting(shopId, name)` 으로 두 row 확인
* `save_allowsSameMenuDifferentTag` 이후 `(shopMenuId, tagId)` 조합 두 건 확인
* `save_allowsSameTagDifferentMenu` 이후 `(shopMenuId, tagId)` 조합 두 건 확인
* 중복이 아닌 다른 `Tag` 저장 후 row 개수와 이름 목록 확인

우선순위 근거:

* 현재 테스트 의도를 유지하면서도 계약 강도를 빠르게 높일 수 있다.

### 3순위. 매핑 테스트와 단위 테스트의 범위를 분리

대상:

* `ShopMenuInputFieldTest`

권장 방향:

* JPA 매핑/unique 제약은 `XxxJpaMappingTest` 성격으로 남긴다.
* `create()` / `update()` / `deactivate()` 규칙은 DB 없는 `UnitTest` 로 분리한다.

우선순위 근거:

* 기능 추가 전에 당장 필수는 아니지만, 테스트 실패 원인 분리와 가독성에 도움이 된다.

## 지금 안 하면 생기는 비용

* `ShopMenuInputField.update()` 의 상태 오염 가능성이 실제 버그로 번질 수 있다.
* `ShopMenu`, `Shop` 의 공개 API가 리팩토링 과정에서 조용히 깨져도 테스트가 잡지 못한다.
* 허용 케이스가 결과 검증 없이 남아 있으면 JPA 회귀를 늦게 발견한다.
* JPA 테스트와 도메인 테스트가 계속 섞이면 이후 보강 작업마다 테스트 구조를 다시 해석해야 한다.

## 결론

현재 `shop.domain` 테스트는 "기본 매핑과 일부 생성 규칙은 커버하지만, 공개 행위 계약과 회귀 방지력이 아직 약한 상태"로 보는 것이 정확하다.

추천 순서는 다음과 같다.

1. `ShopMenuInputField.update()` / `ShopMenu.update()` / `Shop` 공개 API 테스트 추가
2. 허용 케이스를 저장 결과 검증으로 강화
3. `ShopMenuInputFieldTest` 를 JPA 테스트와 단위 테스트로 분리
