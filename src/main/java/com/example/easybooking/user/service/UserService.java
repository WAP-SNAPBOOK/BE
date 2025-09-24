package com.example.easybooking.user.service;


import com.example.easybooking.user.UserWriter;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserWriter userWriter;

    public CustomerSignUpResponse signUpCustomer(String providerId, CustomerSignUpRequest request) {
        User savedUser = userWriter.registerCustomer(request, providerId);
        CustomerSignUpResponse response = CustomerSignUpResponse.builder()
                .nickname(savedUser.getNickname())
                .build();
        return response;
    }

    public OwnerSignUpResponse signUpOwner(String providerId, OwnerSignUpRequest request) {
        User savedUser = userWriter.registerOwner(request, providerId);
        OwnerSignUpResponse response = OwnerSignUpResponse.builder()
                .businessNumber(request.getBusinessNumber())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .businessName(request.getBusinessName())
                .nickname(savedUser.getNickname())
                .build();
        return response;
    }
}
