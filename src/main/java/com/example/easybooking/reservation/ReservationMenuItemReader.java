package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.domain.repository.ReservationMenuItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMenuItemReader {

    private final ReservationMenuItemRepository repository;

    public List<ReservationMenuItem> findByReservationId(Long reservationId) {
        return repository.findByReservationIdOrderBySortOrderAsc(reservationId);
    }

    public List<ReservationMenuItem> findByReservationIdIn(List<Long> reservationIds) {
        return repository.findByReservationIdIn(reservationIds);
    }
}
