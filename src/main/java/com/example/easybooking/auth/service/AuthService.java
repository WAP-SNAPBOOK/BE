package com.example.easybooking.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.easybooking.auth.dto.AuthResponse;
import com.example.easybooking.auth.dto.AuthTokens;
import com.example.easybooking.auth.dto.KakaoDto.KakaoId;
import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.auth.util.OAuthProvider;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
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

    /**
     * Authenticate a user via Kakao OAuth and return either a login result or a signup-required response.
     *
     * Obtains the Kakao provider ID, checks for an existing user by provider ID, and:
     * - If a user exists: returns a successful login response containing an access token, refresh token, user id, role, and user type.
     * - If no user exists: returns a signup-required response containing a temporary token derived from the Kakao provider ID.
     *
     * @param accessCode   the authorization code received from Kakao
     * @param redirect_uri the redirect URI used in the OAuth flow
     * @return             an AuthResponse representing a login success with tokens and user details, or a signup-required response with a temporary token
     * @throws AuthException if Kakao OAuth retrieval or token generation fails
     */
    public AuthResponse oAuthLogin(String accessCode, String redirect_uri) {
        KakaoId kakaoId = requestKakaoId(accessCode, redirect_uri);
        Optional<User> existingUser = userReader.getUserByProviderId(String.valueOf(kakaoId.getId()));

        if (existingUser.isPresent()) {
            log.info("사용자 존재, providerId : {}", kakaoId.getId());
            User user = existingUser.get();
            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());
            return AuthResponse.loginSuccess(accessToken, refreshToken, user.getId(), user.getRole().name(),
                    user.getUserType());
        }

        log.info("회원가입 필요, providerId : {}", kakaoId.getId());
        String tempToken = jwtUtil.generateTempToken(String.valueOf(kakaoId.getId()));
        return AuthResponse.signupRequired(tempToken);
    }

    /**
     * Retrieve the Kakao user identifier using the provided OAuth authorization code and redirect URI.
     *
     * @param accessCode  the authorization code returned by Kakao during the OAuth callback
     * @param redirectUri the redirect URI used in the OAuth request
     * @return            the retrieved KakaoId from the OAuth provider
     * @throws AuthException if the provider reports an authentication error or an unexpected error occurs while requesting the Kakao ID
     */
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
