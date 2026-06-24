package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.ReservationChangeHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationChangeHistoryRepository extends JpaRepository<ReservationChangeHistory, Long> {

    List<ReservationChangeHistory> findByReservationIdOrderByCreatedAtDesc(Long reservationId);
}
