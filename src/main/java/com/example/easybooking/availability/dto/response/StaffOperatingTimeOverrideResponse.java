package com.example.easybooking.availability.dto.response;

import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffOperatingTimeOverrideResponse {

    private DayOfWeek dayOfWeek;

    @JsonProperty("isOff")
    private boolean isOff;

    private LocalTime start;
    private LocalTime end;

    public static StaffOperatingTimeOverrideResponse from(StaffOperatingTime staffOperatingTime) {
        return StaffOperatingTimeOverrideResponse.builder()
                .dayOfWeek(staffOperatingTime.getDayOfWeek())
                .isOff(staffOperatingTime.isOff())
                .start(staffOperatingTime.getStartTime())
                .end(staffOperatingTime.getEndTime())
                .build();
    }
}
