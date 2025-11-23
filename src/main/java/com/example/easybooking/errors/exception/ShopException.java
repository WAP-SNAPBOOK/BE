package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ShopErrorCode;

public class ShopException extends BaseBusinessException {

    public ShopException(ShopErrorCode errorCode) {
        super(errorCode);
    }

    public ShopException(ShopErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
