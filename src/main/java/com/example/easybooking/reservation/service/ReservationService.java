package com.example.easybooking.reservation.service;

import com.example.easybooking.chat.domain.MessageType;
import com.example.easybooking.chat.domain.ReservationChangeSnapshot;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.ReservationMenuInputValueReader;
import com.example.easybooking.reservation.ReservationMenuInputValueWriter;
import com.example.easybooking.reservation.ReservationChangeHistoryWriter;
import com.example.easybooking.reservation.ReservationMenuItemReader;
import com.example.easybooking.reservation.ReservationMenuItemWriter;
import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationTimeBlockWriter;
import com.example.easybooking.reservation.ReservationStatusHistoryWriter;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.TimeBlockGenerator;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationMenuInputValue;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.dto.MenuSelectionRequest;
import com.example.easybooking.reservation.dto.ReservationAvailabilityResponse;
import com.example.easybooking.reservation.dto.ReservationConfirmRequest;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationCustomerResponse;
import com.example.easybooking.reservation.dto.ReservationDetailResponse;
import com.example.easybooking.reservation.dto.ReservationCancelRequest;
import com.example.easybooking.reservation.dto.ReservationMenuInputValueResponse;
import com.example.easybooking.reservation.dto.ReservationMenuItemResponse;
import com.example.easybooking.reservation.dto.ReservationOwnerResponse;
import com.example.easybooking.reservation.dto.ReservationRejectRequest;
import com.example.easybooking.reservation.dto.ReservationUpdateRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.reservation.dto.ReservationStatusResponse;
import com.example.easybooking.reservation.event.ReservationEvent;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.exception.StaffIdNotFoundException;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final int CALENDAR_TIME_UNIT_MINUTES = 30;
    private static final int MIN_DURATION_MINUTES = 30;
    private static final int MAX_DURATION_MINUTES = 180;


    private final ReservationWriter reservationWriter;
    private final ReservationReader reservationReader;
    private final UserReader userReader;
    private final ShopReader shopReader;
    private final StaffReader staffReader;

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeBlockGenerator timeBlockGenerator;
    private final ReservationTimeBlockWriter reservationTimeBlockWriter;
    private final ReservationStatusHistoryWriter reservationStatusHistoryWriter;
    private final ReservationChangeHistoryWriter reservationChangeHistoryWriter;
    private final ReservationMenuItemService reservationMenuItemService;
    private final ReservationMenuInputValueService reservationMenuInputValueService;
    private final ReservationMenuItemReader menuItemReader;
    private final ReservationMenuInputValueReader inputValueReader;
    private final ReservationMenuItemWriter reservationMenuItemWriter;
    private final ReservationMenuInputValueWriter reservationMenuInputValueWriter;
    private final Clock clock;


    /**
     * 1. 고객 예약 신청 로직 (Create)
     */
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request, Long userId) {
        Long customerUserId = userId;
        Long shopId = request.getShopId();
        Long staffId = request.getStaffId();

        Shop shop = shopReader.read(shopId);
        Long ownerUserId = shop.getOwnerId();

        LocalDate date = request.getDate();
        if (date == null) {
            throw new ReservationException(ReservationErrorCode.REQUIRED_DATE_MISSING);
        }

        LocalTime time = request.getTime();
        if (time == null) {
            throw new ReservationException(ReservationErrorCode.REQUIRED_TIME_MISSING);
        }

        if (staffId == null) {
            throw new ReservationException(ReservationErrorCode.REQUIRED_STAFF_ID_MISSING);
        }

        try {
            if (!staffReader.read(staffId).getShopId().equals(shopId)) {
                throw new ReservationException(ReservationErrorCode.STAFF_NOT_IN_SHOP);
            }
        } catch (StaffIdNotFoundException e) {
            throw new ReservationException(ReservationErrorCode.STAFF_NOT_FOUND);
        }

        List<String> designImageURLs = request.getImageUrls() == null
                ? Collections.emptyList()
                : List.copyOf(request.getImageUrls());

        User customer = userReader.read(customerUserId);
        String customerName = customer.getName();

        validateTimeIsOn10MinuteBoundary(time);

        Reservation newReservation = Reservation.createReservation(
                shopId,
                ownerUserId,
                customerUserId,
                date,
                time,
                designImageURLs
        );
        newReservation.setStaffId(staffId);
        newReservation.setRequirements(request.getRequirements());

        Reservation savedReservation = reservationWriter.save(newReservation);

        List<MenuSelectionRequest> menuSelections = request.getMenuSelections();
        if (menuSelections != null && !menuSelections.isEmpty()) {
            List<ReservationMenuItem> savedMenuItems = reservationMenuItemService.saveMenuItems(
                    savedReservation.getId(), shopId, menuSelections);

            // 메뉴별 입력값 저장
            for (int i = 0; i < menuSelections.size(); i++) {
                MenuSelectionRequest selection = menuSelections.get(i);
                if (selection.getInputValues() != null && !selection.getInputValues().isEmpty()) {
                    reservationMenuInputValueService.saveInputValues(
                            savedMenuItems.get(i).getId(),
                            selection.getMenuId(),
                            selection.getInputValues()
                    );
                }
            }
        }

        // 예약 생성 커밋 성공 후 시스템 메시지 발행(웹소켓) 처리를 트리거
        eventPublisher.publishEvent(new ReservationEvent(
                savedReservation.getId(),
                savedReservation.getShopId(),
                savedReservation.getCustomerId(),
                null,
                null,
                null,
                MessageType.RESERVATION_CREATED
        ));

        return new ReservationResponse(
                savedReservation,
                customerName);
    }

    private void validateTimeIsOn10MinuteBoundary(LocalTime time) {
        if (time.getMinute() % 10 != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            throw new ReservationException(ReservationErrorCode.INVALID_TIME_INTERVAL);
        }
    }

    private void validateCalendarStartTime(LocalTime time) {
        if (time.getMinute() % CALENDAR_TIME_UNIT_MINUTES != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            throw new ReservationException(ReservationErrorCode.INVALID_CALENDAR_TIME_INTERVAL);
        }
    }

    private void validateCalendarDuration(Integer durationMinutes) {
        if (durationMinutes == null) {
            throw new ReservationException(ReservationErrorCode.INVALID_RESERVATION_UPDATE_REQUEST);
        }
        if (durationMinutes < MIN_DURATION_MINUTES || durationMinutes > MAX_DURATION_MINUTES) {
            throw new ReservationException(ReservationErrorCode.INVALID_DURATION_RANGE);
        }
        if (durationMinutes % CALENDAR_TIME_UNIT_MINUTES != 0) {
            throw new ReservationException(ReservationErrorCode.INVALID_CALENDAR_TIME_INTERVAL);
        }
    }

    private ReservationStatusResponse buildStatusResponse(Reservation reservation, String customerName) {
        ReservationStatusResponse response = new ReservationStatusResponse();
        response.setStatus(reservation.getStatus());
        response.setCustomerName(customerName);
        response.setDate(reservation.getDate());
        response.setTime(reservation.getTime());

        // 확정 메시지 또는 거절 사유를 예약 상태(STATUS)에 따라 분기
        if (reservation.getStatus() == Reservation.Status.CONFIRMED) {
            response.setMessage(reservation.getConfirmationMessage());
        } else if (reservation.getStatus() == Reservation.Status.REJECTED) {
            response.setRejectReason(reservation.getRejectionReason());
        } else if (reservation.getStatus() == Reservation.Status.CANCELED) {
            response.setCanceledByType(reservation.getCanceledByType());
            response.setCanceledByUserId(reservation.getCanceledByUserId());
            response.setCanceledAt(reservation.getCanceledAt());
            response.setCancelReason(reservation.getCancelReason());
            response.setCancelTiming(reservation.getCancelTiming());
            response.setRefundEligible(reservation.getRefundEligible());
        }
        return response;
    }

    /**
     * 2. 원장님 예약 수락(확정) 로직 (Update) - OWNER 권한 검증 - 샵 일치 검증 (본인 샵 예약만 처리 가능) - Reservation 엔티티의 confirm() 메소드 호출
     */
    @Transactional
    public ReservationStatusResponse confirmReservation(Long reservationId, Long ownerUserId,
                                                        ReservationConfirmRequest request) {
        User user = userReader.read(ownerUserId);

        if (user.getUserType() != UserType.OWNER) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "예약 확정 권한이 없습니다. (OWNER만 가능)");
        }

        Reservation reservation = reservationReader.getById(reservationId);

        if (!reservation.getOwnerUserId().equals(ownerUserId)) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }

        Reservation.Status fromStatus = reservation.getStatus();
        LocalDate targetDate = request.getDate() != null ? request.getDate() : reservation.getDate();
        LocalTime targetStartTime = request.getStartAt() != null
                ? request.getStartAt()
                : reservation.getStartAt().toLocalTime();
        if (request.getDate() != null || request.getStartAt() != null) {
            reservation.reschedule(targetDate, targetStartTime);
        }
        validateCalendarStartTime(targetStartTime);
        validateCalendarDuration(request.getDurationMinutes());

        reservation.confirm(request.getMessage(), request.getDurationMinutes());
        reservationStatusHistoryWriter.save(
                reservation.getId(),
                fromStatus,
                reservation.getStatus(),
                ownerUserId,
                request.getMessage()
        );

        List<ReservationTimeBlock> blocks = timeBlockGenerator.generate(
                reservationId,
                reservation.getStaffId(),
                LocalDateTime.of(targetDate, targetStartTime),
                request.getDurationMinutes()
        );

        reservationTimeBlockWriter.allocateOrThrowOnConflict(blocks, reservation.getStaffId());

        eventPublisher.publishEvent(new ReservationEvent(
                reservation.getId(),
                reservation.getShopId(),
                reservation.getCustomerId(),
                request.getDurationMinutes(),
                null,
                null,
                MessageType.RESERVATION_CONFIRMED
        ));

        User customer = userReader.read(reservation.getCustomerId());
        String customerName = customer.getName();

        return buildStatusResponse(reservation, customerName);
    }

    /**
     * 3. 예약 거절 로직 (Update)
     */
    @Transactional
    public ReservationStatusResponse rejectReservation(Long reservationId, Long ownerUserId,
                                                       ReservationRejectRequest request) {
        // 1. 원장님(owner) 권한 검증
        User user = userReader.read(ownerUserId);
        if (user.getUserType() != UserType.OWNER) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "예약 거절 권한이 없습니다. (OWNER만 가능)");
        }

        // 2. 예약 엔티티 조회 및 샵 일치 여부 확인
        Reservation reservation = reservationReader.getById(reservationId);

        // 3. 샵 일치 검증
        if (!reservation.getOwnerUserId().equals(ownerUserId)) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }

        // 4. 엔티티 상태 변경
        log.info("예약 ID: {} - 거절 전 상태: {}", reservationId, reservation.getStatus());
        Reservation.Status fromStatus = reservation.getStatus();
        reservation.reject(request.getReason());
        reservationStatusHistoryWriter.save(
                reservation.getId(),
                fromStatus,
                reservation.getStatus(),
                ownerUserId,
                request.getReason()
        );
        log.info("예약 ID: {} - 거절 후 상태: {}", reservationId, reservation.getStatus());

        eventPublisher.publishEvent(new ReservationEvent(
                reservation.getId(),
                reservation.getShopId(),
                reservation.getCustomerId(),
                null,
                null,
                null,
                MessageType.RESERVATION_REJECTED
        ));

        // 5. 고객명 조회
        User customer = userReader.read(reservation.getCustomerId());
        String customerName = customer.getName();

        // 6. 응답 DTO 생성 및 반환
        return buildStatusResponse(reservation, customerName);
        // TODO: 고객에게 거절 알림
    }

    /**
     * 3-1. 원장님 예약 수정 로직 (Update)
     */
    @Transactional
    public ReservationDetailResponse updateReservation(Long reservationId, Long ownerUserId,
                                                       ReservationUpdateRequest request) {
        User user = userReader.read(ownerUserId);
        if (user.getUserType() != UserType.OWNER) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "예약 수정 권한이 없습니다. (OWNER만 가능)");
        }

        Reservation reservation = reservationReader.getById(reservationId);
        if (!reservation.getOwnerUserId().equals(ownerUserId)) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }
        if (reservation.getStatus() != Reservation.Status.CONFIRMED) {
            throw new ReservationException(ReservationErrorCode.INVALID_RESERVATION_STATUS_FOR_UPDATE);
        }

        LocalDate oldDate = reservation.getDate();
        LocalTime oldStartAt = reservation.getStartAt() != null
                ? reservation.getStartAt().toLocalTime()
                : reservation.getTime();
        Integer oldDurationMinutes = reservation.getDurationMinutes();
        Long oldStaffId = reservation.getStaffId();
        String oldOwnerMessage = reservation.getConfirmationMessage();
        List<ReservationChangeSnapshot.MenuSnapshot> oldMenus = loadMenuSnapshots(reservationId);
        String beforeJson = serializeChangeSnapshot(buildChangeSnapshot(reservation));
        LocalDate targetDate = request.getDate() != null ? request.getDate() : oldDate;
        LocalTime targetStartAt = request.getStartAt();
        Long targetStaffId = request.getStaffId() != null ? request.getStaffId() : reservation.getStaffId();
        Integer targetDurationMinutes = request.getDurationMinutes() != null
                ? request.getDurationMinutes()
                : reservation.getDurationMinutes();
        String ownerMessage = normalizeOwnerMessage(request.getMessage());
        ReservationChangeSnapshot.ValueChange<String> ownerMessageChange =
                ownerMessage != null && !Objects.equals(oldOwnerMessage, ownerMessage)
                        ? new ReservationChangeSnapshot.ValueChange<>(oldOwnerMessage, ownerMessage)
                        : null;
        boolean menuUpdateRequested = request.getMenuSelections() != null;

        boolean hasAnyUpdate = (request.getDate() != null && !Objects.equals(oldDate, targetDate))
                || (targetStartAt != null && !Objects.equals(oldStartAt, targetStartAt))
                || (request.getStaffId() != null && !Objects.equals(oldStaffId, targetStaffId))
                || (request.getDurationMinutes() != null
                        && !Objects.equals(oldDurationMinutes, targetDurationMinutes))
                || ownerMessageChange != null
                || menuUpdateRequested;
        if (!hasAnyUpdate) {
            throw new ReservationException(ReservationErrorCode.INVALID_RESERVATION_UPDATE_REQUEST);
        }

        if (targetStaffId == null) {
            throw new ReservationException(ReservationErrorCode.REQUIRED_STAFF_ID_MISSING);
        }
        Staff targetStaff;
        try {
            targetStaff = staffReader.read(targetStaffId);
            if (!targetStaff.getShopId().equals(reservation.getShopId())) {
                throw new ReservationException(ReservationErrorCode.STAFF_NOT_IN_SHOP);
            }
        } catch (StaffIdNotFoundException e) {
            throw new ReservationException(ReservationErrorCode.STAFF_NOT_FOUND);
        }

        LocalTime targetStartTime = targetStartAt != null ? targetStartAt : oldStartAt;
        validateCalendarStartTime(targetStartTime);
        validateCalendarDuration(targetDurationMinutes);

        LocalDateTime targetStartDateTime = LocalDateTime.of(targetDate, targetStartTime);
        List<ReservationTimeBlock> blocks = timeBlockGenerator.generate(
                reservationId,
                targetStaffId,
                targetStartDateTime,
                targetDurationMinutes
        );
        reservationTimeBlockWriter.replaceForReservationOrThrowOnConflict(reservationId, blocks, targetStaffId);
        reservation.updateConfirmed(targetDate, targetStartTime, targetStaffId, targetDurationMinutes, ownerMessage);

        List<ReservationChangeSnapshot.MenuSnapshot> updatedMenus = oldMenus;
        ReservationChangeSnapshot.ValueChange<List<ReservationChangeSnapshot.MenuSnapshot>> menusChange = null;
        if (menuUpdateRequested) {
            replaceReservationMenus(reservationId, reservation.getShopId(), request.getMenuSelections());
            updatedMenus = loadMenuSnapshots(reservationId);
            if (!Objects.equals(oldMenus, updatedMenus)) {
                menusChange = new ReservationChangeSnapshot.ValueChange<>(oldMenus, updatedMenus);
            }
        }

        LocalTime updatedStartAt = reservation.getStartAt() != null
                ? reservation.getStartAt().toLocalTime()
                : reservation.getTime();
        ReservationChangeSnapshot reservationChange = buildReservationChangeSnapshot(
                oldDate,
                reservation.getDate(),
                oldStartAt,
                updatedStartAt,
                oldDurationMinutes,
                reservation.getDurationMinutes(),
                oldStaffId,
                reservation.getStaffId(),
                targetStaff,
                ownerMessageChange,
                menusChange
        );
        if (reservationChange == null) {
            throw new ReservationException(ReservationErrorCode.INVALID_RESERVATION_UPDATE_REQUEST);
        }

        String afterJson = serializeChangeSnapshot(buildChangeSnapshot(reservation));
        reservationChangeHistoryWriter.save(
                reservation.getId(),
                ownerUserId,
                "UPDATE",
                beforeJson,
                afterJson,
                ownerMessage
        );

        eventPublisher.publishEvent(new ReservationEvent(
                reservation.getId(),
                reservation.getShopId(),
                reservation.getCustomerId(),
                reservation.getDurationMinutes(),
                "예약 정보가 변경되었습니다.",
                reservationChange,
                MessageType.RESERVATION_UPDATED
        ));

        return getReservationDetail(reservationId, ownerUserId);
    }

    /**
     * 4. 예약 취소 로직 (Update)
     */
    @Transactional
    public ReservationStatusResponse cancelReservation(Long reservationId, Long ownerUserId,
                                                        ReservationCancelRequest request) {
        User user = userReader.read(ownerUserId);
        if (user.getUserType() != UserType.OWNER) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "예약 취소 권한이 없습니다. (OWNER만 가능)");
        }

        Reservation reservation = reservationReader.getById(reservationId);
        if (!reservation.getOwnerUserId().equals(ownerUserId)) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }

        Reservation.Status fromStatus = reservation.getStatus();
        LocalDateTime canceledAt = LocalDateTime.now(clock);
        Reservation.CancelTiming cancelTiming = determineCancelTiming(reservation, canceledAt);
        Boolean refundEligible = cancelTiming == Reservation.CancelTiming.BEFORE_CUTOFF;

        reservation.cancel(
                ownerUserId,
                Reservation.CanceledByType.OWNER,
                cancelTiming,
                request.getReason(),
                refundEligible,
                canceledAt
        );
        reservationStatusHistoryWriter.save(
                reservation.getId(),
                fromStatus,
                reservation.getStatus(),
                ownerUserId,
                request.getReason()
        );
        reservationTimeBlockWriter.deleteByReservationId(reservationId);

        eventPublisher.publishEvent(new ReservationEvent(
                reservation.getId(),
                reservation.getShopId(),
                reservation.getCustomerId(),
                null,
                buildReservationCancelContent(request.getReason(), cancelTiming, refundEligible),
                null,
                MessageType.RESERVATION_CANCELED
        ));

        User customer = userReader.read(reservation.getCustomerId());
        return buildStatusResponse(reservation, customer.getName());
    }

    /**
     * 예약 상세 조회 (reservationId) - 인증 필요 - 해당 예약의 고객(customerId) 또는 점주(ownerUserId)만 조회 가능
     */
    public ReservationDetailResponse getReservationDetail(Long reservationId, Long requesterUserId) {
        Reservation reservation = reservationReader.getById(reservationId);

        boolean isCustomer = reservation.getCustomerId().equals(requesterUserId);
        boolean isOwner = reservation.getOwnerUserId().equals(requesterUserId);
        if (!isCustomer && !isOwner) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "해당 예약을 조회할 권한이 없습니다.");
        }

        User customer = userReader.read(reservation.getCustomerId());
        Shop shop = shopReader.read(reservation.getShopId());
        String staffName = resolveStaffName(reservation.getStaffId());

        List<String> imageUrls = reservation.getDesignImageURLs() == null
                ? List.of()
                : List.copyOf(reservation.getDesignImageURLs());

        List<ReservationMenuItemResponse> menus = loadMenuResponses(reservationId);
        Long totalPrice = calculateTotalPrice(menus);

        return ReservationDetailResponse.builder()
                .id(reservation.getId())
                .status(reservation.getStatus())
                .date(reservation.getDate())
                .time(reservation.getTime())
                .startAt(reservation.getStartAt())
                .durationMinutes(reservation.getDurationMinutes())
                .createdAt(reservation.getCreatedAt())
                .shopId(reservation.getShopId())
                .shopName(shop.getBusinessName())
                .staffId(reservation.getStaffId())
                .staffName(staffName)
                .customerName(customer.getName())
                .customerPhone(customer.getPhoneNumber())
                .rejectionReason(reservation.getRejectionReason())
                .confirmationMessage(reservation.getConfirmationMessage())
                .requirements(reservation.getRequirements())
                .photoUrls(imageUrls)
                .photoCount(imageUrls.size())
                .imageUrls(imageUrls)
                .imageCount(imageUrls.size())
                .menus(menus)
                .totalPrice(totalPrice)
                .build();
    }

    private String resolveStaffName(Long staffId) {
        if (staffId == null) {
            return null;
        }
        try {
            return staffReader.read(staffId).getName();
        } catch (StaffIdNotFoundException e) {
            return null;
        }
    }

    private List<ReservationMenuItemResponse> loadMenuResponses(Long reservationId) {
        List<ReservationMenuItem> menuItems = menuItemReader.findByReservationId(reservationId);
        if (menuItems.isEmpty()) {
            return List.of();
        }

        List<Long> menuItemIds = menuItems.stream()
                .map(ReservationMenuItem::getId)
                .toList();

        List<ReservationMenuInputValue> allInputValues =
                inputValueReader.findByReservationMenuItemIds(menuItemIds);

        Map<Long, List<ReservationMenuInputValue>> inputValuesByMenuItemId = allInputValues.stream()
                .collect(Collectors.groupingBy(ReservationMenuInputValue::getReservationMenuItemId));

        return menuItems.stream()
                .map(item -> {
                    List<ReservationMenuInputValueResponse> inputValueResponses =
                            inputValuesByMenuItemId.getOrDefault(item.getId(), List.of()).stream()
                                    .map(iv -> ReservationMenuInputValueResponse.builder()
                                            .fieldLabelSnapshot(iv.getFieldLabelSnapshot())
                                            .inputTypeSnapshot(iv.getInputTypeSnapshot())
                                            .valueNumber(iv.getValueNumber())
                                            .valueText(iv.getValueText())
                                            .build())
                                    .toList();

                    return ReservationMenuItemResponse.builder()
                            .shopMenuId(item.getShopMenuId())
                            .menuNameSnapshot(item.getMenuNameSnapshot())
                            .tagNameSnapshot(item.getTagNameSnapshot())
                            .priceSnapshot(item.getPriceSnapshot())
                            .sortOrder(item.getSortOrder())
                            .inputValues(inputValueResponses)
                            .build();
                })
                .toList();
    }

    private List<ReservationChangeSnapshot.MenuSnapshot> loadMenuSnapshots(Long reservationId) {
        List<ReservationMenuItem> menuItems = menuItemReader.findByReservationId(reservationId);
        if (menuItems.isEmpty()) {
            return List.of();
        }

        List<Long> menuItemIds = menuItems.stream()
                .map(ReservationMenuItem::getId)
                .toList();

        List<ReservationMenuInputValue> allInputValues =
                inputValueReader.findByReservationMenuItemIds(menuItemIds);

        Map<Long, List<ReservationMenuInputValue>> inputValuesByMenuItemId = allInputValues.stream()
                .collect(Collectors.groupingBy(ReservationMenuInputValue::getReservationMenuItemId));

        return menuItems.stream()
                .map(item -> new ReservationChangeSnapshot.MenuSnapshot(
                        item.getShopMenuId(),
                        item.getMenuNameSnapshot(),
                        item.getTagNameSnapshot(),
                        item.getPriceSnapshot(),
                        item.getSortOrder(),
                        inputValuesByMenuItemId.getOrDefault(item.getId(), List.of()).stream()
                                .map(iv -> new ReservationChangeSnapshot.MenuInputValueSnapshot(
                                        iv.getShopMenuInputFieldId(),
                                        iv.getFieldLabelSnapshot(),
                                        iv.getInputTypeSnapshot(),
                                        iv.getValueNumber(),
                                        iv.getValueText()
                                ))
                                .toList()
                ))
                .toList();
    }

    private void replaceReservationMenus(
            Long reservationId,
            Long shopId,
            List<MenuSelectionRequest> menuSelections
    ) {
        List<ReservationMenuItem> existingMenuItems = menuItemReader.findByReservationId(reservationId);
        List<Long> existingMenuItemIds = existingMenuItems.stream()
                .map(ReservationMenuItem::getId)
                .toList();
        reservationMenuInputValueWriter.deleteByReservationMenuItemIds(existingMenuItemIds);
        reservationMenuItemWriter.deleteByReservationId(reservationId);

        if (menuSelections == null || menuSelections.isEmpty()) {
            return;
        }

        List<ReservationMenuItem> savedMenuItems = reservationMenuItemService.saveMenuItems(
                reservationId,
                shopId,
                menuSelections
        );

        for (int i = 0; i < menuSelections.size(); i++) {
            MenuSelectionRequest selection = menuSelections.get(i);
            if (selection.getInputValues() != null && !selection.getInputValues().isEmpty()) {
                reservationMenuInputValueService.saveInputValues(
                        savedMenuItems.get(i).getId(),
                        selection.getMenuId(),
                        selection.getInputValues()
                );
            }
        }
    }

    private Long calculateTotalPrice(List<ReservationMenuItemResponse> menus) {
        if (menus == null || menus.isEmpty()) {
            return null;
        }

        long total = menus.stream()
                .map(ReservationMenuItemResponse::getPriceSnapshot)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        boolean hasPricedMenu = menus.stream()
                .map(ReservationMenuItemResponse::getPriceSnapshot)
                .anyMatch(java.util.Objects::nonNull);
        return hasPricedMenu ? total : null;
    }

    private Map<String, Object> buildChangeSnapshot(Reservation reservation) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("status", reservation.getStatus());
        snapshot.put("date", reservation.getDate());
        snapshot.put("time", reservation.getTime());
        snapshot.put("startAt", reservation.getStartAt());
        snapshot.put("staffId", reservation.getStaffId());
        snapshot.put("durationMinutes", reservation.getDurationMinutes());
        snapshot.put("confirmationMessage", reservation.getConfirmationMessage());
        snapshot.put("rejectionReason", reservation.getRejectionReason());
        snapshot.put("menus", loadMenuSnapshots(reservation.getId()));
        return snapshot;
    }

    private String serializeChangeSnapshot(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new IllegalStateException("예약 변경 이력 직렬화에 실패했습니다.", e);
        }
    }

    private ReservationChangeSnapshot buildReservationChangeSnapshot(
            LocalDate oldDate,
            LocalDate newDate,
            LocalTime oldStartAt,
            LocalTime newStartAt,
            Integer oldDurationMinutes,
            Integer newDurationMinutes,
            Long oldStaffId,
            Long newStaffId,
            Staff newStaff,
            ReservationChangeSnapshot.ValueChange<String> ownerMessageChange,
            ReservationChangeSnapshot.ValueChange<List<ReservationChangeSnapshot.MenuSnapshot>> menusChange
    ) {
        ReservationChangeSnapshot.ValueChange<LocalDate> dateChange = Objects.equals(oldDate, newDate)
                ? null
                : new ReservationChangeSnapshot.ValueChange<>(oldDate, newDate);
        ReservationChangeSnapshot.ValueChange<LocalTime> startAtChange = Objects.equals(oldStartAt, newStartAt)
                ? null
                : new ReservationChangeSnapshot.ValueChange<>(oldStartAt, newStartAt);
        ReservationChangeSnapshot.ValueChange<Integer> durationChange =
                Objects.equals(oldDurationMinutes, newDurationMinutes)
                        ? null
                        : new ReservationChangeSnapshot.ValueChange<>(oldDurationMinutes, newDurationMinutes);
        ReservationChangeSnapshot.StaffChange staffChange = Objects.equals(oldStaffId, newStaffId)
                ? null
                : new ReservationChangeSnapshot.StaffChange(
                        readStaffSnapshot(oldStaffId),
                        new ReservationChangeSnapshot.StaffSnapshot(newStaff.getId(), newStaff.getName())
                );

        return dateChange == null && startAtChange == null && durationChange == null
                && staffChange == null && ownerMessageChange == null && menusChange == null
                ? null
                : new ReservationChangeSnapshot(
                        dateChange,
                        startAtChange,
                        durationChange,
                        staffChange,
                        ownerMessageChange,
                        menusChange
                );
    }

    private ReservationChangeSnapshot.StaffSnapshot readStaffSnapshot(Long staffId) {
        if (staffId == null) {
            return new ReservationChangeSnapshot.StaffSnapshot(null, null);
        }
        try {
            Staff staff = staffReader.read(staffId);
            return new ReservationChangeSnapshot.StaffSnapshot(staff.getId(), staff.getName());
        } catch (StaffIdNotFoundException e) {
            return new ReservationChangeSnapshot.StaffSnapshot(staffId, null);
        }
    }

    private String normalizeOwnerMessage(String ownerMessage) {
        return ownerMessage == null || ownerMessage.isBlank() ? null : ownerMessage;
    }

    private Reservation.CancelTiming determineCancelTiming(Reservation reservation, LocalDateTime canceledAt) {
        return canceledAt.toLocalDate().isBefore(reservation.getDate())
                ? Reservation.CancelTiming.BEFORE_CUTOFF
                : Reservation.CancelTiming.AFTER_CUTOFF;
    }

    private String buildReservationCancelContent(String reason, Reservation.CancelTiming cancelTiming,
                                                 Boolean refundEligible) {
        StringBuilder builder = new StringBuilder();
        builder.append("예약이 취소되었습니다.\n");
        builder.append("취소 사유: ").append(reason).append('\n');
        builder.append("취소 시점: ").append(cancelTiming);
        if (refundEligible != null) {
            builder.append('\n').append("환불 가능 여부: ").append(refundEligible ? "가능" : "불가");
        }
        return builder.toString();
    }

    /**
     * 4. 예약 내역 조회 - 고객 전용: 내 예약 내역 조회
     */
    public List<ReservationCustomerResponse> getMyReservations(Long customerUserId) {
        List<Reservation> reservations = reservationReader.findByCustomerId(customerUserId);
        if (reservations.isEmpty()) {
            return List.of();
        }

        User customer = userReader.read(customerUserId);
        String customerName = customer.getName();

        Set<Long> shopIds = reservations.stream()
                .map(Reservation::getShopId)
                .collect(Collectors.toSet());

        Map<Long, String> shopNameById = shopReader.readAllByIds(new ArrayList<>(shopIds)).stream()
                .collect(Collectors.toMap(Shop::getId, Shop::getBusinessName, (a, b) -> a));

        return reservations.stream()
                .map(r -> {
                    String shopName = shopNameById.get(r.getShopId());
                    return ReservationCustomerResponse.from(r, customerName, shopName);
                })
                .toList();
    }

    /**
     * - 점주 젼용: 샵 예약 목록 조회
     */
    public List<ReservationOwnerResponse> getShopReservation(Long ownerUserId) {
        // 점주 권한 검증
        User user = userReader.read(ownerUserId);
        if (user.getUserType() != UserType.OWNER) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "예약 내역 조회 권한이 없습니다. (OWNER만 가능)");
        }

        // ownerUserId에 연결된 샵 ID 목록을 가져옴
        List<Long> shopIds = shopReader.findShopIdsByOwnerId(ownerUserId);

        if (shopIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Reservation> reservations = reservationReader.findByShopIdIn(shopIds);

        Set<Long> customerIds = reservations.stream()
                .map(Reservation::getCustomerId)
                .collect(Collectors.toSet());

        Map<Long, User> userById = userReader.readAllByIds(new ArrayList<>(customerIds)).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return reservations.stream()
                .map(r -> {
                    User customer = userById.get(r.getCustomerId());
                    return ReservationOwnerResponse.from(r, customer.getName(), customer.getPhoneNumber());
                })
                .toList();
    }

    /**
     * - 고객용: 샵 예약 가능 시간 조회
     *
     * @deprecated `booking entry -> staffId -> availability` 흐름으로 전환한 뒤 제거한다.
     */
    @Deprecated(forRemoval = false)
    public ReservationAvailabilityResponse getShopAvailability(Long shopId, LocalDate date) {
        // TODO(#115): 프론트가 staffId 기반 availability API로 전환되면 제거한다.
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        List<Reservation> reservations = reservationReader.findByShopIdAndDate(shopId, targetDate);

        // 🌟 취소와 거절 상태를 제외한 예약 시간 추출
        List<LocalTime> bookedTimes = reservations.stream()
                .filter(r -> r.getStatus() != Reservation.Status.CANCELED &&
                        r.getStatus() != Reservation.Status.REJECTED)
                .map(Reservation::getTime)
                .collect(Collectors.toList());

        return new ReservationAvailabilityResponse(targetDate, bookedTimes);
    }

    /**
     * 5. 채팅방 내 예약 내역 조회 - 고객용: 채팅방 내의 특정 샵에 자신이 했던 예약 내역 조회
     */
    public List<ReservationCustomerResponse> getCustomerReservationInChat(Long customerUserId, Long shopId) {
        List<Reservation> reservations = reservationReader.findByCustomerIdAndShopId(customerUserId, shopId);
        if (reservations.isEmpty()) {
            return List.of();
        }

        // 채팅방 내 "내 예약"이므로 고객/샵 정보는 각각 1번만 조회
        String customerName = userReader.read(customerUserId).getName();
        String shopName = shopReader.read(shopId).getBusinessName();

        return reservations.stream()
                .map(r -> {
                    return ReservationCustomerResponse.from(r, customerName, shopName);
                })
                .toList();
    }

    /**
     * - 점주용: 채팅방 내의 특정 고객 예약 내역 조회
     */
    public List<ReservationOwnerResponse> getReservationsByCustomerInShop(
            Long ownerUserId,
            Long shopId,
            Long customerId) {

        // 현재 로그인된 사용자가 이 샵의 소유자인지 확인
        if (!shopReader.isShopOwnedBy(shopId, ownerUserId)) {
            throw new AuthException(AuthErrorCode.ACCESS_DENIED, "해당 샵의 예약 내역을 조회할 권한이 없습니다. (소유자 불일치)");
        }

        List<Reservation> reservations = reservationReader.findByShopIdAndCustomerId(shopId, customerId);
        if (reservations.isEmpty()) {
            return List.of();
        }

        User customer = userReader.read(customerId);
        String customerName = customer.getName();
        String customerPhone = customer.getPhoneNumber();

        return reservations.stream()
                .map(r -> {
                    return ReservationOwnerResponse.from(r, customerName, customerPhone);
                })
                .toList();
    }

    // 3. 예약 취소 및 거절 로직 (Update)
    // 4. 고객 예약 내역 조회 로직 (Read)
    // 5. 원장님 샵 예약 목록 조회 로직 (Read)
}
