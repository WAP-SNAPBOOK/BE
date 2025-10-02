package com.example.easybooking.user.dto;

import com.example.easybooking.auth.AuthTokens;
import com.example.easybooking.user.domain.OwnerProfile;
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
    private String nickname;        // 닉네임
    private String businessName;    // 상호명
    private String businessNumber;  // 사업자 번호
    private String address;         // 사업장 주소
    private String phoneNumber;     // 연락처
    private AuthTokens tokens;

    public static OwnerSignUpResponse of(UserType userType,User user, AuthTokens tokens, OwnerProfile profile) {
        return OwnerSignUpResponse.builder()
                .userType(userType)
                .nickname(user.getNickname())
                .businessName(profile.getBusinessName())
                .businessNumber(profile.getBusinessNumber())
                .address(profile.getAddress())
                .phoneNumber(profile.getPhoneNumber())
                .tokens(tokens)
                .build();
    }
}
