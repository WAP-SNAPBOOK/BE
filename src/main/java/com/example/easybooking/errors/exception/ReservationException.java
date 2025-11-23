package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;

public class ReservationException extends BaseBusinessException {

    /**
     * Creates a ReservationException associated with the specified reservation error code.
     *
     * @param errorCode the reservation-specific error code that identifies the business error
     */
    public ReservationException(ReservationErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a ReservationException for the given reservation error code with an additional detail message.
     *
     * @param errorCode    the reservation-specific error code describing the failure condition
     * @param detailMessage an optional human-readable message that provides additional context about the error
     */
    public ReservationException(ReservationErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}