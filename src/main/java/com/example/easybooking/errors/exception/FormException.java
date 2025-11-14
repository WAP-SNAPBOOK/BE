package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.FormErrorCode;
import lombok.Getter;

@Getter
public class FormException extends RuntimeException {

    private final FormErrorCode formErrorCode;

    public FormException(FormErrorCode errorCode) {
        super(errorCode.getMessage());
        this.formErrorCode = errorCode;
    }
}


