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
            @AuthenticationPrincipal String providerId,
            @RequestBody CustomerSignUpRequest request
    ) {
        CustomerSignUpResponse response = userService.signUpCustomer(providerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/owner/signup")
    public ResponseEntity<OwnerSignUpResponse> ownerSignUp(
            @AuthenticationPrincipal String providerId,
            @RequestBody OwnerSignUpRequest request
    ) {
        OwnerSignUpResponse response = userService.signUpOwner(providerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(
            @AuthenticationPrincipal String providerId
    ) {
        userService.deleteUser(providerId);
        return ResponseEntity.ok("회원탈퇴가 완료되었습니다.");
    }
}
