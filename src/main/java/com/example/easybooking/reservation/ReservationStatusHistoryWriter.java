package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationStatusHistory;
import com.example.easybooking.reservation.domain.repository.ReservationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationStatusHistoryWriter {

    private final ReservationStatusHistoryRepository repository;

    public ReservationStatusHistory save(
            Long reservationId,
            Reservation.Status fromStatus,
            Reservation.Status toStatus,
            Long changedByUserId,
            String reason
    ) {
        return repository.save(ReservationStatusHistory.create(
                reservationId,
                fromStatus,
                toStatus,
                changedByUserId,
                reason
        ));
    }
}
