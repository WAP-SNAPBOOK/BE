# #999 API 카탈로그 및 설계 점검

## 범위
- 소스 기준: `src/main/java/com/example/easybooking/**/*Controller.java`
- 포함: REST API + WebSocket(STOMP) 진입점
- 제외: 내부 서비스/리포지토리 상세 구현

## 인증 정책 요약
- 기본: `SecurityConfig`에서 `anyRequest().authenticated()` 적용  
  참조: `src/main/java/com/example/easybooking/auth/SecurityConfig.java:55`
- 예외(permitAll): `allowUrls` 배열에 등록된 경로만 공개  
  참조: `src/main/java/com/example/easybooking/auth/SecurityConfig.java:23`
- 주의: 컨트롤러에 `@RequireAuthenticatedUser`가 없더라도, permitAll이 아니면 JWT 인증이 필요함.

## API 카탈로그
### 1) Auth
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| POST | `/oauth/login/kakao` | 공개 | `KakaoAccessCodeRequest` | `AuthResponse` | 배포 redirect 사용 |
| POST | `/oauth/login/kakao/local` | 공개 | `KakaoAccessCodeRequest` | `AuthResponse` | 로컬 redirect 사용 |
| POST | `/auth/refresh` | 공개 | `TokenRequest` | `AuthResponse` | 리프레시 토큰 기반 재발급 |
| POST | `/auth/token-validation` | 공개 | `TokenRequest` | `204/200` | 토큰 유효성 확인 |

### 2) User
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| GET | `/user/me` | `@RequireAuthenticatedUser` | - | `UserResponse` | 내 정보 조회 |
| POST | `/user/customer/signup` | `@RequireTempUser` | `CustomerSignUpRequest` | `CustomerSignUpResponse` | 임시 사용자 가입 |
| POST | `/user/owner/signup` | `@RequireTempUser` | `OwnerSignUpRequest` | `OwnerSignUpResponse` | 임시 사용자 점주 가입 |

### 3) Shop / Link
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| POST | `/shop` | `@RequireAuthenticatedUser` | `CreateShopRequest` | `CreateShopResponse` | 매장 생성 |
| GET | `/shop/{slugOrCode}` | `@RequireAuthenticatedUser` | Path | `ShopInfoResponse` | slug/code 기반 조회 |
| GET | `/shop/link` | `@RequireAuthenticatedUser` | - | `LinkInfoResponse` | 내 매장 링크 조회 |
| PUT | `/shop/link/slug` | `@RequireAuthenticatedUser` | `SlugUpdateRequest` | `LinkInfoResponse` | slug 갱신 |
| GET | `/shop/info/{shopId}` | `@RequireAuthenticatedUser` | Path | `ShopInfoResponse` | shopId 조회 |
| GET | `/link/chat/{slugOrCode}` | `@RequireAuthenticatedUser` | Path | `ChatRoomResponse` | 링크로 채팅방 조회/생성 |
| GET | `/s/{slugOrCode}` | 공개 | Path | `302 Redirect` | 프론트 경로로 리다이렉트 |

### 4) Menu / InputField / Tag
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| POST | `/api/shops/{shopId}/menus` | JWT(전역) | `CreateShopMenuRequest` | `ShopMenuResponse` | 메뉴 생성 |
| GET | `/api/shops/{shopId}/menus` | JWT(전역) | `tagIds`(opt) | `List<ShopMenuResponse>` | 태그 필터 조회 |
| PATCH | `/api/shops/{shopId}/menus/{menuId}` | JWT(전역) | `UpdateShopMenuRequest` | `ShopMenuResponse` | 메뉴 수정 |
| DELETE | `/api/shops/{shopId}/menus/{menuId}` | JWT(전역) | Path | `200` | 메뉴 비활성화 |
| POST | `/api/shops/{shopId}/menus/{menuId}/input-fields` | JWT(전역) | `CreateInputFieldRequest` | `InputFieldResponse` | 입력필드 생성 |
| GET | `/api/shops/{shopId}/menus/{menuId}/input-fields` | JWT(전역) | Path | `List<InputFieldResponse>` | 입력필드 조회 |
| PATCH | `/api/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}` | JWT(전역) | `UpdateInputFieldRequest` | `InputFieldResponse` | 입력필드 수정 |
| DELETE | `/api/shops/{shopId}/menus/{menuId}/input-fields/{fieldId}` | JWT(전역) | Path | `200` | 입력필드 비활성화 |
| POST | `/api/tags` | JWT(전역) | `CreateTagRequest` | `TagResponse` | 태그 생성/재사용 |
| GET | `/api/tags` | JWT(전역) | - | `List<TagResponse>` | 전체 태그 조회 |
| POST | `/api/shops/{shopId}/menus/{menuId}/tags` | JWT(전역) | `AddTagToMenuRequest` | `200` | 메뉴-태그 연결 |
| DELETE | `/api/shops/{shopId}/menus/{menuId}/tags/{tagId}` | JWT(전역) | Path | `200` | 메뉴-태그 해제 |

