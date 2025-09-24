package com.example.easybooking.user;

import com.example.easybooking.user.dto.SignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup/complete")
    public ResponseEntity<SignUpResponse> signUp(
            @AuthenticationPrincipal String providerId,
            @RequestBody SignUpRequest request
    ) {
        SignUpResponse response = userService.signUp(providerId, request);
        return ResponseEntity.ok(response);
    }
}
