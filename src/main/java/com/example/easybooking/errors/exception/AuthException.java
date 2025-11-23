package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.AuthErrorCode;

public class AuthException extends BaseBusinessException {
    /**
     * Creates an AuthException representing the specified authentication error.
     *
     * @param errorCode the AuthErrorCode indicating the authentication failure reason
     */
    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Create an AuthException with the given authentication error code and detail message.
     *
     * @param errorCode     the AuthErrorCode that identifies the authentication error
     * @param detailMessage an optional human-readable detail message to include with the error
     */
    public AuthException(AuthErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}