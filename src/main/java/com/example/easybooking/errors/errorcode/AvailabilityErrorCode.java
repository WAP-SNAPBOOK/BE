package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum AvailabilityErrorCode implements ErrorCode {

    SHOP_SETTINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "매장 예약 설정을 찾을 수 없습니다."),
    INVALID_INTERVAL_MINUTES(HttpStatus.BAD_REQUEST, "예약 간격은 30분 또는 60분만 허용됩니다."),
    BOOKING_WINDOW_EXCEEDED(HttpStatus.BAD_REQUEST, "예약 가능 기간을 초과했습니다."),
    STAFF_OVERRIDE_OUT_OF_SHOP_RANGE(HttpStatus.BAD_REQUEST, "직원 운영 시간이 매장 운영 범위를 벗어났습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
