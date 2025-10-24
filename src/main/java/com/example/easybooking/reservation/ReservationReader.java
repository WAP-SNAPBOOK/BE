package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationReader {

    private final ReservationRepository reservationRepository;

    /**
     * ID를 통해 DB에서 Reservation 엔티티를 찾고, 없으면 예외 발생
     */
    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약 ID를 찾을 수 없습니다: " + id));
    }



}
