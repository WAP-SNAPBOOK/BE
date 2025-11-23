package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;

public class ReservationException extends BaseBusinessException {

    public ReservationException(ReservationErrorCode errorCode) {
        super(errorCode);
    }

    public ReservationException(ReservationErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
