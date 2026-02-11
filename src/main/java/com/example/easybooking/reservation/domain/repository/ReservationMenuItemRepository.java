package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.ReservationMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationMenuItemRepository extends JpaRepository<ReservationMenuItem, Long> {
}
