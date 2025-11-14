package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.SlotErrorCode;
import lombok.Getter;

@Getter
public class SlotException extends RuntimeException {

    private final SlotErrorCode slotErrorCode;

    public SlotException(SlotErrorCode errorCode) {
        super(errorCode.getMessage());
        this.slotErrorCode = errorCode;
    }
}


