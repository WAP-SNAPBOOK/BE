package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ChatErrorCode implements ErrorCode {
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메시지입니다."),
    MESSAGE_CONTENT_INVALID(HttpStatus.BAD_REQUEST, "메시지 내용이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

