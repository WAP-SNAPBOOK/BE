# PR Draft — #99 메뉴 다중 선택 / 추가 입력 / 태그 필터링

**Branch**: `feature/jiseob/#99` -> `develop`
**Commits**: 25개 (TDD 기반, 커밋당 단일 논리 단위)

---

## Summary

기존 예약 시스템은 `formDataJson`(비정형 JSON)에 전적으로 의존하여 메뉴 정보를 관리했다.
이로 인해 메뉴별 입력값 검증, 태그 필터링, 스냅샷 보존 등이 불가능했다.

이 PR은 정형화된 메뉴/태그/입력 필드 엔티티를 도입하고, 예약 생성-조회 전 과정에서 구조적 데이터를 사용하도록 변경한다. 기존 `formDataJson` 기반 흐름은 dual-write + fallback으로 호환을 유지한다.

**대안 검토**:
- `formDataJson` 스키마 강제화 -> 기존 데이터 마이그레이션 비용이 크고, 필드별 검증이 어려움
- 새 정형 테이블 도입 + dual-write -> 레거시 호환 유지하면서 점진 전환 가능 (채택)

---

## Changes

1. **ShopMenu / Tag / ShopMenuInputField 도메인 신규 구축** (Phase 1~3)
   - `ShopMenu`, `Tag`, `ShopMenuTag`, `ShopMenuInputField` 엔티티 + CRUD API
   - 태그 기반 메뉴 필터링 (OR 로직)
   - 입력 필드 타입별 검증 (NUMBER: min/max/step, TEXT: maxLength)
   - Flyway V4: `shop_services` -> `shop_menus` 계열 리네이밍 + `key` 컬럼 제거

2. **예약-메뉴 연결 및 입력값 저장** (Phase 4)
   - `ReservationMenuItem`, `ReservationMenuInputValue` 엔티티
   - 예약 생성 시 메뉴 선택 + 입력값 검증/저장 (shop/active 검증, required/범위/step/타입 검증)
   - Dual-write: `formDataJson` + 새 테이블 동시 저장, 트랜잭션 정합성 보장

3. **예약 조회 시 메뉴/입력값 응답** (Phase 5)
   - `ReservationMenuItemReader`, `ReservationMenuInputValueReader` 추가
   - `ReservationDetailResponse`에 `menus` 필드 추가 (메뉴별 inputValues 중첩)
   - `reservation_menu_items` 없는 기존 예약은 `formDataJson` fallback

---

## Test plan

- TDD plan: [`docs/issues/#99/04-tdd/plan.md`](docs/issues/#99/04-tdd/plan.md)

### 확인한 테스트 (Phase별)

- [x] Phase 1: ShopMenu 엔티티 JPA 매핑, UNIQUE 제약, Reader/Writer, CRUD API (7건)
- [x] Phase 2: Tag 엔티티, ShopMenuTag, 태그 필터링(OR), TagService CRUD (7건)
- [x] Phase 3: ShopMenuInputField 엔티티, 타입별 검증(NUMBER/TEXT), CRUD API (6건)
- [x] Phase 4-A/B: ReservationMenuItem/InputValue 엔티티, JPA 매핑, UNIQUE 제약 (4건)
- [x] Phase 4-C: 메뉴 선택 로직 (스냅샷, 빈 리스트, 다른 샵, 비활성 메뉴) (5건)
- [x] Phase 4-D: 입력값 저장 (required, min/max, step, maxLength, 타입 불일치, 필드 소속) (7건)
- [x] Phase 4-E: Dual-write (formDataJson 동시 저장, 트랜잭션 롤백) (3건)
- [x] Phase 5-A: 조회 응답 (메뉴+입력값 포함, formDataJson fallback, 중첩 구조) (3건)

### 엣지 케이스 / 회귀 포인트

- NUMBER 필드 경계값 (min/max 정확히 일치하는 값)
- step 검증: minValue 기준 offset 계산, stepValue=null 시 skip
- TEXT maxLength=null 시 검증 skip
- 원본 메뉴 이름 변경 후 스냅샷 보존 (통합 테스트)
- Dual-write 실패 시 전체 롤백 (formDataJson 포함)
- 기존 예약(menu_items 없음) 조회 시 fallback 정상 동작

---

## Risks & Rollback

| 위험 요소 | 영향 | 완화 전략 |
|-----------|------|----------|
| Flyway V4 마이그레이션 실패 | 테이블 리네이밍 실패 시 기존 기능 영향 | V4는 `RENAME TABLE`만 사용, 롤백 SQL 준비 가능 |
| `formDataJson` 제거 시점 미정 | Dual-write 유지 비용 | 별도 이슈로 분리 (Non-goal 명시) |
| N+1 쿼리 (조회 시 메뉴/입력값) | 예약 목록 조회 성능 | 현재 상세 조회에만 적용, 목록 조회는 미적용 |
| 새 엔티티 CRUD API 인증/인가 | 비인가 접근 가능성 | 기존 `@RequireAuthenticatedUser` 패턴 적용 |

**롤백**: 브랜치 revert 시 Flyway V4를 역방향 마이그레이션(V5로 rollback SQL)하면 원복 가능. Dual-write 구조이므로 새 테이블 제거해도 `formDataJson` 기반 기존 흐름은 영향 없음.

---

## Docs

- 이슈 초안: [`docs/issues/#99/01-issue/0001-github-issue-draft.md`](docs/issues/#99/01-issue/0001-github-issue-draft.md)
- 설계안: [`docs/issues/#99/02-design/design-plan.md`](docs/issues/#99/02-design/design-plan.md)
- TDD plan: [`docs/issues/#99/04-tdd/plan.md`](docs/issues/#99/04-tdd/plan.md)
- ERD v2: [`docs/issues/#97/reservation-erd-v2.md`](docs/issues/#97/reservation-erd-v2.md)
- Flyway V4: [`src/main/resources/db/migration/V4__rename_shop_services_to_shop_menus.sql`](src/main/resources/db/migration/V4__rename_shop_services_to_shop_menus.sql)

---

## PR 제목 제안

```
feat(#99): 메뉴 다중 선택, 추가 입력, 태그 필터링 기능 구현
```
