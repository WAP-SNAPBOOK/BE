# #99 TDD Plan — 메뉴 다중 선택 / 추가 입력 / 태그 필터링

Created: 2026-02-10  
Issue: `#99`  
Status: 계획 수립  
Design: `docs/issues/#99/02-design/design-plan.md`

---

## Ground Rules

1. **Red -> Green -> Refactor**: 실패 테스트 -> 최소 구현 -> 리팩토링
2. **Tidy First**: 구조 변경과 동작 변경을 같은 커밋에 섞지 않음
3. **Defect Protocol**: API-level 실패 테스트 -> 최소 재현 테스트 -> 둘 다 통과
4. **한 커밋 = 한 논리 단위**: 테스트 통과 상태에서만 커밋
5. **프로젝트 강제 규칙**:
   - **public API만 테스트** (private 메서드 테스트 금지, public 테스트로 간접 검증)
   - **Reflection 금지**
   - **RED 단계 임시 구현 허용**: 컴파일만 되는 최소 임시 구현(`return null;`, `throw new UnsupportedOperationException("TODO");` 등)으로 "실행 실패(RED)"를 만든다.

---

## Scope

### Goals

- P1: ShopMenu(매장별 메뉴) 엔티티 + CRUD API
- P2: Tag(태그) + 태그 기반 메뉴 필터링 API
- P3: ShopMenuInputField(메뉴별 추가 입력 필드) 엔티티 + CRUD API
- P4: 예약 생성 시 메뉴 선택 + 입력값 저장 (ReservationMenuItem + ReservationMenuInputValue)
- P5: 예약 조회 시 메뉴/입력값 포함 응답

### Non-goals

- `formDataJson` 완전 제거 (Cleanup 단계 — 별도 이슈)
- 메뉴별 가격 관리 (현재 ERD에 없음)
- 메뉴 이미지 관리

---

## Test List

### Phase 1-A: ShopMenu 엔티티 + 저장

> **정책**: ShopMenu = 매장별 메뉴. `shop_menus` 테이블에 매핑. `UNIQUE(shop_id, name)`.

- [x] **1-A-1**: `ShopMenu` 엔티티가 `shop_menus` 테이블에 매핑된다
  - **목적**: DB 테이블과 엔티티 매핑 확인
  - **입력**: `ShopMenu(shopId=1, name="젤네일", isActive=true, sortOrder=0)`
  - **출력**: DB에 저장 후 조회 성공, id 할당됨
  - **엣지케이스**: `shopId` null -> 예외, `name` null -> 예외

- [x] **1-A-2**: 같은 shop에 동일 이름의 메뉴 생성 시 UNIQUE 위반 예외가 발생한다
  - **목적**: 메뉴 이름 중복 방지
  - **입력**: shopId=1, name="젤네일" 2회 저장
  - **출력**: `DataIntegrityViolationException`
  - **엣지케이스**: 다른 shop(shopId=2)에 동일 이름 -> 허용

---

### Phase 1-B: ShopMenu Reader/Writer

- [x] **1-B-1**: `ShopMenuWriter.save()`가 메뉴를 저장하고 ID를 할당한다
  - **목적**: Writer 계층 동작 확인
  - **입력**: `ShopMenu` 엔티티
  - **출력**: 저장된 `ShopMenu` (id 할당됨)

- [x] **1-B-2**: `ShopMenuReader.findActiveByShopId(shopId)`가 해당 shop의 활성 메뉴를 sort_order 순으로 반환한다
  - **목적**: 매장별 메뉴 목록 조회
  - **입력**: shopId (활성 메뉴 3개 + 비활성 1개 존재)
  - **출력**: 활성 메뉴 3개만 sort_order 오름차순
  - **엣지케이스**: 메뉴 없으면 빈 리스트

- [x] **1-B-3**: `ShopMenuReader.getById(id)`가 메뉴를 반환한다
  - **목적**: 단일 메뉴 조회
  - **입력**: 존재하는 id
  - **출력**: 해당 `ShopMenu`
  - **엣지케이스**: 존재하지 않는 id -> 예외 (`ShopMenuNotFoundException` 또는 유사)

---

### Phase 1-C: ShopMenu CRUD API

- [x] **1-C-1**: `POST /api/shops/{shopId}/menus` 메뉴 생성 API
  - **목적**: 점주가 메뉴를 생성
  - **입력**: `{ "name": "젤네일", "description": "기본 젤네일", "sortOrder": 0 }`
  - **출력**: 201 Created + 생성된 메뉴 정보
  - **엣지케이스**:
    - name 빈 문자열 -> 400
    - 동일 이름 중복 -> 409
    - 인증 없음 -> 401
    - 다른 shop 소유자 -> 403

