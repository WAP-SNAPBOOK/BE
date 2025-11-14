package com.example.easybooking.auth.util;

import com.example.easybooking.auth.dto.KakaoDto;

public interface OAuthProvider {
    /**
 * Obtain a Kakao user identifier using an OAuth authorization code.
 *
 * @param accessCode the OAuth authorization code received from Kakao
 * @param redirectUri the redirect URI used in the OAuth flow (must match the URI registered with Kakao)
 * @return the `KakaoId` containing the user's Kakao identifier
 */
KakaoDto.KakaoId requestKakaoId(String accessCode, String redirectUri);
}