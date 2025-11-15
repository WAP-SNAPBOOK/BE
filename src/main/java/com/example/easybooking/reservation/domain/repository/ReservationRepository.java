package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 4. 샵 ID와 채팅방 내의 고객 ID(CUSTOMER ID)로 예약 목록 조회 (점주용)
    List<Reservation> findByShopIdAndCustomerId(Long shopId, Long customerId);

    // 5. 샵 ID와 날짜로 예약 목록 조회 시 비관적 잠금 적용 (쓰기 전용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.shopId = :shopId AND r.date = :date")
    List<Reservation> findByShopIdAndDateForUpdate(
            @Param("shopId") Long shopId,
            @Param("date") LocalDate date
    );
}
