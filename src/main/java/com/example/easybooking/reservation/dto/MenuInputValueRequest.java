package com.example.easybooking.reservation.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuInputValueRequest {
    private final Long fieldId;
    private final BigDecimal valueNumber;
    private final String valueText;
}
