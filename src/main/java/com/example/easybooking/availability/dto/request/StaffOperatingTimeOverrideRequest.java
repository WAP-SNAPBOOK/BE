package com.example.easybooking.availability.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StaffOperatingTimeOverrideRequest {

    private DayOfWeek dayOfWeek;

    @JsonProperty("isOff")
    private boolean isOff;

    private LocalTime start;
    private LocalTime end;
}
