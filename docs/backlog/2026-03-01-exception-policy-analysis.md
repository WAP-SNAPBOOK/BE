# 예외 정책 정합성 분석 (도메인 예외 vs 세부 예외 vs ErrorCode)

## 메타
- 날짜: 2026-03-01
- 관련 이슈: `#103`
- 관련 문서: `docs/issues/#103/04-tdd/plan.md`

## 문제 재정의 (한 문장)
기존 운영 방식은 `도메인 예외 + ErrorCode 직접 사용`이었는데, 신규 요구사항(availability/staff)에서 세부 예외 클래스가 추가되며 예외 모델링이 혼재되었다.

## 원인 분석

### 가설
- 가설 1: 프로젝트 기본 패턴은 `도메인 예외 + ErrorCode 직접 사용`이다.
- 가설 2: 세부 예외 클래스는 availability/staff 신규 요구를 처리하는 과정에서 국소적으로 추가되었다.
- 가설 3: `IllegalArgumentException` 혼용이 API 에러 계약(상태코드/에러코드) 정합성을 떨어뜨린다.

### 검증 (증거)
- 비즈니스 예외 공통 처리:
  - `BaseBusinessException`만 도메인 예외 경로로 처리됨: `src/main/java/com/example/easybooking/errors/handler/GlobalExceptionHandler.java:32`
  - 기타 예외는 일반 `Exception` 경로로 처리됨: `src/main/java/com/example/easybooking/errors/handler/GlobalExceptionHandler.java:40`
- 도메인 루트 예외(중간 계층) 존재:
  - `BaseBusinessException` 상속 예외 11개(`AuthException`, `ShopException`, `ReservationException` 등): `src/main/java/com/example/easybooking/errors/exception/`
- 세부 예외는 현재 코드에서 availability/staff에만 집중:
  - `AvailabilityException` 하위: `BookingWindowExceededException`, `ShopSettingsNotFoundException`, `StaffOverrideOutOfShopRangeException`
  - `StaffException` 하위: `StaffIdNotFoundException`, `StaffNotFoundException`
  - 위치: `src/main/java/com/example/easybooking/availability/exception/`, `src/main/java/com/example/easybooking/staff/exception/`
- 다른 도메인(auth/shop/reservation/form/file/chat/slot/user)에서는 세부 하위 예외보다 `도메인 예외 + ErrorCode` 직접 사용이 대부분:
  - 예: `throw new AuthException(AuthErrorCode.INVALID_TOKEN)`, `throw new ShopException(ShopErrorCode.SHOP_NOT_FOUND)`, `throw new ReservationException(ReservationErrorCode.TIME_BLOCK_ALREADY_BOOKED)`
- 도메인 예외의 실제 catch 활용은 편중:
  - 도메인 예외를 직접 `catch`하는 케이스는 5건이며 모두 `AuthException` 중심
  - 예: `AuthService`, `JwtAuthenticationFilter`, `JwtChannelInterceptor`
- 반면 `IllegalArgumentException`도 여전히 다수 사용:
  - `throw new IllegalArgumentException(...)` 20건
  - 예: `ShopSettings.updateInterval`에서 interval 검증 시 사용 (`src/main/java/com/example/easybooking/availability/domain/ShopSettings.java:52`)
- 테스트 계약은 ErrorCode 중심:
  - API 테스트의 `$.code` 검증 다수(64건)
  - 예: `BOOKING_WINDOW_EXCEEDED` 검증 (`src/test/java/com/example/easybooking/availability/presentation/AvailabilityControllerIntegrationTest.java:153`)
- `#103`에서 이미 모호점으로 표면화:
  - "수치 검증 실패 시 `IllegalArgumentException` vs 비즈니스 예외" 확인 필요 (`docs/issues/#103/04-tdd/plan.md:138`)

### 결론 (원인)
- 팀의 주된 구현 패턴은 여전히 `도메인 예외 + ErrorCode 직접 사용`이다.
- availability/staff 경로에서 세부 예외가 추가되며 패턴이 이원화되었고, 명시 정책 부재로 혼재가 고착됐다.
- 추가로 `IllegalArgumentException` 혼용이 API 예외 계약(도메인 코드/상태)과의 정합성을 약화시킨다.

## 대안 비교

