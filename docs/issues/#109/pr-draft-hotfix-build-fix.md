# `#109` 후속 핫픽스: `ShopMenuTagRepository` 누락 import 추가

## 배경

`#109` PR 머지 후 CI에서 `./gradlew build`가 `compileJava` 단계에서 실패했다.

원인은 `ShopMenuTagRepository`에 추가한 `@Query`, `@Modifying`, `@Param` 사용 코드에
필요한 Spring Data JPA import가 빠져 있었기 때문이다.

에러 요약:

- `cannot find symbol: class Query`
- `cannot find symbol: class Modifying`
- `cannot find symbol: class Param`

---

## 변경 사항

대상 파일:
- [ShopMenuTagRepository.java](C:/Users/User/Desktop/wjs/dev/project/SNAPBOOK_BE/BE/src/main/java/com/example/easybooking/shop/repository/ShopMenuTagRepository.java)

추가한 import:

- `org.springframework.data.jpa.repository.Modifying`
- `org.springframework.data.jpa.repository.Query`
- `org.springframework.data.repository.query.Param`

코드 동작 변경은 없고, 누락된 import만 보완한 컴파일 수정이다.

---

## 영향 범위

- `ShopMenuTagRepository` 컴파일 오류 해소
- `#109`에 포함된 메뉴-태그 브리지 / fallback 관련 repository 메서드 정상 컴파일
- 런타임 동작 변화 없음

---

## 테스트 / 검증

확인한 것:

- `./gradlew compileJava` 성공

이번 핫픽스에서는 비즈니스 로직 변경이 없어서 추가 테스트 코드는 없다.

---

## 리스크

- 사실상 없음
- import 누락 수정이라 동작 리스크보다 빌드 복구 목적이 명확한 변경이다

---

## 롤백

롤백 필요성은 낮다.

문제가 생기면 이 커밋만 revert 하면 된다.

---

## 비고

- 이 PR은 `#109` 본 변경의 후속 빌드 복구 PR이다.
- 본문 `#109` PR과 별도로, `develop` 기준 hotfix 브랜치에서 올린다.
