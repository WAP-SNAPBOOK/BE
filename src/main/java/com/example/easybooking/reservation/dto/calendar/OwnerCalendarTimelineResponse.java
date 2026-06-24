package com.example.easybooking.reservation.dto.calendar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerCalendarTimelineResponse {

    private LocalDate date;
    private boolean holiday;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<OwnerCalendarTimeRangeResponse> workingRanges;
    private List<OwnerCalendarTimeRangeResponse> unavailableRanges;
    private List<OwnerCalendarStaffColumnResponse> staffColumns;
}
