package com.example.easybooking.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/oauth/login/kakao")
    public void kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse httpServletResponse) {
        authService.oAuthLogin(accessCode, httpServletResponse);
    }

}