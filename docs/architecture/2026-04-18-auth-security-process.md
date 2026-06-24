# SNAPBOOK 인증/보안 프로세스

작성일: `2026-04-18`

## 목적

이 문서는 현재 레포 구현 기준으로 `SNAPBOOK`의 인증과 보안 흐름을 정확하게 설명하기 위한 문서다.

면접용 질문 리스트가 아니라, 실제 코드가 어떤 순서로 동작하는지와 어디에서 어떤 보안 판단이 일어나는지를 분리해서 정리한다.

---

## 1. 핵심 결론

이 레포의 인증 구조는 크게 네 층으로 나뉜다.

1. `SecurityConfig`가 전역적으로 `permitAll` 경로와 `authenticated()` 경로를 나눈다.
2. `JwtAuthenticationFilter`가 HTTP `Authorization: Bearer ...` 헤더를 읽어 `SecurityContext`에 `AuthenticatedUser` 또는 `TempUser`를 넣는다.
3. 컨트롤러 파라미터의 `@RequireAuthenticatedUser`, `@RequireTempUser`는 커스텀 `ArgumentResolver`가 해석한다.
4. 실제 `OWNER만 가능`, `이 샵의 주인만 가능`, `이 채팅방 참여자만 가능` 같은 비즈니스 인가는 서비스 레이어에서 다시 검증한다.

즉 이 프로젝트는 `전역 시큐리티 -> JWT 인증 -> principal 타입 검증 -> 도메인 인가` 순서로 보안을 구성한다.

---

## 2. 인증 관련 주요 구성요소

### 전역 HTTP 보안

- `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
- `SessionCreationPolicy.STATELESS`
- `httpBasic`, `formLogin`, `csrf` 비활성화
- `JwtAuthenticationFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 삽입
- `allowUrls` 이외의 모든 요청은 `authenticated()`

### HTTP JWT 인증

- `src/main/java/com/example/easybooking/auth/JwtAuthenticationFilter.java`
- Bearer 토큰 추출
- 토큰 검증
- `AuthenticatedUser` 또는 `TempUser` principal 생성
- `SecurityContextHolder`에 저장

### 컨트롤러 principal 주입

- `src/main/java/com/example/easybooking/auth/resolver/AuthenticatedUserArgumentResolver.java`
- `src/main/java/com/example/easybooking/auth/resolver/TempUserArgumentResolver.java`
- `src/main/java/com/example/easybooking/common/config/WebConfig.java`

### 로그인/재발급

- `src/main/java/com/example/easybooking/auth/presentation/AuthController.java`
- `src/main/java/com/example/easybooking/auth/service/AuthService.java`
- `src/main/java/com/example/easybooking/auth/util/JwtUtil.java`
- `src/main/java/com/example/easybooking/auth/util/DelegatingOAuthProvider.java`
- `src/main/java/com/example/easybooking/auth/util/KakaoOAuthProvider.java`

### WebSocket 인증

- `src/main/java/com/example/easybooking/chat/config/WebSocketConfig.java`
- `src/main/java/com/example/easybooking/chat/config/JwtChannelInterceptor.java`
- `src/main/java/com/example/easybooking/chat/service/ChatService.java`

---

## 3. 이 레포가 사용하는 인증 주체

### `AuthenticatedUser`

- 타입: `src/main/java/com/example/easybooking/auth/domain/AuthenticatedUser.java`
- 의미: 회원가입까지 완료된 정식 사용자
- 내부 값:
  - `userId`
  - `role`

### `TempUser`

- 타입: `src/main/java/com/example/easybooking/auth/domain/TempUser.java`
- 의미: 카카오 인증은 끝났지만 회원가입은 아직 끝나지 않은 사용자
- 내부 값:
  - `providerId`

### 중요한 구분

- 이 프로젝트에서 JWT의 `role` claim은 `User.Role` 기준이다.
- `src/main/java/com/example/easybooking/user/domain/User.java`
- 현재 `User.Role`은 `USER`, `ADMIN`이다.
- 반면 실제 서비스의 고객/점주 구분은 `UserType`이며 값은 `CUSTOMER`, `OWNER`다.
- `src/main/java/com/example/easybooking/user/domain/UserType.java`

즉:

- JWT `role` = 시큐리티 principal용 값
- `UserType` = 비즈니스 권한용 값

그래서 이 프로젝트는 `점주만 허용` 같은 인가를 JWT claim만으로 처리하지 않고, 서비스 레이어에서 DB 조회 후 `UserType`이나 소유권으로 다시 검증한다.

---

## 4. 토큰 종류와 실제 claim

