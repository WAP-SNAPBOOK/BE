# `#115` GitHub Issue Draft

## 배경

- 현재 예약 캘린더 조회와 예약 생성은 `staffId`를 요구한다.
- 공개 링크 진입과 채팅방 내 예약 버튼 진입 모두 예약 시작 전에 직원 선택 단계가 필요해졌다.
- 기존 링크/채팅 응답은 예약 시작에 필요한 직원 목록을 제공하지 않는다.

## 목표

- 링크 진입과 채팅 예약 버튼 진입이 공통 예약 시작 컨텍스트를 조회할 수 있게 한다.
- 프론트가 예약 시작 전에 `staffId`를 선택할 수 있게 한다.

## 요구사항

- `slugOrCode` 기반 공개 예약 진입 API가 필요하다.
- `shopId` 기반 인증 예약 진입 API가 필요하다.
- 응답에는 `shopId`, `shopName`, `defaultStaffId`, `staffs[]`가 포함되어야 한다.
- 직원 목록은 현재 정책상 `id ASC`를 기본 정렬로 사용한다.
- 기존 `shopId` 기반 예약 가능 시간 API는 레거시로 표시한다.

## Acceptance Criteria

- 공개 링크 진입에서 직원 목록을 조회할 수 있다.
- 채팅 예약 버튼 진입에서 직원 목록을 조회할 수 있다.
- 프론트가 응답만으로 `staffId`를 선택한 뒤 기존 availability API를 호출할 수 있다.
- 레거시 예약 가능 시간 API에 deprecated 및 cleanup TODO가 표시된다.
