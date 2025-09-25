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
public class CustomerSignUpResponse {
    private String nickname;
    private AuthTokens tokens;

}
