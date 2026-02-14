package com.example.easybooking.availability.dto.request;

import com.example.easybooking.availability.domain.HolidayType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateShopHolidayRequest {

    private HolidayType holidayType;
    private DayOfWeek dayOfWeek;
    private Integer weekOfMonth;
    private LocalDate referenceDate;
    private LocalDate specificDate;
}
