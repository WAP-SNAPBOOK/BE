package com.example.easybooking.availability.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopHolidaysResponse {

    private List<ShopHolidayResponse> holidays;
}
