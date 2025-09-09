package com.example.easybooking.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final KakaoUtil kakaoUtil;

    public void oAuthLogin(String accessCode, HttpServletResponse httpServletResponse) {
        KakaoDto.OAuthToken oAuthToken = kakaoUtil.requestToken(accessCode);
        log.info("oAuthToken : " + oAuthToken.getAccess_token());
    }
}
