package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ShopErrorCode implements ErrorCode {

    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 매장입니다."),
    SHOP_NOT_FOUND_FOR_OWNER(HttpStatus.NOT_FOUND, "점주가 소유한 매장을 찾을 수 없습니다."),
    INVALID_SHOP_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 매장 ID입니다."),
    SHOP_MENU_MISMATCH(HttpStatus.BAD_REQUEST, "매장과 메뉴 소속이 일치하지 않습니다."),
    SHOP_TAG_MISMATCH(HttpStatus.BAD_REQUEST, "매장과 태그 소속이 일치하지 않습니다."),
    INVALID_LINK_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 매장 링크 코드입니다."),
    INVALID_SLUG(HttpStatus.BAD_REQUEST, "유효하지 않은 매장 슬러그입니다."),
    SHOP_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 매장을 등록한 사용자입니다."),
    SHOP_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "매장 소유자가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

