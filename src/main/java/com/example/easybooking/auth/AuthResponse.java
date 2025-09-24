package com.example.easybooking.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
    private String message;
    private AuthStatus authStatus;

    public static AuthResponse loginSuccess(String accessToken, String refreshToken, String role) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .authStatus(AuthStatus.LOGIN_SUCCESS)
                .message("로그인 성공")
                .build();
    }
    public static AuthResponse signupRequired(String tempToken) {
        return AuthResponse.builder()
                .accessToken(tempToken)
                .authStatus(AuthStatus.SIGNUP_REQUIRED)
                .message("회원가입 필요")
                .build();
    }
    public static AuthResponse failure(String message) {
            return AuthResponse.builder()
                    .message(message)
                    .build();
    }

    public enum AuthStatus {
        LOGIN_SUCCESS,      // 기존 회원 로그인
        SIGNUP_REQUIRED     // 회원가입 필요
    }
}

