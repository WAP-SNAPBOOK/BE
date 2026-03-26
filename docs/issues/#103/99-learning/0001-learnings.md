# #103 Learning Note

## 날짜

- 2026-03-01

## 오늘 배운 점

- BaseBusinessException 계층으로 예외를 통일하면 GlobalExceptionHandler에서 도메인 의도에 맞는 httpStatus/code/message를 일관되게 내려줄 수 있다.
- 도메인 예외(AvailabilityException 등)와 세부 예외(ShopSettingsNotFoundException 등)를 분리하면, 공통 처리와 실패 원인 표현을 동시에 확보할 수 있다.
- IllegalArgumentException 같은 일반 런타임 예외를 도메인 정책 검증에 사용하면, 현재 핸들러 구조에서 의도치 않게 공통 Exception 경로로 흘러 내부 서버 에러 처리될 위험이 있다.

## 의외의 사실/실수/교훈

- 단순히 "400으로 보이겠지"라고 가정하면 위험하다. 실제 핸들러 매핑을 확인하지 않으면 검증 실패가 500으로 노출될 수 있다.
- 예외 타입을 정할 때는 도메인 로직의 편의보다도 "API 계약(상태코드/에러코드)" 관점이 우선되어야 한다.
- 예외 클래스를 세분화할 때는 분기 가치가 있는 케이스만 추가해야 유지보수 복잡도를 통제할 수 있다.

## 현재 코드베이스 적용 현황

- 도메인 예외를 `catch`로 적극 활용하는 대표 사례는 `AuthException` 경로다.
- `JwtAuthenticationFilter`, `AuthService`, `JwtChannelInterceptor`에서 `AuthException`을 별도 분기 처리해 응답/로그를 안정적으로 제어하고 있다.
- `AvailabilityException`, `ShopException`, `ReservationException`은 주로 `throw`와 에러코드 표준화(및 글로벌 핸들러 처리)에 활용되고, 중간 계층 타입 자체를 `catch`하는 패턴은 현재 제한적이다.
- 즉, "중간 도메인 예외 계층"의 효용은 이미 존재하지만 도메인별 `catch` 전략은 도메인마다 채택 강도가 다르다.

## 관련 링크

- [TDD plan](../../04-tdd/plan.md)
- [Root cause & options](../../02-analysis/0001-root-cause-and-options.md)
- [Design plan](../../02-design/0001-design-plan.md)
