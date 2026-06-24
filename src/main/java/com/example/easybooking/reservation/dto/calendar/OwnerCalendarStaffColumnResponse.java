package com.example.easybooking.reservation.dto.calendar;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerCalendarStaffColumnResponse {

    private Long staffId;
    private String staffName;
    private boolean unassigned;
    private List<OwnerCalendarTimeRangeResponse> workingRanges;
    private List<OwnerCalendarTimeRangeResponse> unavailableRanges;
    private List<OwnerCalendarReservationResponse> reservations;
}
