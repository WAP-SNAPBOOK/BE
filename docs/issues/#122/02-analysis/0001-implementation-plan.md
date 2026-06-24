# `#122` 서버 오류 즉시 인지 체계 구현 계획

작성일: `2026-03-29`
최종 갱신: `2026-03-29 17:19:55 +09:00`

## 목표

프론트팀 전달이나 수동 로그 확인 없이, 서버 장애와 주요 오류를 운영자가 즉시 인지할 수 있는 최소 알림 체계를 도입한다.

이번 이슈의 1차 목표는 아래 두 가지다.

1. 서버 다운 또는 헬스체크 실패를 자동 감지한다.
2. 감지 결과를 `Discord webhook` 같은 채널로 즉시 전달한다.

## 전제

- `monitoring/` 디렉터리는 로컬 전용 자산으로 보고 이 이슈 설계 근거에서 제외한다.
- 따라서 저장소 내부에 "이미 운영 중인 경보 시스템"은 없는 것으로 본다.

## 현재 상태 요약

- 애플리케이션에는 `Actuator`, `Prometheus` 의존성이 있다.
- `management.endpoints.web.exposure.include`에 `health`, `metrics`, `prometheus`가 열려 있다.
- `SecurityConfig`에서 `/actuator/**`가 허용 경로다.
- `Dockerfile`에는 컨테이너 내부 `HEALTHCHECK`가 있다.
- `GlobalExceptionHandler`는 business exception과 unexpected exception을 구분해 로그를 남긴다.
- `TraceIdFilter`는 모든 요청에 `traceId`를 생성하고 응답 헤더 `X-Trace-Id`로 내려준다.

즉, "관찰 신호"는 이미 일부 있으나, "누가 자동으로 감지해서 알려주는가" 계층은 아직 없다.

## 핵심 결정

### 권장 1차 구조

가장 작은 실전 해법은 아래 2층 구조다.

1. 외부 헬스체커가 서버를 주기적으로 호출한다.
2. 실패 시 `Discord webhook`으로 알린다.

필요하면 그 다음 단계로 아래를 추가한다.

3. 서버 내부에서 `unexpected exception`만 별도로 알린다.

### 왜 이 구성이 먼저인가

- 서버 프로세스 다운, 기동 실패, OOM, 네트워크 단절은 서버 내부 코드로는 감지할 수 없다.
- 외부 pull 기반 체크는 이 문제를 잡을 수 있다.
- 반대로 예외 상세 내용은 서버 내부가 더 잘 안다.
- 따라서 "다운 감지"와 "예외 내용 전달"은 분리하는 편이 맞다.

## 방법 비교

### 방법 A. 외부 헬스체커 -> Discord

예:

- 외부 uptime checker
- 클라우드 health checker
- 별도 작은 watcher 서비스

대상:

- `/actuator/health`
- 또는 별도 운영용 `/internal/health`

장점:

- 서버 다운도 잡는다.
- 애플리케이션 코드 변경이 거의 없거나 아예 없을 수 있다.
- 알림 중복 제어를 헬스체커 쪽에서 맡길 수 있다.

단점:

- "서버가 죽었는지"는 알지만, 어떤 예외였는지는 모른다.
- 현재처럼 `/actuator/**`가 공개 허용이면 보안 재검토가 필요하다.

### 방법 B. `GlobalExceptionHandler` -> Discord

장점:

- 어떤 요청에서 어떤 예외가 났는지 바로 알 수 있다.
- `traceId`를 함께 보내면 로그 추적이 빨라진다.

단점:

- 서버 다운은 못 잡는다.
- business exception까지 섞이면 알림이 무너진다.
- 디스코드 전송 실패가 요청 처리 경로에 영향 없도록 비동기 분리가 필요하다.
- 같은 장애가 짧은 시간에 수십 건 중복 발송될 수 있다.

### 권장 결론

1차는 `방법 A`, 2차는 `방법 A + 방법 B(unexpected exception만)` 조합이 맞다.

## 구현 단위

### 변경 단위 1

- 목표: 외부 헬스체크 대상 엔드포인트를 안전하게 확정한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/resources/application.yml`
  - `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
  - 필요 시 신규 health endpoint
- 검증:
  - 헬스체크 엔드포인트가 외부 checker에서 접근 가능하다.
  - 불필요한 actuator 상세 정보가 그대로 공개되지 않는다.
- 완료 조건:
  - 운영자가 사용할 헬스체크 URL과 접근 정책이 명확해진다.

### 변경 단위 2

