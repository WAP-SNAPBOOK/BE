package com.example.easybooking.reservation;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationReader {

    private final ReservationRepository reservationRepository;

    /**
     * ID를 통해 DB에서 Reservation 엔티티를 찾고, 없으면 예외 발생
     */
    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    // 1. 고객 ID로 모든 예약 목록 조회 (최신순 정렬)
    public List<Reservation> findByCustomerId(Long customerId) {
        return reservationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    // 2. 샵 ID로 모든 예약 목록 조회 (점주용)
    public List<Reservation> findByShopId(Long shopId) {
        return reservationRepository.findByShopId(shopId);
    }

    // 3. 샵 ID와 날짜로 예약 목록 조회 (고객용: 예약 가능 시간 확인)
    public List<Reservation> findByShopIdAndDate(Long shopId, LocalDate date) {
        return reservationRepository.findByShopIdAndDate(shopId, date);
    }

    // 4. 샵 ID 목록으로 모든 예약 목록 조회 (최신순 정렬)
    public List<Reservation> findByShopIdIn(List<Long> shopIds) {
        return reservationRepository.findByShopIdInOrderByCreatedAtDesc(shopIds);
    }

    // 4. 샵 ID와 채팅방 내 고객 ID(CUSTOMER ID)로 예약 목록 조회 (점주용, 최신순 정렬)
    public List<Reservation> findByShopIdAndCustomerId(Long shopId, Long customerId) {
        return reservationRepository.findByShopIdAndCustomerIdOrderByCreatedAtDesc(shopId, customerId);
    }

    // 5. 샵 ID와 날짜로 예약 목록 조회 (쓰기 전용: 비관적 잠금 적용)
    public List<Reservation> findByShopIdAndDateForUpdate(Long shopId, LocalDate date) {
        return reservationRepository.findByShopIdAndDateForUpdate(shopId, date);
    }
}