- [x] **1-C-2**: `GET /api/shops/{shopId}/menus` 메뉴 목록 조회 API
  - **목적**: 매장의 활성 메뉴 목록
  - **입력**: shopId
  - **출력**: 200 OK + 활성 메뉴 리스트 (sort_order 순)
  - **엣지케이스**: 메뉴 없으면 빈 배열

- [x] **1-C-3**: `PATCH /api/shops/{shopId}/menus/{menuId}` 메뉴 수정 API
  - **목적**: 메뉴 이름/설명/정렬순서 수정
  - **입력**: `{ "name": "젤네일(수정)", "description": "업데이트", "sortOrder": 1 }`
  - **출력**: 200 OK + 수정된 메뉴 정보
  - **엣지케이스**:
    - 존재하지 않는 menuId -> 404
    - 변경 후 이름 중복 -> 409
    - 다른 shop의 메뉴 -> 403/404

- [x] **1-C-4**: `DELETE /api/shops/{shopId}/menus/{menuId}` 메뉴 비활성화 API
  - **목적**: soft delete (is_active = false)
  - **입력**: 존재하는 메뉴 menuId
  - **출력**: 200 OK (또는 204 No Content)
  - **엣지케이스**:
    - 이미 비활성인 메뉴 -> 멱등 (성공)
    - 존재하지 않는 menuId -> 404

---

### Phase 2-A: Tag 엔티티 + 저장

> **정책**: Tag = 전역 태그. `UNIQUE(name)`. 태그는 shop에 종속되지 않음.

- [x] **2-A-1**: `Tag` 엔티티가 `tags` 테이블에 매핑된다
  - **목적**: DB 테이블과 엔티티 매핑 확인
  - **입력**: `Tag(name="손관리")`
  - **출력**: DB에 저장 후 조회 성공, id 할당됨
  - **엣지케이스**: `name` null -> 예외

- [x] **2-A-2**: 동일 이름 태그 생성 시 UNIQUE 위반 예외가 발생한다
  - **목적**: 태그 이름 중복 방지
  - **입력**: name="손관리" 2회 저장
  - **출력**: `DataIntegrityViolationException`

---

### Phase 2-B: ShopMenuTag 엔티티 + 메뉴-태그 연결

- [x] **2-B-1**: `ShopMenuTag` 엔티티가 `shop_menu_tags` 테이블에 매핑된다
  - **목적**: 메뉴-태그 연결 확인
  - **입력**: `ShopMenuTag(shopMenuId=1, tagId=1)`
  - **출력**: DB에 저장 후 조회 성공

- [x] **2-B-2**: 동일 (메뉴, 태그) 중복 연결 시 UNIQUE 위반 예외가 발생한다
  - **목적**: 중복 연결 방지
  - **입력**: (shopMenuId=1, tagId=1) 2회 저장
  - **출력**: `DataIntegrityViolationException`
  - **엣지케이스**: 같은 메뉴에 다른 태그 -> 허용, 같은 태그에 다른 메뉴 -> 허용

---

### Phase 2-C: 태그 기반 메뉴 필터링

- [x] **2-C-1**: `ShopMenuReader.findActiveByShopIdAndTagIds(shopId, tagIds)` 태그로 메뉴 필터링
  - **목적**: 태그 기반 메뉴 검색
  - **입력**: shopId=1, tagIds=[1] (태그 "손관리"가 달린 메뉴 2개 존재)
  - **출력**: 해당 태그가 있는 활성 메뉴 2개
  - **엣지케이스**:
    - tagIds 여러 개 -> OR 필터 (하나라도 있는 메뉴)
    - 존재하지 않는 tagId -> 빈 결과
    - 태그 없는 메뉴 -> 필터 시 제외
    - 비활성 메뉴 -> 무조건 제외

- [x] **2-C-2**: `GET /api/shops/{shopId}/menus?tagIds=1,2` 태그 필터 API
  - **목적**: API 레벨 태그 필터링
  - **입력**: shopId + tagIds 쿼리 파라미터
  - **출력**: 200 OK + 필터된 메뉴 리스트
  - **엣지케이스**:
    - tagIds 미지정 -> 전체 활성 메뉴 반환 (기존 1-C-2와 동일)
    - tagIds 빈 배열 -> 전체 활성 메뉴 반환

