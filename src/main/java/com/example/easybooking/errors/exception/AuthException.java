package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.AuthErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{
    private final AuthErrorCode authErrorCode;
    public AuthException(AuthErrorCode errorCode) {
        this.authErrorCode = errorCode;
    }
}
