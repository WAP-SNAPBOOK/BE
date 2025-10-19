package com.example.easybooking.user.presentation;

import com.example.easybooking.auth.AuthenticatedUser;
import com.example.easybooking.auth.RequireAuthenticatedUser;
import com.example.easybooking.auth.RequireTempUser;
import com.example.easybooking.auth.TempUser;
import com.example.easybooking.user.dto.*;
import com.example.easybooking.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * 고객 회원가입
     * 임시 토큰(TempUser)이 필요합니다.
     */
    @PostMapping("/customer/signup")
    public ResponseEntity<CustomerSignUpResponse> customerSignUp(
            @RequireTempUser TempUser tempUser,
            @RequestBody CustomerSignUpRequest request
    ) {
        CustomerSignUpResponse response = userService.signUpCustomer(
            tempUser.getProviderId(), 
            request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 사업자 회원가입
     * 임시 토큰(TempUser)이 필요합니다.
     */
    @PostMapping("/owner/signup")
    public ResponseEntity<OwnerSignUpResponse> ownerSignUp(
            @RequireTempUser TempUser tempUser,
            @RequestBody OwnerSignUpRequest request
    ) {
        OwnerSignUpResponse response = userService.signUpOwner(
            tempUser.getProviderId(), 
            request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴
     * 정식 인증 토큰(AuthenticatedUser)이 필요합니다.
     */
    @DeleteMapping
    public ResponseEntity<String> deleteUser(
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        userService.deleteUser(user.getUserId());
        return ResponseEntity.ok("회원탈퇴가 완료되었습니다.");
    }
}
