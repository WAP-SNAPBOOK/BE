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
    private boolean isNewUser;

    public static AuthResponse loginSuccess(String accessToken, String refreshToken, String role) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .message("로그인 성공")
                .build();
    }
    public static AuthResponse signupRequired(String tempToken) {
        return AuthResponse.builder()
                .accessToken(tempToken)
                .message("회원가입 필요")
                .isNewUser(true)
                .build();
    }
    public static AuthResponse failure(String message) {
            return AuthResponse.builder()
                    .message(message)
                    .build();
    }
}

