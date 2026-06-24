package com.example.easybooking.reservation.dto.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerCalendarDayResponse {

    private LocalDate date;
    private DayOfWeek dayOfWeek;
    private String dayLabel;
    private int dayOfMonth;
    private boolean selected;
    private boolean holiday;
    private boolean hasPending;
    private boolean hasConfirmed;
}
