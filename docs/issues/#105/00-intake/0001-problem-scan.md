# #999 API 폴더 문서 기반 구현 모순 점검 (Problem Scan)

## Context(현재 상황)

- 요청: `api/` 폴더 문서(`01~04`) 기준으로 현재 프로젝트 API 구현과 모순 여부 점검.
- 점검 문서:
    - `api/01-user-flow.md`
    - `api/02-api-mapping.md`
    - `api/03-api-spec.md`
    - `api/04-api-spec-by-flow.md`
- 대조 코드:
    - `src/main/java/com/example/easybooking/auth/SecurityConfig.java`
    - `src/main/java/com/example/easybooking/availability/presentation/AvailabilityController.java`
    - `src/main/java/com/example/easybooking/availability/CustomerAvailabilityService.java`
    - `src/main/java/com/example/easybooking/availability/HolidayChecker.java`
    - `src/main/java/com/example/easybooking/shop/presentation/*Controller.java`
    - `src/main/java/com/example/easybooking/shop/service/*Service.java`
    - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java`
    - `src/main/java/com/example/easybooking/reservation/dto/*`

## 증상/징후

~~### 1) 인증 정책 문서와 실제 보안 설정 불일치 (광범위)

- 문서 근거:
    - `api/03-api-spec.md:859,888,913,1209,1260` 등 다수 구간에 `인증: 없음 (공개)` 표기
    - `api/04-api-spec-by-flow.md:734,794,849,890`에 availability/menu 조회 공개 표기
- 구현 근거:
    - `src/main/java/com/example/easybooking/auth/SecurityConfig.java:23` (`allowUrls`)
    - `src/main/java/com/example/easybooking/auth/SecurityConfig.java:55` (`anyRequest().authenticated()`)
    - 공개 예외에는 `/api/**` 계열이 포함되지 않음
- 재현 절차:
    1. JWT 없이 `GET /api/v1/shops/{shopId}/staff/{staffId}/availability?date=...` 호출
    2. JWT 없이 `GET /api/tags`, `GET /api/shops/{shopId}/menus` 호출
    3. 문서 기대: 200
    4. 실제 기대: 401/403
- 로그/지표/스크린샷:
    - 미실행(정적 코드 점검)~~

### 2) 예약 생성 요청 스펙의 필수성 불일치 (`formData`)

- 문서 근거:
    - `api/04-api-spec-by-flow.md:975`에서 `formData`를 `필수 N`으로 표기
- 구현 근거:
    - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java:92`
    - `src/main/java/com/example/easybooking/reservation/service/ReservationService.java:102,109` (
      `formData.get("date"), formData.get("time")` 직접 접근)
- 재현 절차:
    1. `POST /api/reservations` 요청에서 `formData`를 생략
    2. 문서 기대: nullable 허용
    3. 실제: `formData` null 접근으로 예외 발생 가능 (500 계열 위험)
- 로그/지표/스크린샷:
    - 미실행(정적 코드 점검)

### 3) 예약 상세 응답 필드명 문서-코드 불일치

- 문서 근거:
    - `api/04-api-spec-by-flow.md:1068,1069,1072,1073` (`menuId`, `menuName`, `fieldId`, `label`)
    - `api/03-api-spec.md:1395,1396` (`menuId`, `menuName`)
- 구현 근거:
    - `src/main/java/com/example/easybooking/reservation/dto/ReservationMenuItemResponse.java:10-12`
        - `shopMenuId`, `menuNameSnapshot`, `priceSnapshot`
    - `src/main/java/com/example/easybooking/reservation/dto/ReservationMenuInputValueResponse.java:10-11`
        - `fieldLabelSnapshot`, `inputTypeSnapshot`
- 재현 절차:
    1. 문서 스키마(`menuId`, `menuName`, `label`) 기준으로 프론트 파싱 구현
    2. 실제 응답 수신 시 키 불일치로 매핑 실패/빈값 처리 발생
- 로그/지표/스크린샷:
    - 미실행(DTO 정의 기반)

### 4) 월 캘린더 색상/분류 규칙이 문서 간/구현 간 상충

- 문서 근거:
    - `api/01-user-flow.md:70-71` 휴무=회색, 공휴일=빨간색
    - `api/03-api-spec.md:1236,1243` `holidayDates=공휴일`, `closedDates=휴무일`
    - `api/04-api-spec-by-flow.md:772-773` `holidayDates=공휴일+정기휴무`, `closedDates=운영시간 없음/직원 off`
- 구현 근거:
    - `src/main/java/com/example/easybooking/availability/CustomerAvailabilityService.java:59-60`
        - `holidayChecker.isHoliday(...)`이면 `holidayDates`로 분류
    - `src/main/java/com/example/easybooking/availability/HolidayChecker.java:22-34`
        - 정기휴무 + (설정 시) 공휴일을 모두 holiday로 판정
    - `src/main/java/com/example/easybooking/availability/CustomerAvailabilityService.java:65-66`
        - 운영시간 없음/직원 off는 `closedDates`
- 재현 절차:
    1. WEEKLY 휴무만 설정한 샵에서 월 조회
    2. 문서 01/03 기대: closed(회색) 또는 공휴일만 holiday
    3. 실제: WEEKLY 휴무가 `holidayDates`로 내려감
- 로그/지표/스크린샷:
    - 미실행(코드 로직 점검)

### 5) `{shopId}` 경로 의미와 실제 소유권 검증의 불일치

- 문서/경로 계약 징후:
    - `/api/shops/{shopId}/menus/...` 형태는 매장 스코프 강제를 암시
- 구현 근거:
    - `src/main/java/com/example/easybooking/shop/service/ShopMenuManagementService.java:43-44,50-51`
        - `shopId` 파라미터를 받아도 `menuId` 단독 조회/수정
    - `src/main/java/com/example/easybooking/shop/service/ShopMenuInputFieldService.java:40-41,49-50`
        - `fieldId` 단독 조회/수정
    - `src/main/java/com/example/easybooking/shop/service/TagService.java:35,40`
        - 메뉴-태그 연결/해제 시 `shopId` 검증 없음
- 재현 절차:
    1. 다른 샵의 `menuId`/`fieldId`를 알고 있는 계정으로 수정/삭제 호출
    2. path의 `{shopId}`와 실데이터 소속이 달라도 통과 가능성 존재
- 로그/지표/스크린샷:
    - 미실행(정적 코드 점검, 보안 리스크 후보)

## 영향 범위

- 사용자:
    - 문서대로 연동 시 인증 실패/응답 파싱 실패로 기능 중단 가능.
- 도메인:
    - 예약/가용성/메뉴 플로우의 계약 신뢰도 저하.
- 성능:
    - 직접 성능 저하는 제한적이나, 재시도/오류 핸들링 증가로 간접 부하 가능.
- 비용:
    - 프론트-백엔드 스펙 재정렬 및 QA 회귀 비용 증가.
- 보안:
    - `{shopId}` 소유권 검증 누락 시 수평 권한 상승 위험.

## 리팩토링 후보 목록(우선순위 + 근거)

1. P0 - 인증 정책 문서 일괄 정정

- 근거: 공개 표기된 다수 API가 실제로는 인증 필수.
- 조치: `api/03`, `api/04`의 `인증: 없음` 라벨 전수 수정 + 공개 경로 표준표 추가.

2. P0 - 메뉴/태그/입력필드 소유권 검증 강제

- 근거: `{shopId}`와 실제 검증 경계 불일치.
- 조치: 서비스 계층에서 `shopId-menuId-fieldId` 정합성 검증 + owner 검증 공통화.

3. P1 - 예약 생성 스키마 계약 정합화

- 근거: 문서는 `formData` optional, 구현은 사실상 required.
- 조치: 문서를 required로 고치거나 DTO/서비스를 nullable-safe로 개선.

4. P1 - 예약 상세 응답 키 계약 통일

- 근거: 문서 필드명과 DTO 필드명 불일치.
- 조치: 문서를 DTO 기준으로 정정하거나 응답 DTO alias 필드 제공.

5. P2 - 캘린더 분류 용어/색상 규칙 단일화

- 근거: `api/01`, `api/03`, `api/04` 간 의미 충돌.
- 조치: `holidayDates`/`closedDates`의 정의를 1개 문서에서 단일 소스로 고정.

## 지금 당장 안 하면 생기는 비용

- 신규 프론트 연동에서 인증/스키마 오해로 반복 장애가 발생한다.
- QA가 문서 기준 기대값과 실제 결과 불일치로 테스트 케이스를 반복 수정하게 된다.
- 메뉴/태그 수정 권한 경계가 불명확한 상태가 지속되면 보안 사고 대응 비용이 급격히 커진다.
