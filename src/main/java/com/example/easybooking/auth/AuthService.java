package com.example.easybooking.auth;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.easybooking.user.User;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.UserWriter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final KakaoUtil kakaoUtil;
    private final UserReader userReader;
    private final UserWriter userWriter;
    private final JwtUtil jwtUtil;

    public AuthResponse oAuthLogin(String accessCode, HttpServletResponse httpServletResponse) {
        try{
            KakaoDto.KakaoId kakaoId = kakaoUtil.requestKakaoId(accessCode);
            Optional<User> existingUser = userReader.getUserByKakaoId(String.valueOf(kakaoId.getId()));
            boolean isNewUser = existingUser.isEmpty();
            User user = existingUser.orElseGet(() -> createNewUser(kakaoId.getId()));

            String accessToken = jwtUtil.generateAccessToken(String.valueOf(user.getKakaoId()), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(user.getKakaoId()));

            return AuthResponse.success(
                    accessToken,
                    refreshToken,
                    user.getRole().name(),
                    isNewUser
            );
        }
        catch (Exception e){
            log.error("OAuth login failed", e);
            return AuthResponse.failure("OAuth 로그인 실패: " + e.getMessage());
        }

    }

    private User createNewUser(Long kakaoId) {
        User newUser = User.createNewUser(String.valueOf(kakaoId));
        return userWriter.save(newUser);
    }
}

