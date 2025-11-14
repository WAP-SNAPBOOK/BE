package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ChatErrorCode implements ErrorCode {

    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메시지입니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다."),
    CHAT_PARTICIPANT_REQUIRED(HttpStatus.FORBIDDEN, "해당 채팅방에 참여 권한이 없습니다."),
    MESSAGE_CONTENT_INVALID(HttpStatus.BAD_REQUEST, "메시지 내용이 올바르지 않습니다."),
    UNAUTHENTICATED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    INVALID_AUTH_PRINCIPAL(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 정보입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}


