package com.example.easybooking.availability.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailabilitySlotsResponse {

    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private String date;
    private List<String> slots;
    private boolean holiday;

    public static AvailabilitySlotsResponse of(LocalDate date, List<LocalTime> slots, boolean holiday) {
        return AvailabilitySlotsResponse.builder()
                .date(date.toString())
                .slots(slots.stream().map(slot -> slot.format(SLOT_FORMATTER)).toList())
                .holiday(holiday)
                .build();
    }
}
