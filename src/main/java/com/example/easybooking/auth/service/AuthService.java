package com.example.easybooking.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.easybooking.auth.dto.AuthResponse;
import com.example.easybooking.auth.dto.KakaoDto.KakaoId;
import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.auth.util.OAuthProvider;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final OAuthProvider oAuthProvider;
    private final UserReader userReader;
    private final JwtUtil jwtUtil;

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
        } catch (AuthException e) {
            log.error("OAuth 로그인 실패: {}", e.getAuthErrorCode().getMessage());
            return AuthResponse.failure("OAuth 로그인 실패: " + e.getAuthErrorCode().getMessage());
        } catch (Exception e) {
            log.error("OAuth 로그인 중 예상치 못한 오류 발생: {}", e.getMessage());
            return AuthResponse.failure("OAuth 로그인 실패: " + e.getMessage());
        }
    }
}

