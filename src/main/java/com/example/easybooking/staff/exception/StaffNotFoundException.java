package com.example.easybooking.staff.exception;

import com.example.easybooking.errors.errorcode.StaffErrorCode;
import com.example.easybooking.errors.exception.StaffException;

public class StaffNotFoundException extends StaffException {

    public StaffNotFoundException() {
        super(StaffErrorCode.DEFAULT_STAFF_NOT_FOUND);
    }
}

