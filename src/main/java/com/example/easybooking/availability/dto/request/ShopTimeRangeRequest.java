package com.example.easybooking.availability.dto.request;

import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ShopTimeRangeRequest {

    private LocalTime start;
    private LocalTime end;
}
