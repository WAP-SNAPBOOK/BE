package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.AvailabilityErrorCode;

public class AvailabilityException extends BaseBusinessException {

    public AvailabilityException(AvailabilityErrorCode errorCode) {
        super(errorCode);
    }

    public AvailabilityException(AvailabilityErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