### Access Token

- 발급 위치: `JwtUtil.generateAccessToken`
- subject: `userId`
- claims:
  - `role`: `user.getRole().name()`
  - `type`: `access`
- 만료: `jwt.access-token-expiration`

### Refresh Token

- 발급 위치: `JwtUtil.generateRefreshToken`
- subject: `userId`
- claims:
  - `type`: `refresh`
- 만료: `jwt.refresh-token-expiration`
- 주의: `role` claim이 없다.

### Temp Token

- 발급 위치: `JwtUtil.generateTempToken`
- subject: `providerId`
- claims:
  - `type`: `TEMP`
  - `role`: `TEMP`
- 만료: 10분

---

## 5. HTTP 인증 프로세스

### 5-1. 로그인 전 단계: 카카오 OAuth 코드 교환

진입점:

- `POST /oauth/login/kakao`
- `POST /oauth/login/kakao/local`

흐름:

1. `AuthController`가 요청 본문에서 `accessCode`를 받는다.
2. 배포용은 `spring.kakao.auth.redirect`, 로컬용은 `spring.kakao.auth.redirect-local`을 사용한다.
3. `AuthService.oAuthLogin`이 `OAuthProvider.requestKakaoId`를 호출한다.
4. 실제 주입 구현체는 `DelegatingOAuthProvider`다.
5. `accessCode`가 `loadtest_code_`로 시작하면 `MockOAuthProvider`, 아니면 `KakaoOAuthProvider`로 분기한다.
6. `KakaoOAuthProvider`는
   - `https://kauth.kakao.com/oauth/token`에 인가코드를 교환 요청하고
   - 받은 카카오 access token으로 `https://kapi.kakao.com/v2/user/me`를 호출해 카카오 ID를 가져온다.

결과:

- 기존 사용자면 정식 `accessToken`, `refreshToken`을 발급한다.
- 신규 사용자면 `Temp Token`을 발급한다.

### 5-2. 로그인 응답의 실제 의미

응답 타입:

- `src/main/java/com/example/easybooking/auth/dto/AuthResponse.java`

케이스 1. 기존 사용자

- `authStatus = LOGIN_SUCCESS`
- `accessToken` = 정식 access token
- `refreshToken` = refresh token
- `userId`
- `role`
- `userType`

케이스 2. 신규 사용자

- `authStatus = SIGNUP_REQUIRED`
- `accessToken` 필드에 `Temp Token`이 들어간다
- `refreshToken`은 없다

중요한 점:

- 신규 사용자의 임시 토큰은 별도 `tempToken` 필드가 아니라 `accessToken` 필드에 실린다.
- 따라서 클라이언트는 `authStatus`를 보고 이 토큰이 정식 access token인지, 회원가입용 temp token인지 구분해야 한다.

### 5-3. 보호된 HTTP API 요청 처리

전역 진입:

- `SecurityConfig.securityFilterChain`

흐름:

1. 요청이 들어오면 `allowUrls`에 포함되지 않은 경로는 `authenticated()` 대상이다.
2. `JwtAuthenticationFilter`가 `Authorization` 헤더를 읽는다.
3. `Bearer ` 접두어가 없으면 토큰이 없는 것으로 보고 다음 필터로 넘긴다.
4. 토큰이 있으면 `JwtUtil.validateToken`으로 서명/만료/형식을 검증한다.
5. 필터는 토큰의 `role` claim을 읽는다.
6. `role == TEMP`이면:
   - `subject`를 `providerId`로 해석
   - principal을 `TempUser`로 생성
   - authority를 `ROLE_TEMP`로 설정
7. 그 외에는:
   - `subject`를 `userId`로 해석
   - principal을 `AuthenticatedUser`로 생성
   - authority를 `ROLE_ + role`로 설정
8. 생성한 `UsernamePasswordAuthenticationToken`을 `SecurityContextHolder`에 저장한다.
9. 이후 컨트롤러로 진입한다.

### 5-4. 컨트롤러 파라미터 주입

`SecurityContext`에 인증 객체가 있다고 해서 바로 컨트롤러에서 원하는 타입을 받는 것은 아니다.

컨트롤러는 다음 두 애너테이션으로 principal 타입을 명시한다.

- `@RequireAuthenticatedUser`
- `@RequireTempUser`

동작:

- `AuthenticatedUserArgumentResolver`는 principal이 `AuthenticatedUser`인지 확인한다.
- 추가로 `userRepository.existsById(userId)`까지 확인해서, 토큰은 유효하지만 DB에서 삭제된 사용자는 거부한다.
- 이 경우 principal 타입이 다르거나 DB에 사용자가 없으면 `FULL_LOGIN_REQUIRED` 또는 `LOGIN_REQUIRED`를 던진다.

