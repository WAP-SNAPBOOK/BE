# #103 Design Plan — ShopSettings 초기화 + Settings 4필드 반영(B안)

Created: 2026-03-01  
Issue: `#103`

---

## 1. 배경

현재 `POST /shop` 이후 `PUT /api/v1/shops/{shopId}/schedule/settings`, `PUT /api/v1/shops/{shopId}/schedule/operating-times`를 즉시 호출하면 `SHOP_SETTINGS_NOT_FOUND`가 발생할 수 있다.

원인 요약:
- 매장 생성 경로에서 `ShopSettings` 기본 row를 생성하지 않음
- Schedule 서비스가 `shop_settings` 선행 존재를 강제
- Settings API는 4개 필드를 받지만 실제로는 `intervalMinutes`만 반영

본 계획은 **백필(C안) 없이 B안까지** 진행한다.

---

## 2. 설계안 비교 (A/B)

### 설계안 A: 최소 수정 (기본 생성만 추가)

- 내용
  - `ShopService.createShop()` 트랜잭션에서 `ShopSettings.createDefault(shopId)` 생성
  - Settings API는 현행대로 `intervalMinutes`만 반영
  - 문서를 interval-only 정책으로 축소

- 장점
  - 온보딩 직후 404 장애를 가장 빠르게 차단
  - 변경 범위가 작아 회귀 위험이 낮음

- 단점
  - API 계약 품질 저하 지속(요청 4필드 vs 반영 1필드)
  - 문서/프론트 해석 비용 누적

- 적합성
  - 단기 핫픽스에는 적합
  - 이번 이슈의 문서 정합성 목표에는 불충분

### 설계안 B: 기본 생성 + settings 4필드 전체 반영 (선택)

- 내용
  - 설계안 A 포함
  - `updateSettings()`에서 다음 4개 필드 반영
    - `intervalMinutes`
    - `bookingWindowDays`
    - `minBookingLeadMinutes`
    - `publicHolidayOff`
  - `ShopSettings` 도메인에 필요한 업데이트 메서드 추가
  - #101/#103 문서 스펙을 구현과 동일하게 동기화

- 장점
  - API 입력-저장 계약이 일치
  - 프론트/QA 혼선 감소
  - 이후 가용시간 정책 확장 시 재작업 감소

- 단점
  - 검증 정책(값 범위, null 처리)을 명확히 정의해야 함
  - 테스트 케이스 확장 필요

- 적합성
  - 현재 이슈 목표(장애 차단 + 계약 정합성)에 가장 적합

**결정: 설계안 B 채택**

---

## 3. 상세 설계(B안)

### 3.1 온보딩 시 ShopSettings 기본 생성

- 변경 지점
  - `ShopService.createShop()` 내에서 기본 Staff/Form 생성과 같은 트랜잭션 경계에 `shop_settings` 생성 추가

- 생성 규칙
  - `ShopSettings.createDefault(shopId)` 사용
  - 기본값: interval=30, scheduleType=DAILY, bookingWindowDays=30, minBookingLeadMinutes=60, publicHolidayOff=false

- 멱등/안전성
  - 정상 경로에서는 shop 생성당 1회 생성
  - 재시도/중복 시나리오 대비로 `shop_id` unique 제약 충돌에 대한 방어(존재 시 스킵 또는 재조회) 정책 명시

### 3.2 Settings 업데이트 4필드 반영

- 변경 지점
  - `ShopScheduleService.updateSettings()`
  - `ShopSettings` 도메인 업데이트 메서드

- 반영 정책
  - `intervalMinutes`는 필수(기존과 동일)
  - 나머지 3개 필드는 요청값이 존재하면 반영
    - `bookingWindowDays`
    - `minBookingLeadMinutes`
    - `publicHolidayOff`

- 검증 정책(권장)
  - `intervalMinutes > 0`
  - `bookingWindowDays > 0` (요청 시)
  - `minBookingLeadMinutes >= 0` (요청 시)
  - `publicHolidayOff`는 boolean

### 3.3 문서 동기화

- 정합화 대상
  - `docs/issues/#101/01-user-flow.md`
  - `docs/issues/#101/03-api-spec.md`
  - `docs/issues/#101/04-api-spec-by-flow.md`
  - `docs/issues/#101/08-analysis/*`
  - `docs/issues/#103/*` (이슈/분석 문서)

