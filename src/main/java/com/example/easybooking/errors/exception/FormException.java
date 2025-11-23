package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.FormErrorCode;

public class FormException extends BaseBusinessException {

    /**
     * Creates a FormException for the specified form error code.
     *
     * @param errorCode the form error code that categorizes this exception
     */
    public FormException(FormErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Creates a FormException associated with the given form error code and an additional detail message.
     *
     * @param errorCode    the FormErrorCode categorizing the error condition
     * @param detailMessage an optional human-readable detail message providing context about the error
     */
    public FormException(FormErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}