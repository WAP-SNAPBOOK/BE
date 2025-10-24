package com.example.easybooking.reservation;


import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationWriter {

    private final ReservationRepository reservationRepository;

    /**
     * 새로운 예약 DB 저장 및 변경 사항 반영
     */
    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    /**
     * 예약 삭제 (delete)
     */


}
