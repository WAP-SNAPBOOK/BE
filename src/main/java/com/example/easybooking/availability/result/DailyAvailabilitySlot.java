package com.example.easybooking.availability.result;

import java.time.LocalTime;

public record DailyAvailabilitySlot(
        LocalTime time,
        AvailabilitySlotStatus status
) {
}
