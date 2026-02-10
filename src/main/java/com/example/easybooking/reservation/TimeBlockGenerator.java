package com.example.easybooking.reservation;

import com.example.easybooking.reservation.domain.ReservationTimeBlock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public final class TimeBlockGenerator {
    private final int unitMinutes = 10;

    public List<ReservationTimeBlock> generate(Long reservationId, Long staffId, LocalDateTime startAt,
                                               int durationMinutes) {
        int count = durationMinutes / unitMinutes;
        return IntStream.range(0, count)
                .mapToObj(i -> ReservationTimeBlock.create(
                        reservationId,
                        staffId,
                        startAt.plusMinutes((long) i * unitMinutes)
                ))
                .toList();
    }
}