package com.example.easybooking.auth;

import java.util.Optional;

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
    private final UserWriter userWriter;
    private final JwtUtil jwtUtil;

    public AuthResponse oAuthLogin(String accessCode) {
        try{
            KakaoDto.KakaoId kakaoId = kakaoUtil.requestKakaoId(accessCode);
            Optional<User> existingUser = userReader.getUserByKakaoId(String.valueOf(kakaoId.getId()));

            if(existingUser.isPresent()){
                User user = existingUser.get();
                String accessToken = jwtUtil.generateAccessToken(String.valueOf(user.getProviderId()), user.getRole().name());
                String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(user.getProviderId()));

                return AuthResponse.loginSuccess(accessToken, refreshToken, user.getRole().name());
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

