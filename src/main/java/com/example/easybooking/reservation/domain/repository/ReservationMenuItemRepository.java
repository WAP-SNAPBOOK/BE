package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.ReservationMenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationMenuItemRepository extends JpaRepository<ReservationMenuItem, Long> {

    List<ReservationMenuItem> findByReservationIdOrderBySortOrderAsc(Long reservationId);

    List<ReservationMenuItem> findByReservationIdIn(List<Long> reservationIds);

    void deleteByReservationId(Long reservationId);

    void deleteByReservationIdIn(List<Long> reservationIds);
}
