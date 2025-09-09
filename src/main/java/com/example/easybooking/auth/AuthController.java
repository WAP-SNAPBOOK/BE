package com.example.easybooking.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @GetMapping("/oauth/login/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse httpServletResponse) {
        AuthResponse response = authService.oAuthLogin(accessCode, httpServletResponse);
        if (response.getAccessToken() != null) {
            log.info("카카오 로그인 성공: 엑세스 토큰={}", response.getAccessToken());
            return ResponseEntity.ok(response);
        } else {
            log.error("카카오 로그인 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}