- `TempUserArgumentResolver`는 principal이 `TempUser`인지 확인한다.
- 타입이 다르면 `SIGNUP_TOKEN_REQUIRED` 또는 `AUTHENTICATION_REQUIRED`를 던진다.

즉:

- 정식 인증이 필요한 엔드포인트는 `AuthenticatedUser`를 요구하고
- 회원가입 진행 엔드포인트는 `TempUser`를 요구한다.

### 5-5. 회원가입 완료 흐름

진입점:

- `POST /user/customer/signup`
- `POST /user/owner/signup`

흐름:

1. 이 엔드포인트는 전역 시큐리티에서 `permitAll`이 아니다.
2. 따라서 `Authorization: Bearer {tempToken}`이 먼저 필터를 통과해야 한다.
3. 필터는 `TempUser(providerId)`를 `SecurityContext`에 넣는다.
4. `TempUserArgumentResolver`가 이를 꺼내 컨트롤러 파라미터에 주입한다.
5. `UserService`가 `providerId` 중복 여부를 검사한다.
6. 신규 유저를 저장한다.
7. 저장 직후 정식 `accessToken`, `refreshToken`을 새로 발급한다.

즉 회원가입 API는 `temp token -> user 생성 -> full token 교체` 흐름이다.

### 5-6. 토큰 재발급 흐름

진입점:

- `POST /auth/refresh`

특징:

- 이 엔드포인트는 `permitAll`이다.
- `Authorization` 헤더가 아니라 `TokenRequest.token` 본문으로 refresh token을 받는다.

흐름:

1. `AuthService.refreshAccessToken`이 `jwtUtil.validateToken(refreshToken)`을 호출한다.
2. 이어서 `jwtUtil.getTokenType(refreshToken)`이 정확히 `refresh`인지 검사한다.
3. `subject`에서 `userId`를 꺼낸다.
4. DB에서 사용자를 다시 읽는다.
5. 새 access token과 새 refresh token을 둘 다 발급한다.
6. 응답은 `LOGIN_SUCCESS` 형태로 내려간다.

중요한 점:

- refresh token은 서버에 저장하거나 폐기 목록으로 관리하지 않는다.
- 따라서 새 refresh token을 발급해도 이전 refresh token은 만료 전까지 계속 유효하다.

### 5-7. 토큰 유효성 검사 흐름

진입점:

- `POST /auth/token-validation`

동작:

- `AuthService.validateToken`은 단순히 `jwtUtil.validateToken(token)`만 수행한다.
- 즉 이 엔드포인트는 `서명/만료/형식`만 검사하고, 토큰의 용도(`access`, `refresh`, `TEMP`)는 구분하지 않는다.

---

## 6. WebSocket 인증 프로세스

### 6-1. 핸드셰이크와 CONNECT는 다르게 처리된다

구성:

- `SecurityConfig.allowUrls`에 `/ws-connect/**`가 포함돼 있다.
- `WebSocketConfig.registerStompEndpoints`는 `/ws-connect`를 등록하고 `withSockJS()`를 사용한다.

의미:

- HTTP 핸드셰이크 자체는 전역 시큐리티에서 허용된다.
- 하지만 실제 STOMP 연결 인증은 `JwtChannelInterceptor`가 `CONNECT` 프레임에서 다시 처리한다.

즉 `handshake permitAll`과 `STOMP CONNECT 인증`은 분리돼 있다.

### 6-2. CONNECT 프레임 인증

흐름:

1. 클라이언트가 STOMP `CONNECT`를 보낼 때 native header에 `Authorization: Bearer ...`를 넣는다.
2. `JwtChannelInterceptor.preSend`가 `CONNECT` 프레임을 가로챈다.
3. 헤더가 없거나 Bearer 형식이 아니면 `null`을 반환해 연결을 차단한다.
4. 토큰이 있으면 `jwtUtil.validateToken`으로 검증한다.
5. `userId`, `role`을 토큰에서 꺼낸다.
6. `UsernamePasswordAuthenticationToken`을 만들고 `accessor.setUser(authentication)`로 STOMP 세션에 저장한다.

주의:

- HTTP와 달리 WebSocket에서는 principal을 `AuthenticatedUser` 객체로 넣지 않는다.
- principal 자리에 `Long userId`를 직접 넣는다.

### 6-3. 메시지 송신 시 사용자 식별

진입점:

