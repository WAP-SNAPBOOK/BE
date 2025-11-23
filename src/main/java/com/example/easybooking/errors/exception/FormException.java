package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.FormErrorCode;

public class FormException extends BaseBusinessException {

    public FormException(FormErrorCode errorCode) {
        super(errorCode);
    }

    public FormException(FormErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
