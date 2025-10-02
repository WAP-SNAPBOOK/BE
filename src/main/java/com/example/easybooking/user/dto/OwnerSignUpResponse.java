package com.example.easybooking.user.dto;

import com.example.easybooking.auth.AuthTokens;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerSignUpResponse {
    private String nickname;        // 닉네임
    private String businessName;    // 상호명
    private String businessNumber;  // 사업자 번호
    private String address;         // 사업장 주소
    private String phoneNumber;     // 연락처
    private AuthTokens tokens;
}
