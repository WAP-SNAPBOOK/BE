package com.example.easybooking.shop.dto.response;

import com.example.easybooking.shop.domain.ShopMenuInputField;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class InputFieldResponse {

    private final Long id;
    private final Long shopMenuId;
    private final String label;
    private final String inputType;
    private final Boolean required;
    private final BigDecimal minValue;
    private final BigDecimal maxValue;
    private final BigDecimal stepValue;
    private final Integer maxLength;
    private final String placeholder;
    private final Integer sortOrder;
    private final Boolean isActive;

    public InputFieldResponse(ShopMenuInputField field) {
        this.id = field.getId();
        this.shopMenuId = field.getShopMenuId();
        this.label = field.getLabel();
        this.inputType = field.getInputType().name();
        this.required = field.getRequired();
        this.minValue = field.getMinValue();
        this.maxValue = field.getMaxValue();
        this.stepValue = field.getStepValue();
        this.maxLength = field.getMaxLength();
        this.placeholder = field.getPlaceholder();
        this.sortOrder = field.getSortOrder();
        this.isActive = field.getIsActive();
    }
}
