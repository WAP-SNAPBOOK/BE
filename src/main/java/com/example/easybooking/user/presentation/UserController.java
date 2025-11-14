package com.example.easybooking.user.presentation;

import com.example.easybooking.auth.annotation.RequireTempUser;
import com.example.easybooking.auth.domain.TempUser;
import com.example.easybooking.user.dto.CustomerSignUpRequest;
import com.example.easybooking.user.dto.CustomerSignUpResponse;
import com.example.easybooking.user.dto.OwnerSignUpRequest;
import com.example.easybooking.user.dto.OwnerSignUpResponse;
import com.example.easybooking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

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

//    @DeleteMapping
//    public ResponseEntity<String> deleteUser(
//            @RequireAuthenticatedUser AuthenticatedUser user
//    ) {
//        userService.deleteUser(user.getUserId());
//        return ResponseEntity.ok("회원탈퇴가 완료되었습니다.");
//    }
}
