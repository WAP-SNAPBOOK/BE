package com.example.easybooking.user.presentation;

import com.example.easybooking.user.dto.*;
import com.example.easybooking.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/customer/signup")
    public ResponseEntity<CustomerSignUpResponse> customerSignUp(
            @AuthenticationPrincipal Object principal,
            @RequestBody CustomerSignUpRequest request
    ) {
        // TODO : principal 식별을 컨트롤러에서 분리
        String providerId;
        if (principal instanceof String) {
            providerId = (String) principal;
        } else if (principal instanceof Long) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        } else {
            throw new IllegalArgumentException("인증 정보가 유효하지 않습니다.");
        }
        CustomerSignUpResponse response = userService.signUpCustomer(providerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/owner/signup")
    public ResponseEntity<OwnerSignUpResponse> ownerSignUp(
            @AuthenticationPrincipal Object principal,
            @RequestBody OwnerSignUpRequest request
    ) {
        // TODO : principal 식별을 컨트롤러에서 분리
        String providerId;
        if (principal instanceof String) {
            providerId = (String) principal;
        } else if (principal instanceof Long) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        } else {
            throw new IllegalArgumentException("인증 정보가 유효하지 않습니다.");
        }
        OwnerSignUpResponse response = userService.signUpOwner(providerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(
            @AuthenticationPrincipal Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("회원탈퇴가 완료되었습니다.");
    }
}
