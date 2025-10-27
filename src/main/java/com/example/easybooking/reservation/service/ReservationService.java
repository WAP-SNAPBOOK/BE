package com.example.easybooking.reservation.service;

import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);


    private final ReservationWriter reservationWriter;   // Writer 주입
    private final ReservationReader reservationReader;   // Reader 주입
    private final UserReader userReader;

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

    /**
     * 2. 원장님 예약 수락(확정) 로직 (Update)
     * - OWNER 권한 검증
     * - 샵 일치 검증 (본인 샵 예약만 처리 가능)
     * - Reservation 엔티티의 confirm() 메소드 호출
     */
    @Transactional
    public void confirmReservation(Long reservationId, Long ownerUserId) {
        // 1. 원장님(OWNER) 권한 검증
        User user = userReader.read(ownerUserId);

        if (user.getUserType() != UserType.OWNER) {
            throw new IllegalStateException("예약 확정 권한이 없습니다. (OWNER만 가능)");
        }

        // 2. 예약 엔티티 조회 및 샵 일치 여부 확인
        Reservation reservation = reservationReader.getById(reservationId);

        // 3. 샵 일치 검증: 예약된 샵 ID(Reservation.shopId)와 현재 원장님 ID가 일치하는지 확인
        if (!reservation.getShopId().equals(ownerUserId)) {
            throw new IllegalStateException("해당 샵의 예약에 대한 처리 권한이 없습니다.");
        }

        log.info("예약 ID: {} - 상태 변경 전: {}", reservationId, reservation.getStatus());

        reservation.confirm();
        log.info("예약 ID: {} - 상태 변경 후: {}", reservationId, reservation.getStatus());
    }

    // 3. 예약 취소 및 거절 로직 (Update)
    // 4. 고객 예약 내역 조회 로직 (Read)
    // 5. 원장님 샵 예약 목록 조회 로직 (Read)
}