---

### Phase 2-D: 태그 관리 API

- [x] **2-D-1**: `POST /api/tags` 태그 생성 API
  - **목적**: 태그 생성
  - **입력**: `{ "name": "손관리" }`
  - **출력**: 201 Created + 태그 정보
  - **엣지케이스**:
    - 동일 이름 태그 존재 -> 기존 태그 반환 (idempotent) 또는 409
    - name 빈 문자열 -> 400

- [x] **2-D-2**: `POST /api/shops/{shopId}/menus/{menuId}/tags` 메뉴에 태그 연결 API
  - **목적**: 메뉴에 태그 추가
  - **입력**: `{ "tagId": 1 }`
  - **출력**: 200 OK
  - **엣지케이스**:
    - 이미 연결된 태그 -> 멱등 또는 409
    - 존재하지 않는 tagId -> 404
    - 다른 shop 메뉴 -> 403

- [x] **2-D-3**: `DELETE /api/shops/{shopId}/menus/{menuId}/tags/{tagId}` 메뉴에서 태그 제거 API
  - **목적**: 메뉴-태그 연결 해제
  - **입력**: menuId + tagId
  - **출력**: 200 OK (또는 204)
  - **엣지케이스**: 연결되지 않은 태그 -> 멱등 (성공)

- [x] **2-D-4**: `GET /api/tags` 전체 태그 목록 조회 API
  - **목적**: 사용 가능한 태그 목록
  - **출력**: 200 OK + 태그 리스트
  - **엣지케이스**: 태그 없으면 빈 배열

---

### Phase 3-A: ShopMenuInputField 엔티티 + 저장

> **정책**: 메뉴별 추가 입력 필드 정의. `input_type` = NUMBER 또는 TEXT. `UNIQUE(shop_menu_id, label)`. `key` 컬럼 없음 (id로 식별).

- [x] **3-A-1**: `ShopMenuInputField` 엔티티가 `shop_menu_input_fields` 테이블에 매핑된다
  - **목적**: DB 매핑 확인
  - **입력**: `ShopMenuInputField(shopMenuId=1, label="갯수", inputType=NUMBER, required=true, sortOrder=0)`
  - **출력**: DB에 저장 후 조회 성공
  - **엣지케이스**: shopMenuId null -> 예외, label null -> 예외

- [x] **3-A-2**: 같은 메뉴에 동일 label 중복 생성 시 UNIQUE 위반 예외
  - **목적**: 필드 label 중복 방지
  - **입력**: (shopMenuId=1, label="갯수") 2회 저장
  - **출력**: `DataIntegrityViolationException`
  - **엣지케이스**: 다른 메뉴(shopMenuId=2)에 동일 label -> 허용

- [x] **3-A-3**: `inputType`이 NUMBER 또는 TEXT가 아니면 예외
  - **목적**: 타입 제한 검증
  - **입력**: inputType="CHECKBOX"
  - **출력**: 검증 실패/예외

---

### Phase 3-B: ShopMenuInputField 검증 로직

- [x] **3-B-1**: NUMBER 타입에서 `minValue > maxValue`이면 검증 실패
  - **목적**: 범위 논리 검증
  - **입력**: inputType=NUMBER, minValue=10, maxValue=5
  - **출력**: 검증 실패
  - **엣지케이스**: minValue == maxValue -> 허용 (고정값)

- [x] **3-B-2**: NUMBER 타입에서 `stepValue <= 0`이면 검증 실패
  - **목적**: step 양수 보장
  - **입력**: inputType=NUMBER, stepValue=0
  - **출력**: 검증 실패
  - **엣지케이스**: stepValue null -> 허용 (step 제한 없음)

- [x] **3-B-3**: TEXT 타입에서 `maxLength <= 0`이면 검증 실패
  - **목적**: 최대 길이 양수 보장
  - **입력**: inputType=TEXT, maxLength=0
  - **출력**: 검증 실패
  - **엣지케이스**: maxLength null -> 허용 (길이 제한 없음)

- [x] **3-B-4**: TEXT 타입에 `minValue/maxValue/stepValue` 설정 시 무시한다
  - **목적**: 타입별 필드 정합성
  - **입력**: inputType=TEXT, minValue=1
  - **출력**: 저장 후 minValue=null

