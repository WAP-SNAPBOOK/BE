package com.example.easybooking.user.service;


import com.example.easybooking.auth.dto.AuthTokens;
import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.user.UserWriter;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.dto.CustomerSignUpRequest;
import com.example.easybooking.user.dto.CustomerSignUpResponse;
import com.example.easybooking.user.dto.OwnerSignUpRequest;
import com.example.easybooking.user.dto.OwnerSignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserWriter userWriter;
    private final JwtUtil jwtUtil;

    public CustomerSignUpResponse signUpCustomer(String providerId, CustomerSignUpRequest request) {
        User savedUser = userWriter.registerCustomer(request, providerId);
        AuthTokens tokens = jwtUtil.generateTokens(savedUser.getId(), savedUser.getRole().name());

        return CustomerSignUpResponse.of(UserType.CUSTOMER, savedUser, tokens);
    }

    public OwnerSignUpResponse signUpOwner(String providerId, OwnerSignUpRequest request) {
        User savedUser = userWriter.registerOwner(request, providerId);
        AuthTokens tokens = jwtUtil.generateTokens(savedUser.getId(), savedUser.getRole().name());
        return OwnerSignUpResponse.of(UserType.OWNER, savedUser, tokens);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userWriter.deleteUser(userId);
    }

}
