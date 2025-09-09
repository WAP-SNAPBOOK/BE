package com.example.easybooking.auth;

import com.example.easybooking.user.User;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.UserWriter;
import org.springframework.stereotype.Service;

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
            KakaoDto.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
            log.info("oAuthToken : " + oAuthToken.getAccess_token());
            KakaoDto.KakaoId kakaoId = kakaoUtil.requestKakaoId(oAuthToken);
            log.info("kakaoId : " + kakaoId.getId());
            User user = userReader.getUserByKakaoId(String.valueOf(kakaoId.getId()))
                    .orElseGet(() -> createNewUser(kakaoId.getId()));
            String accessToken = jwtUtil.generateAccessToken(String.valueOf(user.getKakaoId()), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(user.getKakaoId()));
            return AuthResponse.success(
                    accessToken,
                    refreshToken,
                    user.getRole().name()
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

