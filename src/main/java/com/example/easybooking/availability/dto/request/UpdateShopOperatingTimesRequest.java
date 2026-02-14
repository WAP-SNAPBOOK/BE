package com.example.easybooking.availability.dto.request;

import com.example.easybooking.availability.domain.ScheduleType;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateShopOperatingTimesRequest {

    private ScheduleType scheduleType;
    private List<ShopTimeRangeRequest> times;
    private List<ShopTimeRangeRequest> weekdayTimes;
    private List<ShopTimeRangeRequest> weekendTimes;
    private Map<DayOfWeek, List<ShopTimeRangeRequest>> dayTimes;
}
