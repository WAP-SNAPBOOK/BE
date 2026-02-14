package com.example.easybooking.availability.exception;

import com.example.easybooking.errors.errorcode.AvailabilityErrorCode;
import com.example.easybooking.errors.exception.AvailabilityException;

public class ShopSettingsNotFoundException extends AvailabilityException {

    public ShopSettingsNotFoundException() {
        super(AvailabilityErrorCode.SHOP_SETTINGS_NOT_FOUND);
    }
}
