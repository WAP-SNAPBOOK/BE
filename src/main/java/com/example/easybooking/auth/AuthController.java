package com.example.easybooking.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/oauth/login/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@RequestBody KakaoAccessCodeRequest request) {
        AuthResponse response = authService.oAuthLogin(request.getAccessCode());
        if (response.getAccessToken() != null) {
            log.info("카카오 로그인 성공: 엑세스 토큰={}", response.getAccessToken());
            return ResponseEntity.ok(response);
        } else {
            log.error("카카오 로그인 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}