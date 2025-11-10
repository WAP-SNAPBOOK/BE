package com.example.easybooking.errors.errorcode;

import com.example.easybooking.auth.RequireAuthenticatedUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum UserErrorCode implements ErrorCode {
    private final HttpStatus httpStatus;
    private final String message;
}
