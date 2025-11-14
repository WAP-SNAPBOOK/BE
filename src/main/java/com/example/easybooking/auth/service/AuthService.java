package com.example.easybooking.auth.service;

import com.example.easybooking.auth.dto.AuthResponse;
import com.example.easybooking.auth.dto.KakaoDto.KakaoId;
import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.auth.util.OAuthProvider;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final OAuthProvider oAuthProvider;
    private final UserReader userReader;
    private final JwtUtil jwtUtil;

    /**
     * Handles OAuth login using Kakao credentials and returns an AuthResponse describing the outcome.
     *
     * Attempts to obtain the Kakao provider ID from the given authorization code and redirect URI. If a user
     * with that provider ID exists, issues access and refresh JWTs and returns a login success response;
     * if no user exists, issues a temporary token and returns a signup-required response; on error returns a failure response.
     *
     * @param accessCode the authorization code received from Kakao
     * @param redirect_uri the redirect URI used in the OAuth flow (must match the one registered with the provider)
     * @return an AuthResponse representing login success (with access and refresh tokens, user id, role, and type), signup required (with temporary token), or failure (with an error message)
     */
    public AuthResponse oAuthLogin(String accessCode, String redirect_uri) {
        try {
            KakaoId kakaoId = oAuthProvider.requestKakaoId(accessCode, redirect_uri);
            Optional<User> existingUser = userReader.getUserByProviderId(String.valueOf(kakaoId.getId()));

            if (existingUser.isPresent()) {
                log.info("사용자 존재, providerId : {}", kakaoId.getId());
                User user = existingUser.get();
                String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
                String refreshToken = jwtUtil.generateRefreshToken(user.getId());
                return AuthResponse.loginSuccess(accessToken, refreshToken, user.getId(), user.getRole().name(),
                        user.getUserType());
            } else {
                log.info("회원가입 필요, providerId : {}", kakaoId.getId());
                String tempToken = jwtUtil.generateTempToken(String.valueOf(kakaoId.getId()));
                return AuthResponse.signupRequired(tempToken);
            }
        } catch (Exception e) {
            log.error("OAuth login failed", e);
            return AuthResponse.failure("OAuth 로그인 실패: " + e.getMessage());
        }
    }
}
