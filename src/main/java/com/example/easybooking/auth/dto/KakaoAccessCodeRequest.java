package com.example.easybooking.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class KakaoAccessCodeRequest {
    String accessCode;
}
