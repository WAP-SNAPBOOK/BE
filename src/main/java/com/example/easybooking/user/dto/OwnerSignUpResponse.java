package com.example.easybooking.user.dto;

import com.example.easybooking.auth.AuthTokens;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerSignUpResponse {
    private UserType userType;
    private String nickname;
    private AuthTokens tokens;

    public static OwnerSignUpResponse of(UserType userType,User user, AuthTokens tokens) {
        return OwnerSignUpResponse.builder()
                .userType(userType)
                .nickname(user.getNickname())
                .tokens(tokens)
                .build();
    }
}
