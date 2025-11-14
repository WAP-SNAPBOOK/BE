package com.example.easybooking.reservation.presentation;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.reservation.dto.ReservationConfirmRequest;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationRejectRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.reservation.dto.ReservationCustomerResponse;
import com.example.easybooking.reservation.dto.ReservationOwnerResponse;
import com.example.easybooking.reservation.dto.ReservationAvailabilityResponse;
import com.example.easybooking.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;


import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;

    /**
     * 고객 예약 신청 API
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationCreateRequest request,
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser) {

        Long userId = authenticatedUser.getUserId();
        log.info("인증된 사용자 내부 PK 추출 값: {}", userId);

        ReservationResponse response = reservationService.createReservation(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 원장님(OWNER) 예약 확정 API
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ReservationStatusResponse> confirmReservation(
            @PathVariable("id") Long reservationId,
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ReservationConfirmRequest request) {

        Long ownerUserId = authenticatedUser.getUserId();

        ReservationStatusResponse response = reservationService.confirmReservation(reservationId, ownerUserId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 원장님(OWNER) 예약 거절 API
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ReservationStatusResponse> rejectReservation(
            @PathVariable("id") Long reservationId,
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser,
            @Valid @RequestBody ReservationRejectRequest request) {

        Long ownerUserId = authenticatedUser.getUserId();

        ReservationStatusResponse response = reservationService.rejectReservation(reservationId, ownerUserId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 고객 전용: 내 예약 내역 조회 API
     */
    @GetMapping("/my")
    public ResponseEntity<List<ReservationCustomerResponse>>  getMyReservations(
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser) {

        Long customerUserId = authenticatedUser.getUserId();

        List<ReservationCustomerResponse> response = reservationService.getMyReservations(customerUserId);

        return ResponseEntity.ok(response);
    }

    /**
     * 점주 전용: 샵 예약 목록 조회
     */
    @GetMapping("/shop")
    public ResponseEntity<List<ReservationOwnerResponse>> getShopReservations(
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser) {

        Long ownerUserId = authenticatedUser.getUserId();

        List<ReservationOwnerResponse> response = reservationService.getShopReservation(ownerUserId);

        return ResponseEntity.ok(response);
    }

    /**
     * 고객용: 샵 예약 가능 시간 조회
     */
    @GetMapping("/shop/{shopId}/availability")
    public ResponseEntity<ReservationAvailabilityResponse> getShopAvailability(
            @PathVariable Long shopId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        ReservationAvailabilityResponse response = reservationService.getShopAvailability(shopId, date);

        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 내 예약 내역 조회
     * 고객용: 채팅방 내의 특정 샵에 자신이 했던 예약 내역 조회
     */
    @GetMapping("/chat/customer")
    public ResponseEntity<List<ReservationCustomerResponse>> getCustomerReservationInChat(
            @RequestParam Long shopId,
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser) {

        Long customerUserId = authenticatedUser.getUserId();

        List<ReservationCustomerResponse> response =
                reservationService.getCustomerReservationInChat(customerUserId, shopId);

        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 내 예약 내역 조회
     * 점주용: 채팅방 내의 특정 고객 예약 내역 조회
     */
    @GetMapping("/chat/owner")
    public ResponseEntity<List<ReservationOwnerResponse>> getOwnerReservationsForCustomer(
            @RequestParam Long shopId,
            @RequestParam Long customerId,
            @RequireAuthenticatedUser AuthenticatedUser authenticatedUser) {

        Long ownerUserId = authenticatedUser.getUserId();

        List<ReservationOwnerResponse> response =
                reservationService.getOwnerReservationsForCustomer(ownerUserId, shopId, customerId);

        return ResponseEntity.ok(response);
    }




    // TODO: [GET] 고객 예약 내역 조회 API
    // TODO: [PUT] 예약 취소/거절 API
}
