package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.AuthErrorCode;

public class AuthException extends BaseBusinessException {
    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(AuthErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
