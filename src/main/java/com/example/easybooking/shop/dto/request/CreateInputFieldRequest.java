package com.example.easybooking.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateInputFieldRequest {

    @NotBlank
    private String label;

    @NotNull
    private String inputType;

    @NotNull
    private Boolean required;

    private BigDecimal minValue;
    private BigDecimal maxValue;
    private BigDecimal stepValue;
    private Integer maxLength;
    private String placeholder;
    private int sortOrder;

    public CreateInputFieldRequest(String label, String inputType, Boolean required,
                                   BigDecimal minValue, BigDecimal maxValue, BigDecimal stepValue,
                                   Integer maxLength, String placeholder, int sortOrder) {
        this.label = label;
        this.inputType = inputType;
        this.required = required;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepValue = stepValue;
        this.maxLength = maxLength;
        this.placeholder = placeholder;
        this.sortOrder = sortOrder;
    }
}
