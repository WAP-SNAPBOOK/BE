package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.UserErrorCode;

public class UserException extends BaseBusinessException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(UserErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
