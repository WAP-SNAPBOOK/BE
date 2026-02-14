package com.example.easybooking.availability.dto.response;

import com.example.easybooking.availability.domain.HolidayType;
import com.example.easybooking.availability.domain.ShopHoliday;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopHolidayResponse {

    private Long holidayId;
    private HolidayType holidayType;
    private DayOfWeek dayOfWeek;
    private Integer weekOfMonth;
    private LocalDate referenceDate;
    private LocalDate specificDate;

    public static ShopHolidayResponse from(ShopHoliday shopHoliday) {
        return ShopHolidayResponse.builder()
                .holidayId(shopHoliday.getId())
                .holidayType(shopHoliday.getHolidayType())
                .dayOfWeek(shopHoliday.getDayOfWeek())
                .weekOfMonth(shopHoliday.getWeekOfMonth())
                .referenceDate(shopHoliday.getReferenceDate())
                .specificDate(shopHoliday.getSpecificDate())
                .build();
    }
}
