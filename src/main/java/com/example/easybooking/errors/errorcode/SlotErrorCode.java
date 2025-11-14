package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum SlotErrorCode implements ErrorCode {

    SHOP_NOT_FOUND_FOR_SLOT(HttpStatus.NOT_FOUND, "해당 매장을 찾을 수 없어 슬롯을 생성할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}


