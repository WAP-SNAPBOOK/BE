package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.UserErrorCode;

public class UserException extends BaseBusinessException {

    /**
     * Creates a UserException with the given user-specific error code.
     *
     * @param errorCode the UserErrorCode that identifies the user-related business error
     */
    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Create a user-related business exception with the given error code and an additional detail message.
     *
     * @param errorCode    the specific UserErrorCode representing the error condition
     * @param detailMessage an optional human-readable detail message providing additional context
     */
    public UserException(UserErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}