### 5) Form
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| GET | `/api/v1/form/{shopId}` | `@RequireAuthenticatedUser` | Path | `FormResponse` | 폼 조회 |
| PATCH | `/api/v1/form/shops/{shopId}` | `@RequireAuthenticatedUser` | `FormPatchRequest` | `200` | 폼 patch |

### 6) Reservation
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| GET | `/api/reservations/{id}` | `@RequireAuthenticatedUser` | Path | `ReservationDetailResponse` | 예약 상세 |
| POST | `/api/reservations` | `@RequireAuthenticatedUser` | `ReservationCreateRequest` | `ReservationResponse` | 예약 생성 |
| PUT | `/api/reservations/{id}/confirm` | `@RequireAuthenticatedUser` | `ReservationConfirmRequest` | `ReservationStatusResponse` | 점주 확정 |
| PUT | `/api/reservations/{id}/reject` | `@RequireAuthenticatedUser` | `ReservationRejectRequest` | `ReservationStatusResponse` | 점주 거절 |
| GET | `/api/reservations/my` | `@RequireAuthenticatedUser` | - | `List<ReservationCustomerResponse>` | 고객 내역 |
| GET | `/api/reservations/shop` | `@RequireAuthenticatedUser` | - | `List<ReservationOwnerResponse>` | 점주 내역 |
| GET | `/api/reservations/shop/{shopId}/availability` | JWT(전역) | `date`(opt) | `ReservationAvailabilityResponse` | 예약 가능시간 |
| GET | `/api/reservations/chat/customer` | `@RequireAuthenticatedUser` | `shopId` | `List<ReservationCustomerResponse>` | 채팅 내 고객 예약 |
| GET | `/api/reservations/chat/owner` | `@RequireAuthenticatedUser` | `shopId`,`customerId` | `List<ReservationOwnerResponse>` | 채팅 내 점주 조회 |

### 7) Availability v1
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| GET | `/api/v1/shops/{shopId}/staff/{staffId}/availability` | JWT(전역) | `date` | `AvailabilitySlotsResponse` | 일간 가용시간 |
| GET | `/api/v1/shops/{shopId}/staff/{staffId}/availability/monthly` | JWT(전역) | `yearMonth` | `AvailabilityMonthlyResponse` | 월간 가용일 |
| GET | `/api/v1/shops/{shopId}/schedule/settings` | `@RequireAuthenticatedUser` | - | `ShopScheduleSettingsResponse` | 점주 설정 조회 |
| PUT | `/api/v1/shops/{shopId}/schedule/settings` | `@RequireAuthenticatedUser` | `UpdateShopScheduleSettingsRequest` | `ShopScheduleSettingsResponse` | 점주 설정 수정 |
| PUT | `/api/v1/shops/{shopId}/schedule/operating-times` | `@RequireAuthenticatedUser` | `UpdateShopOperatingTimesRequest` | `200` | 영업시간 수정 |
| GET | `/api/v1/shops/{shopId}/schedule/operating-times` | `@RequireAuthenticatedUser` | - | `ShopOperatingTimesResponse` | 영업시간 조회 |
| GET | `/api/v1/shops/{shopId}/schedule/holidays` | `@RequireAuthenticatedUser` | - | `ShopHolidaysResponse` | 휴무일 조회 |
| POST | `/api/v1/shops/{shopId}/schedule/holidays` | `@RequireAuthenticatedUser` | `CreateShopHolidayRequest` | `ShopHolidayResponse` | 휴무일 생성 |
| DELETE | `/api/v1/shops/{shopId}/schedule/holidays/{holidayId}` | `@RequireAuthenticatedUser` | Path | `204` | 휴무일 삭제 |
| PUT | `/api/v1/shops/{shopId}/staff/{staffId}/operating-times` | `@RequireAuthenticatedUser` | `UpdateStaffOperatingTimesRequest` | `200` | 직원 오버라이드 수정 |
| GET | `/api/v1/shops/{shopId}/staff/{staffId}/operating-times` | `@RequireAuthenticatedUser` | - | `StaffOperatingTimesResponse` | 직원 오버라이드 조회 |

