package com.example.easybooking.auth.presentation;

import com.example.easybooking.auth.dto.AuthResponse;
import com.example.easybooking.auth.dto.KakaoAccessCodeRequest;
import com.example.easybooking.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @Value("${spring.kakao.auth.redirect}")
    private String redirect;

    @Value("${spring.kakao.auth.redirect-local}")
    private String redirectLocal;

    /**
     * Performs Kakao OAuth login using the provided access code and returns the authentication result.
     *
     * @param request the request body containing the Kakao access code
     * @return a ResponseEntity wrapping the AuthResponse: HTTP 200 with the authentication data when login succeeds, HTTP 400 with the response when login fails
     */
    @PostMapping("/oauth/login/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@RequestBody KakaoAccessCodeRequest request) {
        AuthResponse response = authService.oAuthLogin(request.getAccessCode(), redirect);
        if (response.getAccessToken() != null) {
            log.info("카카오 로그인 성공: 엑세스 토큰={}", response.getAccessToken());
            return ResponseEntity.ok(response);
        } else {
            log.error("카카오 로그인 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Handle Kakao OAuth login using the local redirect URL with the provided access code.
     *
     * @param request the request containing the Kakao access code
     * @return a ResponseEntity containing the AuthResponse: HTTP 200 with the AuthResponse when login succeeds (access token present), HTTP 400 with the AuthResponse when login fails
     */
    @PostMapping("/oauth/login/kakao/local")
    public ResponseEntity<AuthResponse> kakaoLoginForLocal(@RequestBody KakaoAccessCodeRequest request) {
        AuthResponse response = authService.oAuthLogin(request.getAccessCode(), redirectLocal);
        if (response.getAccessToken() != null) {
            log.info("카카오 로그인 성공: 엑세스 토큰={}", response.getAccessToken());
            return ResponseEntity.ok(response);
        } else {
            log.error("카카오 로그인 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Performs a Kakao OAuth login using the configured redirect URL specifically for load testing and returns the resulting authentication data.
     *
     * @param request the request carrying the Kakao access code
     * @return the authentication response produced by the OAuth login, wrapped in an HTTP 200 ResponseEntity
     */
    @PostMapping("/oauth/login/kakao/loadtest")
    @Profile("loadtest")
    public ResponseEntity<AuthResponse> kakaoLoginForLoadTest(
            @RequestBody KakaoAccessCodeRequest request) {
        AuthResponse response = authService.oAuthLogin(request.getAccessCode(), redirect);
        return ResponseEntity.ok(response);
    }

}