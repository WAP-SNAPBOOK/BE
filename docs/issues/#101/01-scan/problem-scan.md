# 문제/리팩토링 후보 스캔 - #101 예약 가능 시간 관리

## 1. 현재 상태

### 1.1 slot 도메인 (사용 안 함)

- `Slot.java`: shop별 단순 슬롯 (`shop`, `startDateTime`, `isBooked`)
- staff별 관리 불가, 운영 시간 설정 없음, 자동 생성 없음
- **현재 사용하지 않는 코드** — 이번 이슈에서 slot 테이블은 무시한다

### 1.2 staff 도메인 (구현됨)

- `Staff.java`: `id`, `shopId`, `name`, `createdAt`, `updatedAt`
- `StaffReader`, `StaffWriter`: Reader/Writer 패턴 적용
- Shop 생성 시 점주가 기본 Staff로 자동 추가됨

### 1.3 shop 도메인 (부분 구현)

- `Shop.java`: `id`, `ownerId`, `businessName`, `businessNumber`, `address`, `publicCode`, `slug`
- 예약 설정(간격, 운영 시간, 휴무일) 관련 필드/테이블 없음

### 1.4 reservation 도메인 (v2 구현됨)

- `Reservation.java`: `startAt`, `durationMinutes`, `status`, `staffId` 등
- `ReservationTimeBlock.java`: 확정 예약 점유 (10분 단위, `UNIQUE(staff_id, block_start_at)`)
- 상태: `PENDING` -> `CONFIRMED`/`REJECTED`/`CANCELED`

### 1.5 Flyway 마이그레이션

- V1: reservation v2 테이블/컬럼 추가
- V2: start_at, staff backfill
- V3: 제약조건/인덱스
- V4: shop_services -> shop_menus 이름 변경
- 운영 시간/휴무일 관련 마이그레이션 없음

---

## 2. 부재한 기능 (이번 이슈 대상)

| 기능 | 현재 | 필요 |
|------|------|------|
| 매장 예약 설정 | 없음 | 간격(30/60분), 예약 가능 기간, 마감 시간, 공휴일 여부 |
| 매장 운영 시간 | 없음 | 요일별 시작/종료 시간 (복수 블록, 휴게시간 지원, 종료=마지막 예약 가능 시간) |
| 직원 운영 시간 오버라이드 | 없음 | 매장 기본값 + 직원별 요일 단위 오버라이드 |
| 매장 휴무일 | 없음 | 매주/격주/매달 정기 휴무 + 특정일 휴무 |
| 공휴일 데이터 | 없음 | 한국 공휴일 정적 데이터 |
| 예약 가능 시간 조회 | 없음 | 규칙 기반 계산 (운영시간 - 휴무 - 점유) |

---

## 3. #97 ERD v2와의 관계

- #97: 예약 생성/확정/거절/취소 + 점유 관리 (reservation_time_blocks)
- #101: 예약 가능한 시간대를 설정하고 조회하는 기능
- **의존관계**: #101의 조회 결과가 #97의 예약 생성 입력값이 됨

---

## 4. 참고 파일

- `src/main/java/com/example/easybooking/slot/Slot.java` (사용 안 함)
- `src/main/java/com/example/easybooking/staff/domain/Staff.java`
- `src/main/java/com/example/easybooking/shop/domain/Shop.java`
- `src/main/java/com/example/easybooking/reservation/domain/ReservationTimeBlock.java`
- `docs/issues/#97/reservation-erd-v2.md`
