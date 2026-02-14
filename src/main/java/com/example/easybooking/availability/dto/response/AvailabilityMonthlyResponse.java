package com.example.easybooking.availability.dto.response;

import java.time.YearMonth;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailabilityMonthlyResponse {

    private String yearMonth;
    private List<Integer> availableDates;
    private List<Integer> holidayDates;
    private List<Integer> closedDates;

    public static AvailabilityMonthlyResponse of(
            YearMonth yearMonth,
            List<Integer> availableDates,
            List<Integer> holidayDates,
            List<Integer> closedDates
    ) {
        return AvailabilityMonthlyResponse.builder()
                .yearMonth(yearMonth.toString())
                .availableDates(availableDates)
                .holidayDates(holidayDates)
                .closedDates(closedDates)
                .build();
    }
}
