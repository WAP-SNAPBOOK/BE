# `#105` 예약 계약 갭 후속 설계안

작성일: `2026-03-26`  
기준 이슈: `#105`  
관련 문서:

- `docs/issues/#105/frontend-api-handoff.md`
- `docs/specs/reservation/tech-api-spec.md`

## 문제 재정의

`#105`는 요청 계약에서 `formData`를 제거하는 데는 성공했지만,
현재 외부 API 계약은 아래 갭 때문에 아직 "완결된 단일 계약" 상태가 아니다.

1. `requirements`가 별도 영속 저장되지 않는다.
2. 요청 필드명(`requirements`, `imageUrls`)과 생성 응답 필드명(`requests`, `photoUrls`)이 다르다.
3. 상세/목록 응답에서는 `requirements`를 다시 받을 수 없다.

## 현재 상태 진단

### 코드 기준 사실

- 요청 DTO
  - `ReservationCreateRequest`는 `requirements`, `imageUrls`를 받는다.
- 저장 모델
  - `Reservation` 엔티티에는 `requirements` 필드가 없다.
  - 이미지 URL만 `reservation_photos`로 영속 저장한다.
- 생성 응답
  - `ReservationResponse`는 `requests`, `photoUrls`, `photoCount`를 반환한다.
  - `requests`는 현재 저장값이 아니라 요청값 에코다.
- 상세/목록 응답
  - `ReservationDetailResponse`, `ReservationCustomerResponse`, `ReservationOwnerResponse`에는 `requirements`가 없다.

### 왜 문제인가

#### 1. `requirements`가 생성 직후 응답에만 존재한다

- 재조회 시 복원되지 않는다.
- 프론트는 "저장된 예약 데이터"와 "생성 직후 에코 데이터"를 구분해서 다뤄야 한다.
- 추후 예약 상세/관리 화면에서 요구사항을 보여주려면 별도 우회가 필요하다.

#### 2. 요청/응답 필드명이 다르면 프론트 후처리가 늘어난다

- 요청 시 `requirements`, 응답 해석 시 `requests`
- 요청 시 `imageUrls`, 응답 해석 시 `photoUrls`
- 같은 개념을 화면별로 다른 이름으로 관리하게 된다.

#### 3. 지금 바로 문서만 고치면 기술 부채가 고착된다

- 문서로 설명은 가능하지만, 프론트와 백엔드 양쪽에 과도기 로직이 남는다.
- 다음 예약 수정/상세/알림 기능에서 같은 불일치가 다시 전파된다.

## 목표

### 목표

- 예약 API를 "요청/응답/재조회"가 일관된 단일 계약으로 정리한다.
- 프론트가 생성 직후와 재조회 결과를 같은 필드명으로 다룰 수 있게 한다.
- `requirements`를 실제 예약 데이터로 저장하고 다시 읽을 수 있게 한다.

### 비목표

- 예약 도메인 전체 버전 업그레이드
- `menuSelections` 구조 개편
- 예약 수정 API까지 한 번에 재설계

## 대안 비교

### A안. 현 상태 유지 + 문서 보강

#### 내용

- 코드는 그대로 둔다.
- 프론트 전달 문서로만 현재 차이를 설명한다.

#### 장점

- 구현 비용이 가장 낮다.
- 배포 리스크가 없다.

#### 단점

- `requirements` 미저장 문제가 그대로 남는다.
- 생성 응답과 재조회 응답의 의미 차이가 유지된다.
- 필드명 불일치가 장기 부채로 굳어진다.

#### 판단

- 단기 응급조치로는 가능하지만, 해결안으로는 부적절하다.

### B안. 기존 엔드포인트 유지 + 단계적 계약 정렬

#### 내용

- 기존 `POST /api/reservations`와 조회 API는 유지한다.
- `requirements`를 DB에 저장한다.
- 응답에 새 표준 필드(`requirements`, `imageUrls`)를 추가한다.
- 기존 레거시 응답 필드(`requests`, `photoUrls`)는 일정 기간 함께 유지하고 deprecated 처리한다.
- 상세/목록 응답에도 `requirements`, `imageUrls`를 추가한다.

#### 장점

- 가장 현실적인 점진 정렬 방식이다.
- 프론트가 먼저 새 필드로 전환하고, 이후 레거시 필드를 제거할 수 있다.
- 요청/응답 명칭 불일치와 미저장 문제를 동시에 줄일 수 있다.

#### 단점

- 응답 DTO에 과도기 중복 필드가 생긴다.
- 문서/테스트/프론트/백엔드가 한 차례 같이 움직여야 한다.

#### 판단

- 가장 권장하는 기본 방향이다.

### C안. 예약 API v2 도입

#### 내용

- `/api/v2/reservations` 또는 별도 응답 버전을 만든다.
- v2에서는 처음부터 `requirements`, `imageUrls` 기준의 깨끗한 계약만 제공한다.

#### 장점

- 가장 명확하다.
- 기존 레거시 응답 필드 제거 시점을 명확하게 분리할 수 있다.

#### 단점

- 구현/문서/프론트 전환 비용이 크다.
- 한동안 v1/v2 동시 운영이 필요하다.
- 현재 문제 크기에 비해 과하다.