### 대안 A: 세부 예외 클래스를 줄이고 `도메인 루트 예외 + ErrorCode`로 단순화
- 장점: 타입 수 감소, 학습/검색 비용 감소, ErrorCode 중심 계약과 일치
- 단점: 타입만으로 실패 원인 의미를 읽기 어려움
- 비용: 중간(기존 세부 예외 호출부 정리 필요)
- 리스크: 세부 예외에 의존하던 테스트/로그 맥락 손실 가능
- 운영 난이도: 낮음

### 대안 B: 하이브리드 정책을 명시적으로 표준화
- 정책:
  - 기본: `도메인 루트 예외 + ErrorCode`
  - 예외: 아래 조건을 만족할 때만 세부 예외 클래스 생성
    - 2개 이상 계층/컴포넌트에서 재사용되는 실패 의미
    - 인프라 예외를 도메인 의미로 번역하는 경계 지점
    - 도메인 단위 `catch` 분기가 실제로 필요한 경우
- 장점: 단순성 유지 + 필요한 곳의 의미 명확화
- 단점: 규칙 준수 점검이 필요
- 비용: 낮음~중간(정책 문서화 + 신규 코드 리뷰 규칙 추가)
- 리스크: 규칙이 느슨하면 다시 혼재
- 운영 난이도: 중간

### 대안 C: 도메인 실패 케이스를 광범위하게 세부 예외 클래스로 분리
- 장점: 타입 표현력 극대화, 특정 도메인 catch 전략에 유리
- 단점: 클래스 수 급증, 유지보수 복잡도 증가, ErrorCode와 중복 모델링
- 비용: 높음
- 리스크: 과설계로 인한 생산성 저하
- 운영 난이도: 높음

## 추천안
- **대안 A(도메인 예외 + ErrorCode 직접 사용으로 통일)**를 추천한다.
- 이유:
  - 기존 팀 패턴(직접 ErrorCode 지정)과 가장 일치한다.
  - API 테스트 계약(`$.code`)과 운영 관측 관점에서 가장 단순하고 유지보수 비용이 낮다.
  - availability/staff에만 존재하는 세부 예외를 정리하면 도메인 간 일관성을 회복할 수 있다.

## 결정(반영 예정)
- 앞으로 신규 구현은 세부 예외 클래스 추가 대신 `도메인 루트 예외 + ErrorCode 직접 사용`을 기본으로 한다.
- 기존 availability/staff 세부 예외는 점진적으로 도메인 루트 예외 직접 throw로 이관한다.

## `#103`에 대한 즉시 적용 가이드
- `intervalMinutes <= 0` 검증 실패는 `IllegalArgumentException` 대신 비즈니스 예외 계층으로 통일한다.
- 구현 우선순위:
  1. `AvailabilityErrorCode`에 수치 검증 실패 코드 추가
  2. `new AvailabilityException(AvailabilityErrorCode....)` 또는 동등한 `도메인 루트 예외 + ErrorCode 직접 사용` 패턴으로 통일
  3. API 테스트는 상태코드 + `$.code` 계약을 기준으로 고정

## 위험 / 롤백 / 관측(Observability) 계획
- 위험:
  - 일부 경로에서 여전히 `IllegalArgumentException`이 남아 500 응답이 발생할 수 있음
- 롤백:
  - 세부 예외 제거 과정에서 문제가 생기면, 해당 지점만 임시 복구 후 단계적 이관으로 전환
- 관측:
  - 500 응답 중 `IllegalArgumentException` 비중 모니터링
  - 도메인별 에러코드 발생량(특히 `AvailabilityErrorCode`) 추이 확인
  - `GlobalExceptionHandler`의 `Exception` 경로 유입 로그 점검

## 추후 확인 체크리스트 (Backlog)
- [ ] 예외 모델링 정책 문서에 "`도메인 루트 예외 + ErrorCode 직접 사용` 기본 원칙" 명시
- [ ] 코드 리뷰 체크 항목에 "비즈니스 검증에서 `IllegalArgumentException` 사용 금지" 추가
- [ ] `availability`/`staff` 세부 예외 5개를 루트 예외 직접 throw로 치환하는 이관 계획 수립
- [ ] `availability`/`reservation`/`shop`의 `IllegalArgumentException` 사용처 분류(도메인 검증 vs 프로그래밍 오류)
- [ ] `#103` 구현 후, interval 검증 실패의 API 응답이 `status/code` 계약을 만족하는지 통합 테스트로 확인
- [ ] Auth 외 도메인에서 세부 예외 클래스가 정말 필요한지 재검토(불필요 시 생성 금지)
