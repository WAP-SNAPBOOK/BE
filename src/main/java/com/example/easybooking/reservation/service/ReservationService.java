package com.example.easybooking.reservation.service;

import com.example.easybooking.form.FormParsingUtil;
import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.dto.*;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);


    private final ReservationWriter reservationWriter;   // Writer 주입
    private final ReservationReader reservationReader;   // Reader 주입
    private final UserReader userReader;
    private final ShopReader shopReader;

    private final ObjectMapper objectMapper;


    /**
     * 1. 고객 예약 신청 로직 (Create)
     */
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request, Long userId) {
        Long customerUserId = userId;
        Long shopId = request.getShopId();

        Shop shop = shopReader.read(shopId);
        Long ownerUserId = shop.getOwnerId();

        Map<String, String> formData = request.getFormData();
        String formDataJson;

        try {
            formDataJson = objectMapper.writeValueAsString(formData);
        } catch (JsonProcessingException e) {
            throw new ReservationException(ReservationErrorCode.INVALID_FORM_JSON);
        }

        // 날짜 추출 및 변환
        String dateString = formData.get("date");
        if (dateString == null) {
            throw new ReservationException(ReservationErrorCode.REQUIRED_DATE_MISSING);
        }
        LocalDate date = LocalDate.parse(dateString);

        // 시간 추출 및 변환
        String timeString = formData.get("time");
        if (timeString == null) {
            throw new ReservationException(ReservationErrorCode.REQUIRED_TIME_MISSING);
        }
        LocalTime time = LocalTime.parse(timeString);

        // 예약 가능 시간 검증
        ReservationAvailabilityResponse availability = getShopAvailability(shopId, date);

        if (availability.getBookedTimes().contains(time)) {
            log.warn("중복 예약 시도 감지: ShopId={}, Date={}, Time={}", shopId, date, time);
            throw new IllegalArgumentException("선택하신 시간(" + time + ")은 이미 예약되었거나 접수 대기 중입니다.");
        }

        // 디자인 사진 URL 추출
        String photoJsonString = formData.get("photo");
        List<String> designImageURLs;

        if (photoJsonString == null || photoJsonString.trim().isEmpty()) {
            designImageURLs = Collections.emptyList();
        } else {
            try {
                designImageURLs = objectMapper.readValue(photoJsonString, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                throw new ReservationException(ReservationErrorCode.INVALID_PHOTO_JSON);
            }
        }

        User customer = userReader.read(customerUserId);
        String customerName = customer.getName();
        int photoCount = designImageURLs.size();

        // 폼 데이터에서 상세 필드값 추출
        String part = formData.get("part");
        String removal = formData.get("removal");
        String requests = formData.get("requests");

        Integer extendCount = FormParsingUtil.parseSafeInteger(formData.get("extend"));
        Integer wrappingCount = FormParsingUtil.parseSafeInteger(formData.get("wrapping"));


        Reservation newReservation = Reservation.createReservation(
                shopId,
                ownerUserId,
                customerUserId,
                date,
                time,
                formDataJson,
                designImageURLs
        );

        Reservation savedReservation = reservationWriter.save(newReservation);
        return new ReservationResponse(
                savedReservation,
                customerName,
                photoCount,
                part,
                removal,
                extendCount,
                wrappingCount,
                designImageURLs,
                requests);
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
        }
        return response;
    }

    /**
     * 2. 원장님 예약 수락(확정) 로직 (Update)
     * - OWNER 권한 검증
     * - 샵 일치 검증 (본인 샵 예약만 처리 가능)
     * - Reservation 엔티티의 confirm() 메소드 호출
     */
    @Transactional
    public ReservationStatusResponse confirmReservation(Long reservationId, Long ownerUserId, ReservationConfirmRequest request) {
        // 1. 원장님(OWNER) 권한 검증
        User user = userReader.read(ownerUserId);

        if (user.getUserType() != UserType.OWNER) {
            throw new IllegalStateException("예약 확정 권한이 없습니다. (OWNER만 가능)");
        }

        // 2. 예약 엔티티 조회 및 샵 일치 여부 확인
        Reservation reservation = reservationReader.getById(reservationId);

        // 3. 샵 일치 검증: 예약된 샵 ID(Reservation.shopId)와 현재 원장님 ID가 일치하는지 확인
        if (!reservation.getOwnerUserId().equals(ownerUserId)) {
            throw new AccessDeniedException("해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }

        // 4. 엔티티 상태 변경
        log.info("예약 ID: {} - 상태 변경 전: {}", reservationId, reservation.getStatus());
        reservation.confirm(request.getMessage());
        log.info("예약 ID: {} - 상태 변경 후: {}", reservationId, reservation.getStatus());

        // 5. 고객명 조회
        User customer = userReader.read(reservation.getCustomerId());
        String customerName = customer.getName();

        // 6. 응답 DTO 생성 & 반환
        return buildStatusResponse(reservation, customerName);
    }

    /**
     * 3. 예약 거절 로직 (Update)
     */
    @Transactional
    public ReservationStatusResponse rejectReservation(Long reservationId, Long ownerUserId, ReservationRejectRequest request) {
        // 1. 원장님(owner) 권한 검증
        User user = userReader.read(ownerUserId);
        if (user.getUserType() != UserType.OWNER) {
            throw new IllegalStateException("예약 거절 권한이 없습니다. (OWNER만 가능)");
        }

        // 2. 예약 엔티티 조회 및 샵 일치 여부 확인
        Reservation reservation = reservationReader.getById(reservationId);

        // 3. 샵 일치 검증
        if (!reservation.getOwnerUserId().equals(ownerUserId)) {
            throw new AccessDeniedException("해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }

        // 4. 엔티티 상태 변경
        log.info("예약 ID: {} - 거절 전 상태: {}", reservationId, reservation.getStatus());
        reservation.reject(request.getReason());
        log.info("예약 ID: {} - 거절 후 상태: {}", reservationId, reservation.getStatus());

        // 5. 고객명 조회
        User customer = userReader.read(reservation.getCustomerId());
        String customerName = customer.getName();

        // 6. 응답 DTO 생성 및 반환
        return buildStatusResponse(reservation, customerName);
        // TODO: 고객에게 거절 알림
    }

    /**
     * 4. 예약 내역 조회
     * - 고객 전용: 내 예약 내역 조회
     */
    public List<ReservationCustomerResponse> getMyReservations(Long customerUserId) {
        List<Reservation> reservations = reservationReader.findByCustomerId(customerUserId);

        return reservations.stream()
                .map(r -> ReservationCustomerResponse.from(r, userReader, shopReader))
                .toList();
    }

    /**
     * - 점주 젼용: 샵 예약 목록 조회
     */
    public List<ReservationOwnerResponse> getShopReservation(Long ownerUserId) {
        // 점주 권한 검증
        User user = userReader.read(ownerUserId);
        if (user.getUserType() != UserType.OWNER) {
            throw new AccessDeniedException("샵 예약 목록 조회 권한이 없습니다. (OWNER만 가능)");
        }

        // ownerUserId에 연결된 샵 ID 목록을 가져옴
        List<Long> shopIds = shopReader.findShopIdsByOwnerId(ownerUserId);

        if (shopIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Reservation> reservations = reservationReader.findByShopIdIn(shopIds);

        return reservations.stream()
                .map(r -> ReservationOwnerResponse.from(r, userReader))
                .toList();
    }

    /**
     * - 고객용: 샵 예약 가능 시간 조회
     */
    public ReservationAvailabilityResponse getShopAvailability(Long shopId, LocalDate date) {
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
     * 5. 채팅방 내 예약 내역 조회
     * - 고객용: 채팅방 내의 특정 샵에 자신이 했던 예약 내역 조회
     */
    public List<ReservationCustomerResponse> getCustomerReservationInChat(Long customerUserId, Long shopId) {
        List<Reservation> allReservations = reservationReader.findByCustomerId(customerUserId);

        // 채팅방의 shopID와 일치하는 예약만 필터링
        List<Reservation> filteredList = allReservations.stream()
                .filter(r -> r.getShopId().equals(shopId))
                .toList();

        return filteredList.stream()
                .map(r -> ReservationCustomerResponse.from(r, userReader, shopReader))
                .toList();
    }

    /**
     * - 점주용: 채팅방 내의 특정 고객 예약 내역 조회
     */
    public List<ReservationOwnerResponse> getOwnerReservationsForCustomer(
            Long ownerUserId,
            Long shopId,
            Long customerId) {

        // 현재 로그인된 사용자가 이 샵의 소유자인지 확인
        if (!shopReader.isShopOwnedBy(shopId, ownerUserId)) {
            throw new AccessDeniedException("해당 샵의 예약 내역을 조회할 권한이 없습니다. (소유자 불일치)");
        }

        List<Reservation> reservations = reservationReader.findByShopIdAndCustomerId(shopId, customerId);

        return reservations.stream()
                .map(r -> ReservationOwnerResponse.from(r, userReader))
                .toList();
    }


    // 3. 예약 취소 및 거절 로직 (Update)
    // 4. 고객 예약 내역 조회 로직 (Read)
    // 5. 원장님 샵 예약 목록 조회 로직 (Read)
}
