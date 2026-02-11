package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.domain.repository.ReservationMenuItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMenuItemWriter {

    private final ReservationMenuItemRepository repository;

    public List<ReservationMenuItem> saveAll(List<ReservationMenuItem> items) {
        return repository.saveAll(items);
    }
}
