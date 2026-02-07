package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeBlockRepository extends JpaRepository<ReservationTimeBlock, Long> {
}

