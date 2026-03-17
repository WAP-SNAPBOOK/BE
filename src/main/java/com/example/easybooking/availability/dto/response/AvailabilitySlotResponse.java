package com.example.easybooking.availability.dto.response;

import com.example.easybooking.availability.result.DailyAvailabilitySlot;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailabilitySlotResponse {

    private String time;
    private String status;

    public static AvailabilitySlotResponse of(DailyAvailabilitySlot slot, String formattedTime) {
        return AvailabilitySlotResponse.builder()
                .time(formattedTime)
                .status(slot.status().name())
                .build();
    }
}
