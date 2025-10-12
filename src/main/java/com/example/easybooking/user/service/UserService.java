package com.example.easybooking.user.service;


import com.example.easybooking.auth.AuthTokens;
import com.example.easybooking.auth.JwtUtil;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.UserWriter;
import com.example.easybooking.user.domain.OwnerProfile;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserWriter userWriter;
    private final UserReader userReader;
    private final JwtUtil jwtUtil;

    public CustomerSignUpResponse signUpCustomer(String providerId, CustomerSignUpRequest request) {
        User savedUser = userWriter.registerCustomer(request, providerId);
        AuthTokens tokens = jwtUtil.generateTokens(providerId, savedUser.getRole().name());

        return CustomerSignUpResponse.of(UserType.CUSTOMER,savedUser, tokens);
    }

    public OwnerSignUpResponse signUpOwner(String providerId, OwnerSignUpRequest request) {
        User savedUser = userWriter.registerOwner(request, providerId);
        OwnerProfile profile = userReader.getOwnerProfile(providerId);
        AuthTokens tokens = jwtUtil.generateTokens(providerId, savedUser.getRole().name());
        return OwnerSignUpResponse.of(UserType.OWNER,savedUser,tokens,profile);
    }
    @Transactional
    public void deleteUser(String providerId) {
        userWriter.deleteUser(providerId);
    }
}
