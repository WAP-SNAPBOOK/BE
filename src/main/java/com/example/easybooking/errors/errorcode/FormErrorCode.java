package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum FormErrorCode implements ErrorCode {

    FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "폼을 찾을 수 없습니다."),
    DEFAULT_FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "기본 폼을 찾을 수 없습니다."),
    FORM_FOR_SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 매장의 폼을 찾을 수 없습니다."),
    FORM_FIELD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 필드를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}


