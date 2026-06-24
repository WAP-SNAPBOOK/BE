package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.ReservationChangeHistory;
import com.example.easybooking.reservation.domain.repository.ReservationChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationChangeHistoryWriter {

    private final ReservationChangeHistoryRepository repository;

    public ReservationChangeHistory save(
            Long reservationId,
            Long changedByUserId,
            String changeType,
            String beforeJson,
            String afterJson,
            String reason
    ) {
        return repository.save(ReservationChangeHistory.create(
                reservationId,
                changedByUserId,
                changeType,
                beforeJson,
                afterJson,
                reason
        ));
    }
}
