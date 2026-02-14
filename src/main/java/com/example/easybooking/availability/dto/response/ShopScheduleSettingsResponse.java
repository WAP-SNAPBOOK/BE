package com.example.easybooking.availability.dto.response;

import com.example.easybooking.availability.domain.ScheduleType;
import com.example.easybooking.availability.domain.ShopSettings;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopScheduleSettingsResponse {

    private Long shopId;
    private int intervalMinutes;
    private ScheduleType scheduleType;
    private int bookingWindowDays;
    private int minBookingLeadMinutes;
    private boolean publicHolidayOff;

    public static ShopScheduleSettingsResponse from(ShopSettings shopSettings) {
        return ShopScheduleSettingsResponse.builder()
                .shopId(shopSettings.getShopId())
                .intervalMinutes(shopSettings.getIntervalMinutes())
                .scheduleType(shopSettings.getScheduleType())
                .bookingWindowDays(shopSettings.getBookingWindowDays())
                .minBookingLeadMinutes(shopSettings.getMinBookingLeadMinutes())
                .publicHolidayOff(shopSettings.isPublicHolidayOff())
                .build();
    }
}
