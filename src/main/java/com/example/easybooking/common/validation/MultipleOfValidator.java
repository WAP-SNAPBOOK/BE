package com.example.easybooking.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MultipleOfValidator implements ConstraintValidator<MultipleOf, Integer> {
    private int base;

    @Override
    public void initialize(MultipleOf annotation) {
        this.base = annotation.base();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (base == 0) {
            return false;
        }
        return value % base == 0;
    }
}