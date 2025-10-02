package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
