package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ChatErrorCode;

public class ChatException extends BaseBusinessException {

    /**
     * Constructs a ChatException for the specified chat error code.
     *
     * Associates the given ChatErrorCode with this exception so callers can
     * inspect the specific chat error condition.
     *
     * @param errorCode the ChatErrorCode describing the chat error condition
     */
    public ChatException(ChatErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a ChatException representing the specified chat error with an additional detail message.
     *
     * @param errorCode the ChatErrorCode identifying the specific chat error
     * @param detailMessage additional information about the error; may be {@code null}
     */
    public ChatException(ChatErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}