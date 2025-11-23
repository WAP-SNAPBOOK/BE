package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.SlotErrorCode;

public class SlotException extends BaseBusinessException {

    public SlotException(SlotErrorCode errorCode) {
        super(errorCode);
    }

    public SlotException(SlotErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