- 정합화 기준
  - 인증: `SecurityConfig` 실제 정책 기준
  - settings 반영 필드: 4개 필드 반영 기준
  - 온보딩 직후 schedule API 사용 가능 전제 명시

---

## 4. 단계별 롤아웃 (ADD → backfill → switch → cleanup)

### 4.1 ADD

- 코드
  - shop 생성 경로에 기본 settings 생성 추가
  - settings 4필드 업데이트 로직 추가
- 문서
  - API 스펙/유저플로우/분석 문서 동기화

### 4.2 backfill

- **이번 이슈 범위에서 수행하지 않음 (skip)**
- 전제
  - 요청사항: "백필까지는 필요 없음"
- 메모
  - 운영에서 과거 누락 매장 이슈가 확인되면 별도 이슈로 분리

### 4.3 switch

- 테스트/운영 가이드에서 "수동 `shop_settings` 선생성" 전제를 제거
- 온보딩 직후 schedule API 호출 시나리오를 표준 경로로 전환

### 4.4 cleanup

- 테스트 fixture/문서에서 낡은 설명(선행 수동 생성 전제, interval-only 설명) 제거
- 중복 설명/상충 설명 정리

---

## 5. 리스크 및 롤백 전략

### 리스크

1. 입력 검증 누락으로 잘못된 settings 저장
2. 멱등 처리 미흡 시 `shop_settings.shop_id` unique 충돌
3. 문서 일부만 갱신되어 재드리프트 발생

### 완화

- 도메인 레벨 검증 메서드 추가
- 통합테스트로 온보딩 경로 + settings 업데이트 경로 고정
- 문서 갱신 대상을 체크리스트로 관리

### 롤백

- 문제 발생 시:
  1. settings 4필드 반영 로직만 되돌려 interval-only로 임시 복구
  2. 온보딩 기본 settings 생성은 유지(장애 차단 핵심)
- DB 스키마 변경이 없어 롤백 난이도는 낮음

---

## 6. 테스트 전략 (TDD)

### 원칙

- Red → Green → Refactor
- 온보딩 실제 경로(`POST /shop` 이후 schedule API) 중심 검증
- 문서-구현 정합성 검증을 테스트 명세로 고정

### 체크리스트

#### 6.1 Domain/Service

- [ ] `ShopSettings` 업데이트 메서드 검증
  - [ ] `bookingWindowDays` 정상 반영
  - [ ] `minBookingLeadMinutes` 정상 반영
  - [ ] `publicHolidayOff` 정상 반영
  - [ ] 음수/0 경계값 거부 정책 검증

- [ ] `ShopScheduleService.updateSettings()`가 4필드를 저장 후 응답에 반영

#### 6.2 Integration (핵심)

- [ ] `POST /shop` 직후 `GET/PUT /schedule/settings` 성공
- [ ] `POST /shop` 직후 `PUT /schedule/operating-times` 성공
- [ ] `SHOP_SETTINGS_NOT_FOUND`가 온보딩 표준 시나리오에서 재발하지 않음

#### 6.3 Contract/Regression

- [ ] settings 업데이트 요청 4필드가 read-back에서 일치
- [ ] 인증 정책(비인증 요청 차단) 회귀 없음
- [ ] 기존 interval 업데이트 동작 회귀 없음

#### 6.4 엣지 케이스

- [ ] `intervalMinutes=0` 또는 음수 입력
- [ ] `bookingWindowDays` 미입력/null 처리 정책
- [ ] `minBookingLeadMinutes` 음수 입력
- [ ] `publicHolidayOff` 토글(true/false) 반복 업데이트
- [ ] 동일 shop에 대한 중복 초기화 시나리오(재시도)

---

## 7. 구현 범위 요약

- 포함
  - 온보딩 기본 `shop_settings` 생성
  - settings 4필드 전체 반영
  - 관련 테스트/문서 정합성 수정

- 제외
  - 과거 데이터 백필
  - 추가 스키마 변경

---

## 8. 참고

- `docs/issues/#103/01-issue/0001-github-issue-draft.md`
- `docs/issues/#103/02-analysis/0001-root-cause-and-options.md`
- `docs/issues/#103/00-intake/0003-issue-101-contradiction-audit.md`
- `docs/issues/#103/00-intake/0002-api-catalog-and-design-review.md`
