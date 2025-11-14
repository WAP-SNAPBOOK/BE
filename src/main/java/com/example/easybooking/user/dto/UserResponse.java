package com.example.easybooking.user.dto;

import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String name;
    private String phoneNumber;
    private UserType userType;
    private User.Role role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType())
                .role(user.getRole())
                .build();
    }
}