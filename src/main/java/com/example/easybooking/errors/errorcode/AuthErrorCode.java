package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {
    // OAuth 관련 오류
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 토큰 요청에 실패했습니다."),
    KAKAO_PROFILE_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 요청에 실패했습니다."),
    KAKAO_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 응답 파싱에 실패했습니다."),
    KAKAO_OAUTH_FAILED(HttpStatus.UNAUTHORIZED, "카카오 OAuth 인증에 실패했습니다."),
    INVALID_DEV_AUTH_PERSONA(HttpStatus.BAD_REQUEST, "유효하지 않은 dev auth persona 입니다."),

    // JWT 관련 오류
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰 형식입니다."),
    TOKEN_PARSING_FAILED(HttpStatus.UNAUTHORIZED, "토큰 파싱에 실패했습니다."),
    TOKEN_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 생성에 실패했습니다."),

    // 인증 애너테이션 관련 오류
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    SIGNUP_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "회원가입 토큰이 필요합니다. 이미 가입된 사용자이거나 잘못된 토큰입니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FULL_LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "정식 로그인이 필요합니다. 회원가입을 먼저 완료해주세요."),

    INTERNAL_SEVERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UNAUTHENTICATED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    INVALID_AUTH_PRINCIPAL(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 정보입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
