package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.NotificationErrorCode;

public class NotificationException extends BaseBusinessException {
    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode);
    }
}
