package com.example.easybooking.reservation.presentation;

import com.example.easybooking.auth.AuthenticatedUser;
import com.example.easybooking.auth.RequireAuthenticatedUser;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
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

    // TODO: [GET] 고객 예약 내역 조회 API
    // TODO: [PUT] 원장님 예약 확정 API
    // TODO: [PUT] 예약 취소/거절 API
}
