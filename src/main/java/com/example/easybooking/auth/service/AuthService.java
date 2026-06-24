package com.example.easybooking.auth.service;

import com.example.easybooking.auth.dto.AuthResponse;
import com.example.easybooking.auth.dto.AuthTokens;
import com.example.easybooking.auth.dto.KakaoDto.KakaoId;
import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.auth.util.OAuthProvider;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final OAuthProvider oAuthProvider;
    private final UserReader userReader;
    private final JwtUtil jwtUtil;

    public AuthResponse oAuthLogin(String accessCode, String redirect_uri) {
        KakaoId kakaoId = requestKakaoId(accessCode, redirect_uri);
        Optional<User> existingUser = userReader.getUserByProviderId(
            String.valueOf(kakaoId.getId()));

        if (existingUser.isPresent()) {
            log.info("사용자 존재, providerId : {}", kakaoId.getId());
            User user = existingUser.get();
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());
            return AuthResponse.loginSuccess(accessToken, refreshToken, user.getId(),
                user.getRole().name(),
                user.getUserType());
        }
        log.info("회원가입 필요, providerId : {}", kakaoId.getId());
        String tempToken = jwtUtil.generateTempToken(String.valueOf(kakaoId.getId()));
        return AuthResponse.signupRequired(tempToken);
    }

    private KakaoId requestKakaoId(String accessCode, String redirectUri) {
        try {
            return oAuthProvider.requestKakaoId(accessCode, redirectUri);
        } catch (AuthException e) {
            log.error("OAuth 로그인 실패: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OAuth 로그인 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new AuthException(AuthErrorCode.KAKAO_OAUTH_FAILED, e.getMessage());
        }
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        jwtUtil.validateToken(refreshToken);

        if (!"refresh".equals(jwtUtil.getTokenType(refreshToken))) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        User user = userReader.read(userId);

        AuthTokens tokens = jwtUtil.generateTokens(user.getId(), user.getRole().name());

        return AuthResponse.loginSuccess(
            tokens.accessToken(),
            tokens.refreshToken(),
            user.getId(),
            user.getRole().name(),
            user.getUserType()
        );
    }

    public void validateToken(String token) {
        jwtUtil.validateToken(token);
    }
}

