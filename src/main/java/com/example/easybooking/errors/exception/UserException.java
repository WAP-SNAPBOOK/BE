package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.UserErrorCode;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    private final UserErrorCode userErrorCode;

    public UserException(UserErrorCode errorCode) {
        this.userErrorCode = errorCode;
    }
}
