package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ChatErrorCode;
import lombok.Getter;

@Getter
public class ChatException extends RuntimeException {

    private final ChatErrorCode chatErrorCode;

    public ChatException(ChatErrorCode errorCode) {
        super(errorCode.getMessage());
        this.chatErrorCode = errorCode;
    }
}


