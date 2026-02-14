package com.example.easybooking.availability.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateShopScheduleSettingsRequest {

    private int intervalMinutes;
    private Integer bookingWindowDays;
    private Integer minBookingLeadMinutes;
    private Boolean publicHolidayOff;
}
