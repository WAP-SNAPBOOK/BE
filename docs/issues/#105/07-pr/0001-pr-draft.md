# PR Draft - #121 Reservation Contract Alignment Reland

기준 브랜치: `develop`  
작업 브랜치: `codex/reland-105`  
저장소: `WAP-SNAPBOOK/BE`

## Title

`refactor : reapply reservation contract alignment and persist requirements`

## Body

```md
## Background / Why This Issue

- 예약 생성 계약은 이미 `formData` 제거 방향으로 이동했지만, `requirements`는 실제 저장되지 않았고 생성 응답과 재조회 응답의 필드 의미도 서로 어긋나 있었습니다.
- 이 상태에서는 프론트가 생성 직후 응답과 상세/목록/채팅 재조회 응답을 서로 다른 모델로 처리해야 하고, `requirements`도 서버 저장값이 아니라 생성 시점 에코 값처럼 다뤄야 했습니다.
- 이전 `PR`은 `develop`에서 revert되어 이번 `PR`은 최신 `develop` 기준 clean reland로 다시 올립니다. git 배경은 여기까지만 두고, 본문은 `#105` 계약 정렬 내용에 집중합니다.

## Summary

- 이전 `formData` 제거와 새 예약 생성 요청 스키마 변경을 최신 `develop` 기준으로 다시 반영했습니다.
- `reservations.requirements` nullable 컬럼을 추가하고, 예약 생성 시 `request.requirements`를 실제 엔티티에 저장하도록 변경했습니다.
- 생성 응답에 표준 필드 `requirements`, `imageUrls`, `imageCount`를 추가하고, 기존 `requests`, `photoUrls`, `photoCount`는 additive 방식으로 유지했습니다.
- 상세/목록/채팅 조회 응답도 `requirements`, `imageUrls`, `imageCount`를 노출하도록 정렬했습니다.
- 프론트 전달 문서와 API 문서를 현재 구현 기준으로 다시 정합화했습니다.
- `time` 포맷 이슈는 이번 PR 범위에서 제외하고 backlog로 분리했습니다.

## Testing

- 실행한 테스트
  - `./gradlew test --tests "com.example.easybooking.reservation.domain.ReservationRequirementsJpaMappingTest" --tests "com.example.easybooking.reservation.service.ReservationServiceCreateReservationNewFieldsUnitTest" --tests "com.example.easybooking.reservation.presentation.ReservationCreateRequestContractIntegrationTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetDetailMenuResponseTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetMyReservationsUnitTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetShopReservationUnitTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetCustomerReservationInChatUnitTest" --tests "com.example.easybooking.reservation.service.ReservationServiceGetReservationsByCustomerInShopUnitTest"`
- 확인 포인트
  - 생성 응답에서 표준 필드와 레거시 필드가 함께 내려오는지
  - 생성 후 재조회 시 `requirements`가 유지되는지
  - 상세/목록/채팅 조회 응답에서 `requirements`, `imageUrls`, `imageCount`가 노출되는지

## Risks

- 배포 시 `V9__drop_reservations_form_data_json.sql`, `V10__add_reservations_requirements.sql` 적용이 필요합니다.
- 응답 필드가 과도기적으로 중복되므로, 프론트는 `requirements`, `imageUrls`, `imageCount` 기준으로 전환해야 합니다.
- `time` 직렬화 포맷은 이번 변경에서 건드리지 않았으므로, 관련 불일치는 기존과 동일하게 남아 있습니다.

## Rollback

- 애플리케이션 레벨 롤백이 필요하면 reland 커밋들과 후속 `requirements`/응답 DTO 확장 커밋을 되돌리면 됩니다.
- DB 컬럼은 nullable additive 변경이라 남아 있어도 기존 기능과 충돌하지 않으므로, 긴급 상황에서는 앱만 먼저 롤백해도 됩니다.

## Related Issue

- Closes #105
```

## Notes

- 현재 세션에는 `github-issue-pr-agent`가 정식 스킬 목록에 노출되지 않아, [SKILL.md](C:/Users/User/.codex/skills/github-issue-pr-agent/SKILL.md)와 [pr-template.md](C:/Users/User/.codex/skills/github-issue-pr-agent/references/pr-template.md)를 직접 따라 수동 작성했다.
