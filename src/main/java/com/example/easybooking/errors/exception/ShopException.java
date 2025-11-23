package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ShopErrorCode;

public class ShopException extends BaseBusinessException {

    /**
     * Constructs a ShopException that represents the given shop error code.
     *
     * @param errorCode the {@link ShopErrorCode} identifying the specific shop error
     */
    public ShopException(ShopErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Create a ShopException that associates a shop-specific error code with an additional detail message.
     *
     * @param errorCode     the shop-specific error code identifying the error condition
     * @param detailMessage additional context describing the specific error occurrence
     */
    public ShopException(ShopErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}