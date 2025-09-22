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

    public static AuthResponse success(String accessToken, String refreshToken, String role, boolean isNewUser) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .message("로그인 성공")
                .isNewUser(isNewUser)
                .build();
    }
    public static AuthResponse failure(String message) {
            return AuthResponse.builder()
                    .message(message)
                    .build();
    }
}

