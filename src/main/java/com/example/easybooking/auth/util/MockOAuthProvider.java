package com.example.easybooking.auth.util;

import com.example.easybooking.auth.dto.KakaoDto;
import org.springframework.stereotype.Component;

@Component
public class MockOAuthProvider implements OAuthProvider {

    @Override
    public KakaoDto.KakaoId requestKakaoId(String accessCode, String redirectUri) {
        // 인가코드를 기반으로 가짜 kakaoId 생성
        Long kakaoId = parseKakaoIdFromAccessCode(accessCode);

        KakaoDto.KakaoId result = KakaoDto.KakaoId.forTest(kakaoId);

        return result;
    }

    private Long parseKakaoIdFromAccessCode(String accessCode) {
        // "loadtest_code_123" 형식이면 123 반환
        if (accessCode.startsWith("loadtest_code_")) {
            return Long.parseLong(accessCode.substring("loadtest_code_".length()));
        }
        return Math.abs(accessCode.hashCode()) % 1000000L;
    }
}