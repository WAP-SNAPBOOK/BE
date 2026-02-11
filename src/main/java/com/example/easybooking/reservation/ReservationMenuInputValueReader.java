package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.ReservationMenuInputValue;
import com.example.easybooking.reservation.domain.repository.ReservationMenuInputValueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMenuInputValueReader {

    private final ReservationMenuInputValueRepository repository;

    public List<ReservationMenuInputValue> findByReservationMenuItemIds(List<Long> menuItemIds) {
        if (menuItemIds == null || menuItemIds.isEmpty()) {
            return List.of();
        }
        return repository.findByReservationMenuItemIdIn(menuItemIds);
    }
}
