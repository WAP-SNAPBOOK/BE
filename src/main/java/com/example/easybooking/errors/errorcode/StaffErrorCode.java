package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum StaffErrorCode implements ErrorCode {

    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다."),
    DEFAULT_STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "기본 직원을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

