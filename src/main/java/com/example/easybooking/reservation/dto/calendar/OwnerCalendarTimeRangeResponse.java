package com.example.easybooking.reservation.dto.calendar;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerCalendarTimeRangeResponse {

    private LocalTime startTime;
    private LocalTime endTime;
}
