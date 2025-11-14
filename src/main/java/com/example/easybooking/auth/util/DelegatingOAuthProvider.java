// DelegatingOAuthProvider.java (새 파일)

package com.example.easybooking.auth.util;

import com.example.easybooking.auth.dto.KakaoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary   // OAuthProvider 주입 시 이 구현체가 기본으로 선택되도록
@RequiredArgsConstructor
public class DelegatingOAuthProvider implements OAuthProvider {

    private final KakaoOAuthProvider kakaoOAuthProvider;
    private final MockOAuthProvider mockOAuthProvider;

    @Override
    public KakaoDto.KakaoId requestKakaoId(String accessCode, String redirectUri) {

        if (isLoadTestCode(accessCode)) {
            // 부하 테스트용 → Mock 사용
            return mockOAuthProvider.requestKakaoId(accessCode, redirectUri);
        }

        // 그 외 → 실제 카카오 호출
        return kakaoOAuthProvider.requestKakaoId(accessCode, redirectUri);
    }

    private boolean isLoadTestCode(String accessCode) {
        return accessCode != null &&
                (accessCode.startsWith("loadtest_code_"));
    }
}