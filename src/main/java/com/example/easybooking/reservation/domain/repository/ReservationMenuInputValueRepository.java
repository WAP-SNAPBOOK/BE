package com.example.easybooking.reservation.domain.repository;

import com.example.easybooking.reservation.domain.ReservationMenuInputValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationMenuInputValueRepository extends JpaRepository<ReservationMenuInputValue, Long> {

    List<ReservationMenuInputValue> findByReservationMenuItemIdIn(List<Long> reservationMenuItemIds);
}