#### 판단

- 외부 소비자가 많거나 앱/웹 다중 클라이언트가 있다면 적절하다.
- 현재 팀 내부 연동 위주라면 비용 대비 과하다.

## 권장안

## 권장안: `B안`

핵심 권장은 아래다.

1. `requirements` 저장/재조회 문제는 즉시 해결한다.
2. 응답 필드명은 additive 방식으로 새 표준 필드를 먼저 추가한다.

즉, 이번 후속은 요청/생성응답/재조회 간의 의미 불일치를 먼저 없애고,
프론트가 하나의 예약 모델로 화면을 구성할 수 있게 만드는 데 집중한다.

## 세부 설계

### 1. `requirements` 영속화

#### 제안

- `reservations` 테이블에 nullable `requirements` 컬럼을 추가한다.
- 생성 시 `request.requirements`를 저장한다.
- 상세/목록/채팅 조회 응답에도 이 값을 노출한다.

#### 이유

- 생성 직후 응답만 보고 알 수 있는 값이면 예약 데이터로서 완결성이 없다.
- 예약 관리, 점주 확인, 고객 재조회에서 같은 정보를 재사용할 수 있어야 한다.

#### 마이그레이션 방향

- `V9__add_reservations_requirements.sql`
- nullable로 시작
- 기존 데이터는 `NULL` 허용

### 2. 응답 필드명 정렬

#### 목표 표준 필드

- `requirements`
- `imageUrls`
- `imageCount`

#### 과도기 호환 정책

- 생성 응답:
  - 새 필드 추가
  - 기존 `requests`, `photoUrls`, `photoCount` 유지
- 상세/목록 응답:
  - 새 필드 추가
  - 기존 `photoUrls`, `photoCount`는 유지 가능
  - `requests`는 원래 없었으므로 새로 추가하지 않음

#### 이유

- 요청/응답의 개념 이름을 맞춰야 프론트 상태 모델이 단순해진다.
- 다만 기존 생성 응답을 바로 깨면 프론트가 동시에 바뀌어야 하므로,
  새 필드를 먼저 추가하고 레거시 필드는 추후 제거하는 편이 안전하다.

### 3. 상세/목록 응답 보강

#### 제안

- 아래 응답에 `requirements`를 추가한다.
  - `ReservationDetailResponse`
  - `ReservationCustomerResponse`
  - `ReservationOwnerResponse`

- 이미지도 표준 필드명을 맞추려면 아래 추가를 고려한다.
  - `imageUrls`
  - `imageCount`

#### 이유

- 생성 화면과 조회 화면이 같은 예약 모델을 바라보게 해야 한다.
- "생성 직후에는 보이는데 새로고침하면 사라지는 데이터"가 없어야 한다.

## 추천 롤아웃

### 1단계. 구조 변경

- DB 컬럼 추가: `requirements`
- 엔티티/리더/라이터 정리
- DTO에 새 표준 응답 필드 추가
- 레거시 응답 필드는 유지

### 2단계. 동작 변경

- 생성 시 `requirements` 저장
- 상세/목록/채팅 조회 응답에 `requirements` 노출
- 생성 응답에 `requirements`, `imageUrls`, `imageCount` 추가

### 3단계. 프론트 전환

- 프론트는 새 필드 기준으로만 사용하도록 변경
- `requests`, `photoUrls`, `photoCount` 의존 제거

### 4단계. 정리

- 레거시 응답 필드 제거

## 테스트 전략

### 자동화해야 할 것

- 생성 후 상세 재조회 시 `requirements`가 유지되는지
- 생성 응답에 새 필드와 레거시 필드가 함께 존재하는지
- 목록/상세/채팅 응답에 `requirements`가 노출되는지
- 마이그레이션 후 기존 데이터가 `NULL requirements`로 문제없이 조회되는지

### 회귀 포인트

- 예약 생성 성공 경로
- 예약 상세 조회
- 내 예약 목록 / 샵 예약 목록 / 채팅 컨텍스트 조회
- 프론트가 아직 레거시 필드에 의존하는 화면

## 리스크와 대응

### 리스크 1. 응답 필드 중복으로 계약이 잠시 더 복잡해짐

대응:

- deprecated 필드를 문서에 명시
- 제거 시점을 별도 이슈로 잡는다

### 리스크 2. `requirements` 컬럼 추가 후 null 데이터 혼재

대응:

- nullable로 도입
- 프론트는 `null`/빈 문자열 모두 안전 처리

## 최종 제안

가장 좋은 해결 방향은 아래 한 문장으로 요약된다.

> `#105`는 요청 스키마만 바꾸는 데서 끝내지 말고,  
> `requirements`를 실제로 저장하고 조회 응답까지 같은 의미의 필드명으로 맞춘 뒤,  
> 프론트가 생성 직후와 재조회 결과를 같은 모델로 다룰 수 있게 해야 한다.

실행 우선순위는 다음이 적절하다.

1. `requirements` 영속화
2. 응답 필드 정렬(`requirements`, `imageUrls`, `imageCount` 추가)
3. 프론트 전환
4. 레거시 응답 제거
