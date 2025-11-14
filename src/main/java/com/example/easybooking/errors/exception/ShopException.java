package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.ShopErrorCode;
import lombok.Getter;

@Getter
public class ShopException extends RuntimeException {

    private final ShopErrorCode shopErrorCode;

    public ShopException(ShopErrorCode errorCode) {
        super(errorCode.getMessage());
        this.shopErrorCode = errorCode;
    }
}


