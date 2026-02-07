package com.example.easybooking.staff.exception;

import com.example.easybooking.errors.errorcode.StaffErrorCode;
import com.example.easybooking.errors.exception.StaffException;

public class StaffIdNotFoundException extends StaffException {

    public StaffIdNotFoundException() {
        super(StaffErrorCode.STAFF_NOT_FOUND);
    }
}

