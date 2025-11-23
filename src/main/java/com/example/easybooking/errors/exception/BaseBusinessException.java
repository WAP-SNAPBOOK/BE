package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ErrorCode;

public abstract class BaseBusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    protected BaseBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    protected BaseBusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage != null ? detailMessage : errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }
}