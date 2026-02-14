package com.example.easybooking.availability.exception;

import com.example.easybooking.errors.errorcode.AvailabilityErrorCode;
import com.example.easybooking.errors.exception.AvailabilityException;

public class BookingWindowExceededException extends AvailabilityException {

    public BookingWindowExceededException() {
        super(AvailabilityErrorCode.BOOKING_WINDOW_EXCEEDED);
    }
}
