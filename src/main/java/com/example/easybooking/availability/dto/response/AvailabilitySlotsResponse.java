package com.example.easybooking.availability.dto.response;

import com.example.easybooking.availability.result.DailyAvailabilityResult;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailabilitySlotsResponse {

    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private String date;
    private int intervalMinutes;
    private List<AvailabilitySlotResponse> slots;
    private boolean holiday;

    public static AvailabilitySlotsResponse of(DailyAvailabilityResult result) {
        return AvailabilitySlotsResponse.builder()
                .date(result.date().toString())
                .intervalMinutes(result.intervalMinutes())
                .slots(result.slots().stream()
                        .map(slot -> AvailabilitySlotResponse.of(slot, slot.time().format(SLOT_FORMATTER)))
                        .toList())
                .holiday(result.holiday())
                .build();
    }
}
