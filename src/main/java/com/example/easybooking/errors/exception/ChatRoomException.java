package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ChatRoomErrorCode;
import lombok.Getter;

@Getter
public class ChatRoomException extends RuntimeException {
    private final ChatRoomErrorCode chatRoomErrorCode;

    public ChatRoomException(ChatRoomErrorCode chatRoomErrorCode) {
        this.chatRoomErrorCode = chatRoomErrorCode;
    }
}
