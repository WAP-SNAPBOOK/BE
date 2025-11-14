package com.example.easybooking.auth.util;

import com.example.easybooking.auth.dto.KakaoDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("loadtest")
public class MockOAuthProvider implements OAuthProvider {

    /**
     * Generates a test KakaoId from the provided access code and returns it wrapped as a KakaoDto.KakaoId.
     *
     * @param accessCode the access code from which the test Kakao ID is derived; if it starts with "loadtest_code_" the numeric suffix is used, otherwise a deterministic hash-based id is produced
     * @param redirectUri accepted for interface compatibility; not used by this mock implementation
     * @return the generated test KakaoId wrapped in a KakaoDto.KakaoId
     */
    @Override
    public KakaoDto.KakaoId requestKakaoId(String accessCode, String redirectUri) {
        // 인가코드를 기반으로 가짜 kakaoId 생성
        Long kakaoId = parseKakaoIdFromAccessCode(accessCode);

        KakaoDto.KakaoId result = KakaoDto.KakaoId.forTest(kakaoId);

        return result;
    }

    /**
     * Derives a Kakao numeric id from the provided access code.
     *
     * If the code starts with "loadtest_code_", parses and returns the numeric portion that follows the prefix.
     * Otherwise returns the absolute value of the code's hashCode modulo 1,000,000.
     *
     * @param accessCode the access code to derive the id from
     * @return the derived Kakao id
     * @throws NumberFormatException if the substring after "loadtest_code_" is not a valid number
     */
    private Long parseKakaoIdFromAccessCode(String accessCode) {
        // "loadtest_code_123" 형식이면 123 반환
        if (accessCode.startsWith("loadtest_code_")) {
            return Long.parseLong(accessCode.substring("loadtest_code_".length()));
        }
        return Math.abs(accessCode.hashCode()) % 1000000L;
    }
}