- [x] **3-B-5**: NUMBER 타입에 `maxLength` 설정 시 무시한다
  - **목적**: 타입별 필드 정합성
  - **입력**: inputType=NUMBER, maxLength=100
  - **출력**: 저장 후 maxLength=null

---

### Phase 3-C: ShopMenuInputField CRUD API

- [x] **3-C-1**: `POST /api/shops/{shopId}/menus/{menuId}/input-fields` 입력 필드 생성 API
  - **목적**: 메뉴에 추가 입력 필드 정의
  - **입력**: `{ "label": "손연장 갯수", "inputType": "NUMBER", "required": true, "minValue": 1, "maxValue": 10, "stepValue": 1, "sortOrder": 0 }`
  - **출력**: 201 Created + 생성된 필드 정보
  - **엣지케이스**:
    - 동일 label 중복 -> 409
    - 존재하지 않는 menuId -> 404
    - 다른 shop의 메뉴 -> 403

- [x] **3-C-2**: `GET /api/shops/{shopId}/menus/{menuId}/input-fields` 입력 필드 목록 조회 API
  - **목적**: 메뉴의 활성 입력 필드 목록
  - **입력**: menuId
  - **출력**: 200 OK + 활성 필드 리스트 (sort_order 순)
  - **엣지케이스**: 필드 없으면 빈 배열

- [x] **3-C-3**: `PATCH /api/shops/{shopId}/menus/{menuId}/input-fields/{id}` 입력 필드 수정 API
  - **목적**: 필드 속성 수정
  - **출력**: 200 OK
  - **엣지케이스**: label 변경 후 중복 -> 409

- [x] **3-C-4**: `DELETE /api/shops/{shopId}/menus/{menuId}/input-fields/{id}` 입력 필드 비활성화 API
  - **목적**: soft delete
  - **출력**: 200 OK (또는 204)
  - **엣지케이스**: 이미 비활성 -> 멱등

---

### Phase 4-A: ReservationMenuItem 엔티티 + 저장

> **정책**: 예약-메뉴 다중 선택. `UNIQUE(reservation_id, shop_menu_id)`. 메뉴 이름/설명은 스냅샷 저장.

- [ ] **4-A-1**: `ReservationMenuItem` 엔티티가 `reservation_menu_items` 테이블에 매핑된다
  - **목적**: DB 매핑 확인
  - **입력**: `ReservationMenuItem(reservationId=1, shopMenuId=1, menuNameSnapshot="젤네일", sortOrder=0)`
  - **출력**: DB에 저장 후 조회 성공
  - **엣지케이스**: reservationId null -> 예외, shopMenuId null -> 예외

- [ ] **4-A-2**: 같은 예약에 동일 메뉴 중복 선택 시 UNIQUE 위반 예외
  - **목적**: 메뉴 중복 선택 방지
  - **입력**: (reservationId=1, shopMenuId=1) 2회 저장
  - **출력**: `DataIntegrityViolationException`
  - **엣지케이스**: 다른 예약(reservationId=2)에 동일 메뉴 -> 허용

---

### Phase 4-B: ReservationMenuInputValue 엔티티 + 저장

- [ ] **4-B-1**: `ReservationMenuInputValue` 엔티티가 `reservation_menu_input_values` 테이블에 매핑된다
  - **목적**: DB 매핑 확인
  - **입력**: `ReservationMenuInputValue(reservationMenuItemId=1, shopMenuInputFieldId=1, fieldLabelSnapshot="갯수", inputTypeSnapshot="NUMBER", valueNumber=5)`
  - **출력**: DB에 저장 후 조회 성공

- [ ] **4-B-2**: 동일 (reservationMenuItemId, shopMenuInputFieldId) 중복 시 UNIQUE 위반 예외
  - **목적**: 입력값 중복 방지
  - **입력**: 동일 조합 2회 저장
  - **출력**: `DataIntegrityViolationException`

---

### Phase 4-C: 예약 생성 시 메뉴 선택 비즈니스 로직

- [ ] **4-C-1**: 예약 생성 시 선택한 메뉴 목록이 `reservation_menu_items`에 저장된다
  - **목적**: 메뉴 다중 선택 저장
  - **입력**: 예약 생성 요청 + menuIds=[1, 2]
  - **출력**: reservation_menu_items에 2건 저장, 스냅샷(이름/설명) 포함
  - **관측**: reservationId가 올바르게 연결됨

