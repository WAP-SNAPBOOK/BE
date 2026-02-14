package com.example.easybooking.availability.exception;

import com.example.easybooking.errors.errorcode.AvailabilityErrorCode;
import com.example.easybooking.errors.exception.AvailabilityException;

public class StaffOverrideOutOfShopRangeException extends AvailabilityException {

    public StaffOverrideOutOfShopRangeException() {
        super(AvailabilityErrorCode.STAFF_OVERRIDE_OUT_OF_SHOP_RANGE);
    }
}
