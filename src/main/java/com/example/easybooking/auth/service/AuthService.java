package com.example.easybooking.auth.service;

import java.util.Optional;

import com.example.easybooking.auth.dto.AuthResponse;
import com.example.easybooking.auth.JwtUtil;
import com.example.easybooking.auth.dto.KakaoDto;
import com.example.easybooking.auth.KakaoUtil;
import com.example.easybooking.user.domain.UserType;
import org.springframework.stereotype.Service;

import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.UserWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final KakaoUtil kakaoUtil;
    private final UserReader userReader;
    private final JwtUtil jwtUtil;

    public AuthResponse oAuthLogin(String accessCode) {
        try{
            KakaoDto.KakaoId kakaoId = kakaoUtil.requestKakaoId(accessCode);
            Optional<User> existingUser = userReader.getUserByProviderId(String.valueOf(kakaoId.getId()));

            if(existingUser.isPresent()){
                User user = existingUser.get();
                String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
                String refreshToken = jwtUtil.generateRefreshToken(user.getId());
                return AuthResponse.loginSuccess(accessToken, refreshToken, user.getRole().name(), user.getUserType());
            }
            else{
                String tempToken = jwtUtil.generateTempToken(String.valueOf(kakaoId.getId()));
                return AuthResponse.signupRequired(tempToken);
            }
        }
        catch (Exception e){
            log.error("OAuth login failed", e);
            return AuthResponse.failure("OAuth 로그인 실패: " + e.getMessage());
        }
    }
}

