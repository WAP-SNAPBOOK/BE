package com.example.easybooking.availability.result;

import java.time.LocalDate;
import java.util.List;

public record DailyAvailabilityResult(
        LocalDate date,
        int intervalMinutes,
        boolean holiday,
        List<DailyAvailabilitySlot> slots
) {
}
