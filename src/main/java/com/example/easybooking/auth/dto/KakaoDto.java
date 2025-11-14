package com.example.easybooking.auth.dto;

import java.time.LocalDateTime;
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

        /**
         * Create a KakaoId populated for tests with the given user id and the current connection timestamp.
         *
         * @param id the Kakao user id to set on the returned DTO
         * @return a KakaoId instance with `id` set to the provided value and `connected_at` set to the current date-time string
         */
        public static KakaoId forTest(Long id) {
            KakaoId kakaoId = new KakaoId();
            kakaoId.id = id;
            kakaoId.connected_at = LocalDateTime.now().toString();
            return kakaoId;
        }
    }
}