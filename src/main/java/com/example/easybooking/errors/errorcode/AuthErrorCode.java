package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 토큰 요청에 실패했습니다."),
    KAKAO_PROFILE_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 요청에 실패했습니다."),
    KAKAO_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 응답 파싱에 실패했습니다."),
    KAKAO_OAUTH_FAILED(HttpStatus.UNAUTHORIZED, "카카오 OAuth 인증에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