- [ ] **4-C-2**: 메뉴 0개 선택 시 예외가 발생한다
  - **목적**: 최소 1개 메뉴 필수 검증
  - **입력**: menuIds=[] (빈 배열)
  - **출력**: 검증 실패/예외
  - **엣지케이스**: menuIds=null -> 예외

- [ ] **4-C-3**: 다른 shop의 메뉴 선택 시 예외가 발생한다
  - **목적**: shop 소속 검증
  - **입력**: shopId=1 예약인데 shopId=2의 메뉴 포함
  - **출력**: 예외 (InvalidShopMenuException 또는 유사)

- [ ] **4-C-4**: 비활성(is_active=false) 메뉴 선택 시 예외가 발생한다
  - **목적**: 활성 메뉴만 선택 가능
  - **입력**: is_active=false인 메뉴 ID
  - **출력**: 예외

- [ ] **4-C-5**: 메뉴 이름/설명이 스냅샷으로 저장되어 원본 변경과 무관하게 보존된다
  - **목적**: 스냅샷 정합성
  - **입력**: 메뉴 "젤네일"로 예약 생성 -> 메뉴 이름을 "젤아트"로 변경
  - **출력**: 예약 조회 시 스냅샷은 "젤네일" 유지

---

### Phase 4-D: 예약 생성 시 입력값 저장 비즈니스 로직

- [ ] **4-D-1**: 메뉴별 추가 입력값이 `reservation_menu_input_values`에 저장된다
  - **목적**: 입력값 저장 확인
  - **입력**: menuId=1에 대해 `{ fieldId: 1, value: 5 }` (NUMBER 타입)
  - **출력**: reservation_menu_input_values에 저장, 스냅샷(label/type) 포함
  - **관측**: valueNumber=5, valueText=null

- [ ] **4-D-2**: required 필드 값 누락 시 검증 실패
  - **목적**: 필수 입력 검증
  - **입력**: required=true 필드에 값 미제공
  - **출력**: 검증 실패/예외

- [ ] **4-D-3**: NUMBER 타입에서 min_value~max_value 범위 초과 시 검증 실패
  - **목적**: 범위 검증
  - **입력**: minValue=1, maxValue=10인 필드에 value=15
  - **출력**: 검증 실패
  - **엣지케이스**:
    - value=1 (경계값 하한) -> 허용
    - value=10 (경계값 상한) -> 허용
    - value=0 (하한 미만) -> 실패

- [ ] **4-D-4**: NUMBER 타입에서 step_value 미준수 시 검증 실패
  - **목적**: step 검증
  - **입력**: minValue=0, stepValue=5인 필드에 value=3
  - **출력**: 검증 실패
  - **엣지케이스**:
    - value=5 -> 허용
    - value=10 -> 허용
    - stepValue=null -> step 검증 건너뜀

- [ ] **4-D-5**: TEXT 타입에서 max_length 초과 시 검증 실패
  - **목적**: 길이 검증
  - **입력**: maxLength=100인 필드에 101자 텍스트
  - **출력**: 검증 실패
  - **엣지케이스**:
    - 100자 -> 허용 (경계값)
    - maxLength=null -> 검증 건너뜀

- [ ] **4-D-6**: NUMBER 필드에 value_text만 있으면 예외가 발생한다
  - **목적**: 타입 매칭 검증
  - **입력**: inputType=NUMBER 필드에 valueText="hello"
  - **출력**: 예외
  - **엣지케이스**: TEXT 필드에 valueNumber만 -> 예외

- [ ] **4-D-7**: 해당 메뉴에 정의되지 않은 input_field_id에 값 입력 시 예외가 발생한다
  - **목적**: 필드 소속 검증
  - **입력**: menuId=1의 메뉴인데 menuId=2에 정의된 fieldId 사용
  - **출력**: 예외

---

### Phase 4-E: Dual-write (formDataJson 호환)

- [ ] **4-E-1**: 예약 생성 시 `formDataJson`도 함께 저장된다 (레거시 호환)
  - **목적**: 기존 프론트엔드 호환 유지
  - **입력**: 메뉴 선택 + 입력값 포함 예약 생성
  - **출력**: `reservation.formDataJson`에 레거시 포맷 JSON 저장
  - **관측**: reservation_menu_items + reservation_menu_input_values도 동시 저장

- [ ] **4-E-2**: `reservation_menu_items` 저장 실패 시 전체 트랜잭션이 롤백된다
  - **목적**: 데이터 정합성 보장
  - **입력**: 유효한 예약 + 잘못된 메뉴 데이터 (UNIQUE 위반 유발)
  - **출력**: 전체 롤백, formDataJson도 저장 안 됨

