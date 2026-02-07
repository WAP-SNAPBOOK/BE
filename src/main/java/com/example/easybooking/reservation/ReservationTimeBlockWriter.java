package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationTimeBlockWriter {

    private final ReservationTimeBlockRepository reservationTimeBlockRepository;

    public List<ReservationTimeBlock> saveAll(List<ReservationTimeBlock> blocks) {
        return reservationTimeBlockRepository.saveAll(blocks);
    }
}

