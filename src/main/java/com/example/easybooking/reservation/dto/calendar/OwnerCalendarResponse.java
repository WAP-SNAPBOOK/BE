package com.example.easybooking.reservation.dto.calendar;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerCalendarResponse {

    private Long shopId;
    private LocalDate selectedDate;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<OwnerCalendarDayResponse> days;
    private OwnerCalendarTimelineResponse timeline;
}
