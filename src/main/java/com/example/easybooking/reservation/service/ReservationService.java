package com.example.easybooking.reservation.service;

import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {


    private final ReservationWriter reservationWriter;   // Writer 주입
    private final ReservationReader reservationReader;   // Reader 주입

    private final UserService userService;

    /**
     * 1. 고객 예약 신청 로직 (Create)
     */
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request, Long userId) {
        Long customerUserId = userId;

        // 현재는 샵 ID와 담담 원장님 ID를 모두 shopId로 설정함. 추후 확장 예정.
        // 현재 shopId는 원장님 User.id
        Long shopId = request.getShopId();
        Long ownerUserId = shopId;

        Reservation newReservation = Reservation.createReservation(
                shopId,
                ownerUserId,
                customerUserId,
                request.getDate(),
                request.getTime(),
                request.getDesignImageURL()
        );

        Reservation savedReservation = reservationWriter.save(newReservation);
        return new ReservationResponse(savedReservation);
    }




    // 2. 원장님 예약 확정 로직 (Update)
    // 3. 예약 취소 및 거절 로직 (Update)
    // 4. 고객 예약 내역 조회 로직 (Read)
    // 5. 원장님 샵 예약 목록 조회 로직 (Read)
}
