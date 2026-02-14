package com.example.easybooking.availability.dto.response;

import com.example.easybooking.availability.domain.ScheduleType;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopOperatingTimesResponse {

    private ScheduleType scheduleType;
    private Map<DayOfWeek, List<ShopTimeRangeResponse>> dayTimes;
}