---

### Phase 5-A: 예약 조회 시 메뉴/입력값 응답

- [ ] **5-A-1**: 예약 상세 조회 시 선택된 메뉴 목록 + 입력값이 포함된다
  - **목적**: 메뉴 정보를 포함한 조회
  - **입력**: 메뉴 2개 + 입력값 포함된 예약 조회
  - **출력**: 응답에 `menus` 배열 (메뉴별 inputValues 중첩 포함)

- [ ] **5-A-2**: `reservation_menu_items` 데이터가 없으면 `formDataJson` fallback
  - **목적**: 기존 예약 호환
  - **입력**: reservation_menu_items 없는 기존 예약 조회
  - **출력**: formDataJson 기반 응답 반환

- [ ] **5-A-3**: 메뉴별 입력값이 해당 메뉴 하위로 중첩 반환된다
  - **목적**: 응답 구조 확인
  - **입력**: 메뉴 A (입력값 2개), 메뉴 B (입력값 0개)
  - **출력**: 메뉴 A -> inputValues[2], 메뉴 B -> inputValues[]

---

## Notes

### DB 테이블 리네이밍 + key 제거 (V4 마이그레이션)

- Flyway V1~V3에서 `shop_services` 계열로 생성
- V4에서 `shop_menus` 계열로 리네이밍 + `key` 컬럼 제거 + `field_key_snapshot` 제거
- 입력 필드 식별: `id`(PK) 사용, `UNIQUE(shop_menu_id, label)`로 중복 방지
- 마이그레이션: `V4__rename_shop_services_to_shop_menus.sql`

### 모호한 요구사항/질문 목록

1. **태그 여러 개 필터 정책**: AND vs OR
   - 잠정: OR (하나라도 있으면 포함)
   - 근거: 사용자 편의성, AND 필터는 결과가 지나치게 좁아질 수 있음

-> 태그는 하나만 선택 가능

2. **메뉴 0개 선택 허용 여부**
   - 잠정: 불허 (최소 1개 필수)
   - 근거: 예약 목적상 메뉴 없는 예약은 의미 없음
  
  -> 허용 안하지 당연히

3. **동일 메뉴 중복 선택 허용 여부**
   - 확정: 불허 (`UNIQUE(reservation_id, shop_menu_id)` 적용됨 — V3 + V4 마이그레이션)


4. **태그 생성 정책 (idempotent vs reject)**
   - 잠정: idempotent (이미 존재하면 기존 태그 반환)
   - 근거: 사용 편의성, 동시성 문제 완화

---

## 진행 상태

| Phase | 상태 | 비고 |
|-------|------|------|
| Phase 1-A | 대기 | ShopMenu 엔티티 |
| Phase 1-B | 대기 | ShopMenu Reader/Writer |
| Phase 1-C | 대기 | ShopMenu CRUD API |
| Phase 2-A | 대기 | Tag 엔티티 |
| Phase 2-B | 대기 | ShopMenuTag 엔티티 |
| Phase 2-C | 대기 | 태그 필터링 |
| Phase 2-D | 대기 | 태그 관리 API |
| Phase 3-A | 대기 | ShopMenuInputField 엔티티 |
| Phase 3-B | 대기 | ShopMenuInputField 검증 |
| Phase 3-C | 대기 | ShopMenuInputField CRUD API |
| Phase 4-A | 대기 | ReservationMenuItem 엔티티 |
| Phase 4-B | 대기 | ReservationMenuInputValue 엔티티 |
| Phase 4-C | 대기 | 예약-메뉴 선택 로직 |
| Phase 4-D | 대기 | 예약-입력값 저장 로직 |
| Phase 4-E | 대기 | Dual-write |
| Phase 5-A | 대기 | 조회 응답 |

---

## 관련 문서

- 설계안: `docs/issues/#99/02-design/design-plan.md`
- ERD v2: `docs/issues/#97/reservation-erd-v2.md`
- #97 TDD plan: `docs/issues/#97/04-tdd/plan.md`
- Flyway V1: `src/main/resources/db/migration/V1__reservation_v2_phase1_add_tables_and_columns.sql`
- Flyway V3: `src/main/resources/db/migration/V3__reservation_v2_phase3_constraints_and_indexes.sql`
- Flyway V4 (리네이밍 + key 제거): `src/main/resources/db/migration/V4__rename_shop_services_to_shop_menus.sql`
