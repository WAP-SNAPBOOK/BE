package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ChatRoomErrorCode;

public class ChatRoomException extends BaseBusinessException {

    public ChatRoomException(ChatRoomErrorCode errorCode) {
        super(errorCode);
    }

    public ChatRoomException(ChatRoomErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