- 목표: 외부 헬스체커와 Discord 알림을 연결한다.
- 분류: `Behavioral`
- 수정 대상:
  - 저장소 밖 운영 설정
  - 필요 시 환경 변수 문서
- 검증:
  - 서버를 의도적으로 내리거나 잘못된 URL로 설정했을 때 Discord 알림이 온다.
- 완료 조건:
  - 프론트팀 전달 없이 서버 다운을 바로 인지할 수 있다.

### 변경 단위 3

- 목표: `unexpected exception`만 별도 알림 대상으로 분리한다.
- 분류: `Structural`
- 수정 대상:
  - `src/main/java/com/example/easybooking/errors/handler/GlobalExceptionHandler.java`
  - 신규 `src/main/java/com/example/easybooking/monitoring/...` 또는 `notification/...`
- 검증:
  - business exception은 알림이 가지 않는다.
  - `Exception.class` 경로의 unexpected error만 알림이 간다.
- 완료 조건:
  - 운영 장애성 예외만 별도 채널로 들어온다.

### 변경 단위 4

- 목표: 예외 알림을 비동기/중복 억제 방식으로 안정화한다.
- 분류: `Behavioral`
- 수정 대상:
  - Discord notifier 구현
  - rate limit 또는 dedupe 캐시
- 검증:
  - 동일 예외가 짧은 시간에 반복되어도 과도한 스팸이 발생하지 않는다.
- 완료 조건:
  - 알림이 운영에 도움이 되는 수준으로만 온다.

## 1차 경보 후보

- `InstanceDown`
  - 기준: 외부 헬스체크 연속 실패
- `HealthEndpointTimeout`
  - 기준: 응답 시간 초과
- `UnexpectedExceptionBurst`
  - 기준: 짧은 시간 동안 `unexpected exception` 반복

## 구현 시 알아야 할 점

### 1. 지금 상태에서 `/actuator/**`를 그대로 운영 노출하는 것은 위험할 수 있다

현재는 `/actuator/**`가 공개 허용이고, `health` 상세도 표시된다. 외부 checker를 붙일 거면 운영용 health endpoint를 따로 두거나 접근 제어를 다시 잡는 편이 안전하다.

### 2. `4xx`, `409`는 운영 장애 알림에서 분리해야 한다

현재 코드베이스는 business exception이 많고, 이들은 의도된 `400`, `404`, `409`인 경우가 많다. 이를 그대로 Discord에 보내면 거의 바로 알림 피로가 발생한다.

### 3. `Unexpected error`만 따로 다뤄야 한다

`GlobalExceptionHandler`는 이미 `BaseBusinessException`과 일반 `Exception`을 분리하고 있다. 운영 알림은 후자부터 시작하는 것이 맞다.

### 4. 디스코드는 전달 채널이지 감지기가 아니다

스터디 문서 기준으로도 감지/경보와 알림 채널은 분리해서 보는 것이 맞다. 디스코드 webhook은 마지막 전달 수단으로 두는 편이 안정적이다.

### 5. `traceId`를 알림에 넣어야 운영 속도가 빨라진다

현재 응답 헤더와 에러 응답에 `traceId`가 있으므로, 예외 알림을 붙인다면 `traceId`, 요청 메서드, 경로, 예외 타입은 최소 포함하는 것이 좋다.

## 추천 적용 순서

1. 운영용 헬스체크 엔드포인트와 접근 정책을 먼저 정리한다.
2. 외부 헬스체커에서 해당 URL을 주기적으로 호출하고 Discord 알림을 연결한다.
3. 그 다음에 `unexpected exception` 전용 Discord notifier를 붙인다.
4. 마지막으로 중복 억제와 샘플링을 넣는다.

## 완료 기준

- 프론트팀 전달 없이 서버 다운을 1분 내외로 인지할 수 있다.
- 운영 장애성 예외는 Discord에서 확인할 수 있다.
- 정상적인 `4xx`/`409` 요청 때문에 운영 채널이 오염되지 않는다.

## 참고 근거

### 저장소 코드

- `Actuator`/`Prometheus` 의존성 존재
- actuator endpoint 노출 설정
- `/actuator/**` 허용 설정
- `GlobalExceptionHandler`의 business/unexpected 분리
- `TraceIdFilter`의 `X-Trace-Id`

### 스터디 문서

- `study/study-system-design/part2/refined/chap05.md`
  - pull 기반 수집기, 경보 시스템, 중복 억제
- `study/study-system-design/part1/refined/chap10.md`
  - 알림 채널 분리, 재시도, 운영 알림의 별도 전달 체계
