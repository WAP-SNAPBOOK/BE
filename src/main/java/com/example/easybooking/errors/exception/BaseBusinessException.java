package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ErrorCode;

public abstract class BaseBusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    /**
     * Creates a BaseBusinessException initialized with the given error code; the exception message is taken from the error code and no detail message is set.
     *
     * @param errorCode the ErrorCode describing the business error
     */
    protected BaseBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    /**
     * Constructs a BaseBusinessException with the given error code and an optional detailed message.
     *
     * The exception message will be set to {@code detailMessage} if it is non-null; otherwise it will use
     * {@code errorCode.getMessage()}.
     *
     * @param errorCode    the ErrorCode that identifies the business error
     * @param detailMessage an optional detailed message providing additional context, or {@code null} to use the error code's message
     */
    protected BaseBusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage != null ? detailMessage : errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    /**
     * Retrieve the error code associated with this exception.
     *
     * @return the stored ErrorCode representing the business error
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * The custom detail message associated with this exception.
     *
     * @return the detail message provided at construction, or null if none was provided
     */
    public String getDetailMessage() {
        return detailMessage;
    }
}