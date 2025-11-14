package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ChatRoomErrorCode;
import lombok.Getter;

@Getter
public class ChatRoomException extends RuntimeException {
    private final ChatRoomErrorCode chatRoomErrorCode;

    public ChatRoomException(ChatRoomErrorCode errorCode) {
        super(errorCode.getMessage());
        this.chatRoomErrorCode = errorCode;
    }
}
