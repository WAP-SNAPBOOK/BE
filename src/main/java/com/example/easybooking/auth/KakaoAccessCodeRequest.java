package com.example.easybooking.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class KakaoAccessCodeRequest {
    String accessCode;
}
