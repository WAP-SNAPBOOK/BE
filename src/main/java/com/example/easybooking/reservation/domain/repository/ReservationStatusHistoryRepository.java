package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.ReservationStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationStatusHistoryRepository extends JpaRepository<ReservationStatusHistory, Long> {

    List<ReservationStatusHistory> findByReservationIdOrderByCreatedAtDesc(Long reservationId);
}
