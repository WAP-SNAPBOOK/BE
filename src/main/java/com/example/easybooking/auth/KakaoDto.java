package com.example.easybooking.auth;

import lombok.Getter;

public class KakaoDto {

    @Getter
    public static class OAuthToken {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;
    }

    @Getter
    public static class KakaoId {
        private Long id;
        private String connected_at;
    }
}
