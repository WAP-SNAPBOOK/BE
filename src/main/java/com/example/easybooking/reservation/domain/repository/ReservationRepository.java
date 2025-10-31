package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 1. 고객 ID로 모든 예약 목록 조회
    List<Reservation> findByCustomerId(Long customerId);

    // 2. 샵 ID로 모든 예약 목록 조회 (점주용)
    List<Reservation> findByShopId(Long shopId);

    // 3. 샵 ID와 날짜로 예약 목록 조회 (고객용: 예약 가능 시간 확인)
    List<Reservation> findByShopIdAndDate(Long shopId, LocalDate date);
    List<Reservation> findByShopIdIn(List<Long> shopIds);
}
