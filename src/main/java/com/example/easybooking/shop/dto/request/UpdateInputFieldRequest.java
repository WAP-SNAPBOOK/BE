package com.example.easybooking.shop.dto.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateInputFieldRequest {

    private String label;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private BigDecimal stepValue;
    private Integer maxLength;
    private String placeholder;
    private Integer sortOrder;

    public UpdateInputFieldRequest(String label, BigDecimal minValue, BigDecimal maxValue,
                                   BigDecimal stepValue, Integer maxLength, String placeholder,
                                   Integer sortOrder) {
        this.label = label;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepValue = stepValue;
        this.maxLength = maxLength;
        this.placeholder = placeholder;
        this.sortOrder = sortOrder;
    }
}