### 8) Chat REST + WS
| Type | Destination/Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| REST GET | `/chat/rooms/shop/{shopId}` | `@RequireAuthenticatedUser` | Path | `ChatRoomResponse` | 방 조회/생성 |
| REST GET | `/chat/rooms/` | `@RequireAuthenticatedUser` | - | `List<ChatRoomListResponse>` | 내 채팅방 목록 |
| REST PATCH | `/chat/rooms/{chatRoomId}/last-read-message` | `@RequireAuthenticatedUser` | `UpdateLastReadMessageRequest` | `200` | 읽음 포인터 갱신 |
| REST GET | `/chat/rooms/{chatRoomId}/messages` | `@RequireAuthenticatedUser` | `cursor`,`size` | `List<MessageResponse>` | 메시지 히스토리 |
| WS STOMP Endpoint | `/ws-connect` | JWT 인터셉터 | SockJS connect | - | 핸드셰이크 |
| WS Publish | `/pub/chat/{chatRoomId}` | JWT 인터셉터 | `ChatMessageRequest` | - | 송신 |
| WS Subscribe | `/topic/chat/{chatRoomId}` | JWT 인터셉터 | - | `MessageResponse` | 수신 |

### 9) Slot (레거시 가능성)
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| GET | `/api/slot/{shopId}/available-dates` | JWT(전역) | `year`,`month` | `List<LocalDate>` | 날짜 슬롯 |
| GET | `/api/slot/{shopId}/available-times` | JWT(전역) | `date` | `List<LocalTime>` | 시간 슬롯 |
| POST | `/api/slot/{shopId}/slots` | JWT(전역) | `AddSlotDto` | `200` | 슬롯 생성 |

### 10) Dev
| Method | Path | Auth | Request | Response | 비고 |
|---|---|---|---|---|---|
| DELETE | `/dev/user` | `@RequireAuthenticatedUser` | - | `String` | 자기 계정 강제 삭제 |
| DELETE | `/dev/dev/user/{userId}` | `@RequireAuthenticatedUser` | Path | `String` | 임의 유저 강제 삭제 |

## 설계 점검 (현재 상태)
### 잘 된 점
- 인증 기본 정책이 중앙(`SecurityConfig`)에 통합되어 있고 기본은 보호됨.
- Availability 도메인은 `/api/v1/...`로 버전이 명시되어 변경 관리에 유리함.
- 주요 수정 API에 `@Valid`를 적용한 지점이 많아 입력 검증 시작점이 존재함.

### 개선 필요 포인트
1. URI 규칙이 혼재됨
- `/api/v1/...`, `/api/...`, `/shop`, `/user`, `/chat`, `/link`, `/dev`가 공존.
- 버전 전략과 base path 정책을 통일하는 것이 유지보수에 유리.

2. 인증/인가 표현이 일관되지 않음
- 일부는 `@RequireAuthenticatedUser`를 쓰고, 일부는 전역 보안에만 의존.
- 특히 메뉴/태그/슬롯 API는 컨트롤러 시그니처 상 사용자 컨텍스트가 없어 소유권 검증 경계가 불명확.

3. 리소스 소유권 검증 누락 가능성
- 예: 메뉴/태그 관련 서비스는 `shopId` 소유자 검증 없이 동작하는 코드 경로가 보임.
- 참조: `ShopMenuManagementService`, `ShopMenuInputFieldService`, `TagService`.

4. HTTP 상태코드 일관성 부족
- `POST /shop`이 `201` 대신 `200` 반환.
- 생성 API는 `201 Created`로 통일 권장.

5. 네이밍/경로 품질 이슈
- `MesssageController` 오타(클래스명).
- `/chat/rooms/` trailing slash 사용.
- `/dev/dev/user/{userId}` 경로 중복 의미.

6. 기능 중복/세대 혼재
- `Slot` API와 `Availability v1` API가 공존하여 역할 경계가 모호함.
- 신규 표준 API를 정하고 레거시 deprecation 계획 필요.

7. 운영 안전성
- 강제 삭제용 dev 엔드포인트가 일반 런타임 경로에 존재.
- 프로파일 조건/권한 상향(예: ADMIN role)으로 보호 강화 필요.

## 제안하는 정리 순서
1. API 규칙 문서(버전, base path, plural, 상태코드) 1장 확정
2. 인가 정책 표준화(`@Require...` + 소유권 검증 지점 명문화)
3. 메뉴/태그/슬롯 도메인 소유권 검증 보강
4. `Slot` vs `Availability` 통합 로드맵 수립(Deprecation 포함)
5. dev 엔드포인트 보호 정책 적용

