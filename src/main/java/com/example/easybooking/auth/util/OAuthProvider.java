package com.example.easybooking.auth.util;

import com.example.easybooking.auth.dto.KakaoDto;

public interface OAuthProvider {
    KakaoDto.KakaoId requestKakaoId(String accessCode, String redirectUri);
}
