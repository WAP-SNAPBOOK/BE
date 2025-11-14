package com.example.easybooking.auth.presentation;

import com.example.easybooking.auth.dto.AuthResponse;
import com.example.easybooking.auth.dto.KakaoAccessCodeRequest;
import com.example.easybooking.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
}