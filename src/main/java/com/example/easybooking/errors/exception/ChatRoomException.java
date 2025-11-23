package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ChatRoomErrorCode;

public class ChatRoomException extends BaseBusinessException {

    /**
     * Constructs a ChatRoomException representing the specified chat-room error.
     *
     * @param errorCode the ChatRoomErrorCode that categorizes this chat-room error
     */
    public ChatRoomException(ChatRoomErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Creates a ChatRoomException categorized by the given ChatRoomErrorCode with an additional detail message.
     *
     * @param errorCode the ChatRoomErrorCode categorizing the error
     * @param detailMessage additional detail message describing the error condition
     */
    public ChatRoomException(ChatRoomErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}