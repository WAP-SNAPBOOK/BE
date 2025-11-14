package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import lombok.Getter;

@Getter
public class ReservationException extends RuntimeException {

    private final ReservationErrorCode reservationErrorCode;

    public ReservationException(ReservationErrorCode errorCode) {
        super(errorCode.getMessage());
        this.reservationErrorCode = errorCode;
    }
}


