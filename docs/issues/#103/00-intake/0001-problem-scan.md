# #999 Problem Scan (CS 관점)

## Context(현재 상황)
- 대상: `SNAPBOOK_BE` 백엔드(Spring Boot/JPA/MySQL)
- 방법: 정적 코드 스캔(실행/부하테스트 미포함)
- 목적: 전통적인 CS 관점(무결성, 동시성, 복잡도, 쿼리 비용, 트랜잭션 원자성)에서 개선 우선순위 도출

## 증상/징후
### 1) `durationMinutes=0` 허용으로 점유 블록 0개 생성 가능
- 근거
  - `@MultipleOf(10)`만 적용되어 0 허용: `src/main/java/com/example/easybooking/reservation/dto/ReservationConfirmRequest.java:16`
  - validator는 배수 여부만 검사: `src/main/java/com/example/easybooking/common/validation/MultipleOfValidator.java:23`
  - 블록 개수 `duration / 10`: `src/main/java/com/example/easybooking/reservation/TimeBlockGenerator.java:15`
  - 블록 리스트가 비면 overlap 검사 skip: `src/main/java/com/example/easybooking/reservation/ReservationTimeBlockWriter.java:40`
- 재현 절차(개념)
  1. 예약 확정 API에 `durationMinutes=0` 전달
  2. 확정 상태 전이는 성공
  3. 점유 블록이 생성되지 않아 동일 시간대 중복 확정 가능성 발생

### 2) 채팅방 목록 조회 시 N+1 쿼리 패턴
- 근거
  - 목록 조회 후 방마다 변환: `src/main/java/com/example/easybooking/chat/ChatRoomReader.java:35`
  - 방마다 마지막 메시지 조회: `src/main/java/com/example/easybooking/chat/ChatRoomReader.java:69`
  - 방마다 안읽은 메시지 count 조회: `src/main/java/com/example/easybooking/chat/ChatRoomReader.java:78`
  - 방마다 상대 유저 조회: `src/main/java/com/example/easybooking/chat/ChatRoomReader.java:87`
  - 방마다 샵 조회: `src/main/java/com/example/easybooking/chat/ChatRoomReader.java:49`
- 재현 절차(개념)
  1. 채팅방이 많은 계정으로 목록 API 호출
  2. 방 개수 증가에 비례해 추가 쿼리 선형 증가

### 3) 월간 가용시간 조회에서 반복 DB 호출 + 중첩 스트림 필터
- 근거
  - 월 루프 내 일자별 재계산: `src/main/java/com/example/easybooking/availability/CustomerAvailabilityService.java:58`
  - 일자별 `getAvailableSlots` 호출: `src/main/java/com/example/easybooking/availability/CustomerAvailabilityService.java:71`
  - 슬롯 필터가 `slots x occupiedTimes` 중첩 비교: `src/main/java/com/example/easybooking/availability/AvailabilityService.java:111`
- 재현 절차(개념)
  1. 월간 조회 API 호출
  2. 날짜 수(최대 31)만큼 휴무/운영시간/예약 블록 조회가 반복
  3. 슬롯 수가 늘수록 필터 비용 증가

### 4) 다중 쓰기 로직의 트랜잭션 경계 불명확
- 근거
  - 설정 저장 후 운영시간 replace: `src/main/java/com/example/easybooking/availability/ShopScheduleService.java:117`
  - 직원 운영시간 delete 후 saveAll: `src/main/java/com/example/easybooking/availability/StaffOperatingTimesService.java:40`
- 재현 절차(개념)
  1. 중간 단계에서 예외 발생
  2. 앞선 write만 반영되고 뒤 write가 실패할 가능성

### 5) 채팅방 생성 check-then-act 경쟁 조건
- 근거
  - 조회 후 없으면 생성: `src/main/java/com/example/easybooking/chat/service/ChatRoomService.java:20`
  - 저장 시 DB 유니크 보장 코드 확인 어려움: `src/main/java/com/example/easybooking/chat/domain/ChatRoom.java:11`
- 재현 절차(개념)
  1. 동일 사용자/샵 조합으로 동시 요청
  2. 둘 다 not found를 보고 insert 시도하면 중복 생성 가능성

### 6) 대량 조회 API 페이지네이션 부재
- 근거
  - 예약 목록 API: `src/main/java/com/example/easybooking/reservation/presentation/ReservationController.java:98`
  - 점주 예약 목록 API: `src/main/java/com/example/easybooking/reservation/presentation/ReservationController.java:112`
  - 채팅방 목록 API: `src/main/java/com/example/easybooking/chat/presentation/ChatRoomController.java:42`
- 재현 절차(개념)
  1. 데이터가 누적된 계정에서 목록 조회
  2. 응답 크기/메모리/DB 부하 동시 증가

## 영향 범위
### 사용자
- 예약 중복/충돌 가능성(무결성 이슈)으로 신뢰도 하락
- 채팅/예약 목록 응답 지연으로 UX 저하

### 도메인
- 예약 확정 불변식(유효한 duration + 점유 블록 생성) 위반 위험
- 채팅방 1:1 유일성 보장 실패 시 데이터 정합성 저하

### 성능
- N+1 패턴과 월간 반복 조회로 DB round-trip 급증
- 무페이지네이션으로 응답/메모리 비용 선형 증가

### 비용
- DB 커넥션/CPU 사용량 증가
- 장애 시 운영 대응 비용(수동 정리/데이터 보정) 증가

### 보안/안정성
- 직접적인 보안 취약점보다는 가용성/안정성 리스크가 큼

## 리팩토링 후보 목록(우선순위 + 근거)
1. P0: 예약 확정 입력 불변식 강화
- `durationMinutes`에 `@Positive` 또는 `@Min(10)` 적용, 서비스 레벨 재검증 추가
- 근거: 무결성 훼손 가능성이 직접적이고 재현이 쉬움

2. P0: 채팅방 목록 조회 배치화
- 단건 조회 다발을 조인/집계 쿼리 또는 배치 조회로 통합
- 근거: 사용자 체감 성능과 DB 부하에 즉시 영향

3. P1: 월간 가용시간 조회 프리패치 + 자료구조 최적화
- 월 범위 휴일/예약블록/운영시간 사전 조회 후 메모리 계산
- `occupiedTimes`를 시간축 버킷/Set 기반으로 변환해 중첩 비교 축소
- 근거: 반복 호출/중첩 루프가 명확

4. P1: 스케줄 관련 쓰기 트랜잭션 명시
- 서비스 메서드 단위 `@Transactional`로 원자성 보장
- 근거: 장애 시 부분 반영 리스크 감소

5. P1: 채팅방 유일성 제약 강화
- `(shop_id, customer_id)` 유니크 인덱스 + 충돌 시 재조회 패턴 적용
- 근거: 동시성 환경에서 데이터 정합성 보장

6. P2: 목록 API 페이지네이션 표준화
- `Pageable`/cursor 방식 도입, 기본/최대 size 제한
- 근거: 데이터 증가에 대한 선제적 확장성 확보

## 지금 당장 안 하면 생기는 비용
- 예약 중복/정합성 이슈가 운영 데이터에 누적되어 사후 정리 비용 증가
- 트래픽 증가 시 채팅/예약 조회 지연이 급격히 커질 가능성
- 나중에 데이터가 커진 뒤 최적화하면 마이그레이션/검증 범위가 커져 변경 비용 상승

## 비고
- 본 문서는 코드 스캔 기반 1차 진단이다.
- 실제 우선순위 확정 전, 운영 트래픽/슬로우쿼리/APM 지표와 함께 교차 검증 권장.
