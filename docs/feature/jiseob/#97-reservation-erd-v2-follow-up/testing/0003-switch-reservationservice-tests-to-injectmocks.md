## #97 테스트 정리: `ReservationService` 단위 테스트를 `@InjectMocks`로 전환

- 날짜: 2026-02-09
- 브랜치: `feature/jiseob/#97-reservation-erd-v2-follow-up`
- 범위: `src/test/java/com/example/easybooking/reservation/service/*` 내 `ReservationService` 단위 테스트

---

## 1) 논리 흐름(요약)

- 현재 `ReservationService` 단위 테스트들은 `new ReservationService(...)`로 직접 생성한다.
- `ReservationService` 생성자 의존성이 늘어나면, 모든 테스트의 `setUp()`이 컴파일 에러로 연쇄 수정된다.
- Mockito의 `@InjectMocks`(생성자 주입 기반)로 전환하면, 이후 의존성 추가 시 대부분 “`@Mock`/`@Spy` 필드 1줄 추가”로 끝나도록 바꿀 수 있다.

---

## 2) 어떻게 변경할지 (설계/접근 방식)

### A. 테스트 생성 방식 전환

- 각 테스트 클래스에서 아래를 수행한다.
  - `new ReservationService(...)` 제거
  - `@InjectMocks private ReservationService reservationService;` 추가
  - 생성자 파라미터 중 “mock이 아닌 값 객체”는 `@Spy`로 제공한다.
    - 예: `ObjectMapper`는 `@Spy private ObjectMapper objectMapper = new ObjectMapper();`

### B. 왜 `@Spy ObjectMapper`가 필요한가

- Mockito가 `@InjectMocks`로 인스턴스를 생성할 때, `ReservationService` 생성자 파라미터를 테스트 클래스의 “주입 후보(대개 @Mock/@Spy)”에서 찾는다.
- `ObjectMapper`처럼 실제 인스턴스를 사용해야 하는 타입은 `@Spy`로 제공하는 편이 예측 가능하다.

---

## 3) 변경 근거 (이유, 장단점)

### 장점

- **생성자 변경 내성 증가**: `setUp()`에서 인자 순서를 맞추는 반복 수정이 사라진다.
- **테스트 보일러플레이트 감소**: 서비스 생성 코드 제거.
- **협력 객체 관계가 명확**: 필요한 mock/spy가 필드로 선언되어 “협력”이 드러난다.

### 단점/주의

- 생성자 의존성이 추가되면, 해당 타입의 `@Mock`/`@Spy` 필드를 **각 테스트 클래스에 추가**해야 할 수 있다.
- `@InjectMocks`는 주입 규칙(타입/이름/생성자 우선)에 의존하므로,
  - 새 의존성이 “real 인스턴스”가 필요한 타입이면 `@Spy`를 잊지 않도록 주의한다.

---

## 4) 코드 수정 예시 (diff 또는 예시 코드)

### Before

```java
@BeforeEach
void setUp() {
    reservationService = new ReservationService(
            reservationWriter,
            reservationReader,
            userReader,
            shopReader,
            staffReader,
            objectMapper,
            eventPublisher
    );
}
```

### After

```java
@Spy
private ObjectMapper objectMapper = new ObjectMapper();

@InjectMocks
private ReservationService reservationService;
```

---

## 5) 변경 대상(예정)

- `src/test/java/com/example/easybooking/reservation/service/ReservationServiceCreateReservationStaffIdValidationTest.java`
- `src/test/java/com/example/easybooking/reservation/service/ReservationServiceGetShopReservationUnitTest.java`
- `src/test/java/com/example/easybooking/reservation/service/ReservationServiceGetReservationsByCustomerInShopUnitTest.java`
- `src/test/java/com/example/easybooking/reservation/service/ReservationServiceGetMyReservationsUnitTest.java`
- `src/test/java/com/example/easybooking/reservation/service/ReservationServiceGetCustomerReservationInChatUnitTest.java`

---

## 6) 테스트 전략

- 변경 후 `./gradlew test` 실행으로 컴파일/런타임을 확인한다.
- 각 테스트의 기존 stubbing/verify는 유지되어야 한다(서비스 생성 방식만 변경).

