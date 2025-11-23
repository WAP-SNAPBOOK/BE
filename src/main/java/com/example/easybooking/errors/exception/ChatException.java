package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ChatErrorCode;

public class ChatException extends BaseBusinessException {

    public ChatException(ChatErrorCode errorCode) {
        super(errorCode);
    }

    public ChatException(ChatErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
