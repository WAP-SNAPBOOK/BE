package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.StaffErrorCode;

public class StaffException extends BaseBusinessException {

    public StaffException(StaffErrorCode errorCode) {
        super(errorCode);
    }

    public StaffException(StaffErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}

