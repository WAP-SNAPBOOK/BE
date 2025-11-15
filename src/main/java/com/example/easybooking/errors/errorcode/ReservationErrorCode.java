package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ReservationErrorCode implements ErrorCode {

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    INVALID_RESERVATION_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 예약 ID입니다."),
    INVALID_FORM_JSON(HttpStatus.BAD_REQUEST, "폼 데이터 처리 중 JSON 변환 오류가 발생했습니다."),
    REQUIRED_DATE_MISSING(HttpStatus.BAD_REQUEST, "예약 날짜(date)는 필수 항목입니다."),
    REQUIRED_TIME_MISSING(HttpStatus.BAD_REQUEST, "예약 시간(time)은 필수 항목입니다."),
    INVALID_NUMBER_FORMAT(HttpStatus.BAD_REQUEST, "숫자 필드 형식이 올바르지 않습니다."),
    INVALID_PHOTO_JSON(HttpStatus.BAD_REQUEST, "첨부 사진(photo) 데이터 형식이 올바르지 않습니다. JSON 배열 형식이어야 합니다."),
    TIME_SLOT_ALREADY_BOOKED(HttpStatus.CONFLICT, "선택하신 시간은 이미 예약되었거나 접수 대기 중입니다.");
    private final HttpStatus httpStatus;
    private final String message;
}