- `src/main/java/com/example/easybooking/chat/presentation/ChatController.java`

흐름:

1. `@MessageMapping("/chat/{chatRoomId}")`
2. `Principal`을 받는다.
3. `ChatService.extractUserIdFromPrincipal`이 `UsernamePasswordAuthenticationToken` 안의 principal이 `Long`인지 검사한다.
4. `userId`를 꺼낸 뒤 채팅방 참여자인지 검증한다.
5. 메시지를 저장하고 `/topic/chat/{chatRoomId}`로 발행한다.

즉 WebSocket 쪽은 `Long userId principal` 전제를 가진 별도 인증 흐름이다.

---

## 7. 인가는 어디서 처리되는가

이 레포의 인가는 한 곳에서 끝나지 않는다.

### 7-1. 전역 시큐리티가 하는 일

- 이 요청이 `익명 허용`인지 `인증 필요`인지만 판별한다.

### 7-2. JWT 필터가 하는 일

- 토큰이 유효한지 확인한다.
- principal을 `AuthenticatedUser` 또는 `TempUser`로 바꿔 `SecurityContext`에 넣는다.

### 7-3. ArgumentResolver가 하는 일

- 이 컨트롤러가 기대하는 principal 타입이 맞는지 확인한다.
- `TempUser`만 허용할지, 정식 `AuthenticatedUser`만 허용할지를 결정한다.
- 정식 유저 resolver는 DB에 사용자가 실제 존재하는지도 확인한다.

### 7-4. 서비스 레이어가 하는 일

- 실제 비즈니스 권한을 검증한다.

예시:

- 채팅은 `chatRoom.isParticipant(userId)`로 방 참여자 여부를 확인한다.
- 예약 확정/거절은 `user.getUserType() == OWNER`와 `reservation.ownerUserId == requester`를 둘 다 검사한다.
- 샵 설정 수정은 `shopReader.isShopOwnedBy(shopId, ownerUserId)`를 검사한다.

즉 이 프로젝트는 `role 기반 전역 인가`보다 `userId 기반 도메인 인가` 비중이 훨씬 크다.

---

## 8. 에러 응답은 어디서 만들어지는가

### JWT 필터 단계 실패

- `JwtAuthenticationFilter`가 직접 `HttpServletResponse`에 JSON을 쓴다.
- 응답 형식은 `ErrorResponse`
- 이 경우 `path` 값은 `null`이다.
- `traceId`, `timestamp`, `code`, `message`는 포함된다.

### 컨트롤러/리졸버/서비스 단계 실패

- `GlobalExceptionHandler`가 `BaseBusinessException`을 받아 `ErrorResponse`로 변환한다.
- 이 경우 `path`는 실제 요청 URI가 들어간다.

### WebSocket 인증 실패

- `JwtChannelInterceptor`는 예외를 JSON으로 변환하지 않는다.
- 실패 시 `null`을 반환하여 `CONNECT` 자체를 차단한다.
- 즉 WebSocket 인증 실패는 HTTP REST처럼 구조화된 에러 응답이 아니라 연결 거부에 가깝다.

---

## 9. 이 레포 기준으로 꼭 알아야 할 보안 관찰 사항

### 9-1. 가장 중요한 현재 이슈: `refresh token`이 HTTP 필터에서 정식 로그인처럼 받아들여질 수 있다

이유:

1. `JwtAuthenticationFilter`는 토큰의 `type`이 `access`인지 검사하지 않는다.
2. 필터는 `role` claim만 읽어서 `TEMP` 여부만 분기한다.
3. refresh token에는 `role` claim이 없지만 `subject=userId`는 있다.
4. 따라서 현재 코드상 refresh token도 `AuthenticatedUser(userId, null)`로 인증될 수 있다.
5. `AuthenticatedUserArgumentResolver`는 `role` 값을 검사하지 않고, principal 타입과 DB 존재 여부만 확인한다.

결과:

- `Authorization` 헤더에 refresh token을 넣어 일반 보호 API를 호출해도, 현재 구현상 통과할 가능성이 높다.

같은 문제가 WebSocket에도 있다.

- `JwtChannelInterceptor`도 `type`이 아니라 `validateToken + getUserIdFromToken + getRoleFromToken`만 사용한다.
- refresh token을 보내면 `role`은 `null`이지만 `userId`는 추출되므로 STOMP 세션이 생성될 수 있다.

이건 현재 레포 기준으로 가장 중요한 인증 결함이다.

### 9-2. `Temp Token`은 응답에서 `accessToken` 필드로 내려간다

