package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.ReservationMenuInputValue;
import com.example.easybooking.reservation.domain.repository.ReservationMenuInputValueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMenuInputValueWriter {

    private final ReservationMenuInputValueRepository repository;

    public List<ReservationMenuInputValue> saveAll(List<ReservationMenuInputValue> values) {
        return repository.saveAll(values);
    }

    public void deleteByReservationMenuItemIds(List<Long> reservationMenuItemIds) {
        if (reservationMenuItemIds == null || reservationMenuItemIds.isEmpty()) {
            return;
        }
        repository.deleteByReservationMenuItemIdIn(reservationMenuItemIds);
        repository.flush();
    }
}
