# `reservation time` 응답 포맷 후속 검토

작성일: `2026-03-26`
관련 작업: `#105 예약 생성 계약 정리`
상태: `backlog`

## 배경

`#105` 후속 설계는 우선 `requirements` 영속화와 요청/응답 필드명 정렬에 집중한다.

`time` 요청/응답 표현 차이는 현재 코드 변경 범위에 묶지 않고, 별도 호환성 판단이 필요한 후속 검토 항목으로 분리한다.

## 현재 상태

- 예약 생성 요청은 `LocalTime` 역직렬화로 `HH:mm` 입력을 받는다.
- 예약 생성/조회 응답은 `LocalTime` 기본 직렬화 영향으로 `HH:mm:ss`가 내려간다.
- 필드명은 둘 다 `time`이라 새 필드를 추가하는 additive 방식으로 병행하기 어렵다.

## 후속 검토 포인트

### 1. 변경 시점

- 프론트와 백엔드가 같은 배포 윈도우에서 함께 전환 가능한지 확인한다.
- 다른 소비자나 고정 파서가 `HH:mm:ss`에 의존하는지 확인한다.

### 2. 구현 방식

- 전역 Jackson 설정 변경 대신 예약 응답 DTO에만 포맷을 국소 적용하는 방식을 우선 검토한다.
- 후보 대상:
  - `ReservationResponse`
  - `ReservationDetailResponse`
  - `ReservationCustomerResponse`
  - `ReservationOwnerResponse`

### 3. 완료 기준

- `time` 응답 포맷 정책이 문서와 코드에서 하나로 명시된다.
- 직렬화 포맷 계약 테스트가 추가된다.
- 프론트 전달 문서와 API 문서가 같은 기준으로 갱신된다.
