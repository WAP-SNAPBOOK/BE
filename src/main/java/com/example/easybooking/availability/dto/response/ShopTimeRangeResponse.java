package com.example.easybooking.availability.dto.response;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopTimeRangeResponse {

    private LocalTime start;
    private LocalTime end;
}
