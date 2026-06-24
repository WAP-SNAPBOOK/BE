package com.example.easybooking.reservation.dto.calendar;

import com.example.easybooking.reservation.domain.Reservation;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerCalendarReservationResponse {

    private Long reservationId;
    private Reservation.Status status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer durationMinutes;
    private Integer displayDurationMinutes;
    private Long staffId;
    private String customerName;
    private String representativeMenuName;
    private int menuCount;
    private String menuSummary;
}