- `AuthResponse.signupRequired`는 `tempToken` 전용 필드를 두지 않는다.
- 따라서 클라이언트가 `authStatus`를 반드시 함께 봐야 한다.
- 설계상 모호성이 있고, 클라이언트 실수 여지가 있다.

### 9-3. refresh token은 서버에서 추적하지 않는다

- DB 저장, 블랙리스트, 토큰 버전, 강제 로그아웃 메커니즘이 없다.
- 따라서 탈취된 refresh token은 만료 전까지 계속 재발급에 사용될 수 있다.

### 9-4. 로그인 성공 시 access token을 로그에 남긴다

- `AuthController`는 카카오 로그인 성공 시 access token 전체 문자열을 로그로 남긴다.
- 운영 환경에서는 민감 정보 로그 노출 위험이 있다.

### 9-5. WebSocket endpoint의 CORS 설정이 HTTP보다 느슨하다

- HTTP는 `SecurityConfig.corsConfigurationSource`에서 origin 패턴을 제한한다.
- 하지만 WebSocket endpoint는 `setAllowedOriginPatterns("*")`를 사용한다.
- 운영 환경에서는 더 엄격한 제한이 필요하다.

### 9-6. WebSocket은 `CONNECT`만 인증하고 `SUBSCRIBE` 인가는 하지 않는다

- `JwtChannelInterceptor`는 `CONNECT` 프레임만 검사한다.
- `postSend`에서 `SUBSCRIBE`를 로그로만 남기고, 구독 대상인 `chatRoomId`에 대한 참여자 검증은 하지 않는다.
- 즉 현재 레포 기준으로는 유효한 STOMP 세션만 만들면, 임의의 `/topic/chat/{chatRoomId}`를 구독할 수 있다.
- 메시지 송신은 `ChatService.saveMessage`에서 `chatRoom.isParticipant(userId)`로 막지만, 구독 자체는 막지 못한다.

이건 현재 WebSocket 인증보다 더 직접적인 채팅방 정보 노출 위험이다.

### 9-7. `permitAll`과 `실제 접근 가능`은 다르다

- 예를 들어 `/auth/refresh`는 `permitAll`이지만 아무나 통과하는 API가 아니라, 본문에 유효한 refresh token이 있어야만 성공한다.
- 반대로 회원가입 API는 전역 시큐리티 기준 `permitAll`이 아니므로 인증은 필요하지만, 정식 유저 토큰이 아니라 `TempUser` principal이어야만 resolver를 통과한다.

즉 이 프로젝트는 `경로 공개 여부`와 `principal 타입 허용 여부`가 별개다.

---

## 10. 실제 인증 흐름을 가장 짧게 요약하면

### 기존 회원 로그인

1. 카카오 인가코드 수신
2. 카카오 ID 조회
3. DB에 `providerId` 존재
4. 정식 `accessToken + refreshToken` 발급
5. 보호 API 호출 시 `JwtAuthenticationFilter -> AuthenticatedUserArgumentResolver -> 서비스 인가`

### 신규 회원 로그인

1. 카카오 인가코드 수신
2. 카카오 ID 조회
3. DB에 `providerId` 없음
4. 10분짜리 `Temp Token` 발급
5. `/user/customer/signup` 또는 `/user/owner/signup` 호출
6. `TempUserArgumentResolver` 통과
7. 유저 생성 후 정식 `accessToken + refreshToken` 발급

### WebSocket 연결

1. `/ws-connect` handshake
2. STOMP `CONNECT`에서 Bearer token 전송
3. `JwtChannelInterceptor`가 검증
4. STOMP 세션 principal에 `userId` 저장
5. 메시지 송신 시 `ChatService.extractUserIdFromPrincipal`
6. 채팅방 참여자 검증 후 저장/발행

---

## 11. 내가 이 레포의 인증 구조를 설명할 때 꼭 강조할 문장

- 이 레포는 `정적 role 기반 시큐리티`보다 `principal 타입 분리`와 `도메인 인가` 중심으로 설계돼 있습니다.
- HTTP는 `JwtAuthenticationFilter`가 `AuthenticatedUser`와 `TempUser`를 나누고, 컨트롤러는 `ArgumentResolver`로 필요한 principal 타입만 받습니다.
- 점주/고객/방 참여자/샵 소유자 같은 실제 권한은 서비스 레이어가 `userId`, `UserType`, 소유권으로 다시 검증합니다.
- 다만 현재 구현에는 `refresh token type 미검증`이라는 중요한 결함이 있어서, 필터와 WebSocket interceptor에서 `type=access`만 허용하도록 보완이 필요합니다.
