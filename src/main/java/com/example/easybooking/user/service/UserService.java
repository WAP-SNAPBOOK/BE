package com.example.easybooking.user.service;


import com.example.easybooking.auth.AuthTokens;
import com.example.easybooking.auth.JwtUtil;
import com.example.easybooking.user.UserWriter;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.repository.UserRepository;
import com.example.easybooking.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserWriter userWriter;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public CustomerSignUpResponse signUpCustomer(String providerId, CustomerSignUpRequest request) {
        User savedUser = userWriter.registerCustomer(request, providerId);
        AuthTokens tokens = jwtUtil.generateTokens(providerId, savedUser.getRole().name());

        return CustomerSignUpResponse.builder()
                .nickname(savedUser.getNickname())
                .tokens(tokens)
                .build();
    }

    public OwnerSignUpResponse signUpOwner(String providerId, OwnerSignUpRequest request) {
        User savedUser = userWriter.registerOwner(request, providerId);
        AuthTokens tokens = jwtUtil.generateTokens(providerId, savedUser.getRole().name());

        return OwnerSignUpResponse.builder()
                .businessNumber(request.getBusinessNumber())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .businessName(request.getBusinessName())
                .nickname(savedUser.getNickname())
                .tokens(tokens)
                .build();
    }

    @Transactional(readOnly = true)
    public Long getUserIDByProviderId(String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return user.getId();
    }
}
