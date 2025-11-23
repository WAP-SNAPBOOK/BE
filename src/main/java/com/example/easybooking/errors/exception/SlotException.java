package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.SlotErrorCode;

public class SlotException extends BaseBusinessException {

    /**
     * Create a SlotException representing a slot-related error identified by the given error code.
     *
     * @param errorCode the SlotErrorCode that identifies the specific slot error
     */
    public SlotException(SlotErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Create a SlotException for the specified slot error code with an additional detail message.
     *
     * @param errorCode the SlotErrorCode that identifies the specific slot error
     * @param detailMessage an additional human-readable detail message describing the error
     */
    public SlotException(SlotErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}