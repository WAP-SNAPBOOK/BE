package com.example.easybooking.auth.util;

import com.example.easybooking.auth.dto.KakaoDto;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("!loadtest")
@Slf4j
public class KakaoOAuthProvider implements OAuthProvider {
    @Value("${spring.kakao.auth.client}")
    private String client;

    public KakaoDto.OAuthToken requestToken(String accessCode, String redirect_uri) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", client);
        params.add("redirect_uri", redirect_uri);
        params.add("code", accessCode);

        log.info("redirect uri: {}", redirect_uri);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            KakaoDto.OAuthToken oAuthToken = objectMapper.readValue(response.getBody(), KakaoDto.OAuthToken.class);
            log.info("oAuthToken : " + oAuthToken.getAccess_token());
            return oAuthToken;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("카카오 토큰 요청 실패 - HTTP Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.error("카카오 토큰 요청 중 네트워크 오류 발생", e);
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
        } catch (JsonProcessingException e) {
            log.error("카카오 토큰 응답 파싱 실패", e);
            throw new AuthException(AuthErrorCode.KAKAO_RESPONSE_PARSE_FAILED);
        }
    }

    public KakaoDto.KakaoId requestKakaoId(String accessCode, String redirect_url) {

        try {
            KakaoDto.OAuthToken oAuthToken = requestToken(accessCode, redirect_url);

            RestTemplate restTemplate2 = new RestTemplate();
            HttpHeaders headers2 = new HttpHeaders();
            headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            headers2.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

            HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers2);

            ResponseEntity<String> response2 = restTemplate2.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    kakaoProfileRequest,
                    String.class);

            log.info("kakaoProfile : " + response2);

            ObjectMapper objectMapper = new ObjectMapper();
            KakaoDto.KakaoId kakaoId = objectMapper.readValue(response2.getBody(), KakaoDto.KakaoId.class);
            return kakaoId;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("카카오 사용자 정보 요청 실패 - HTTP Status: {}, Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthException(AuthErrorCode.KAKAO_PROFILE_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.error("카카오 사용자 정보 요청 중 네트워크 오류 발생", e);
            throw new AuthException(AuthErrorCode.KAKAO_PROFILE_REQUEST_FAILED);
        } catch (JsonProcessingException e) {
            log.error("카카오 사용자 정보 응답 파싱 실패", e);
            throw new AuthException(AuthErrorCode.KAKAO_RESPONSE_PARSE_FAILED);
        } catch (AuthException e) {
            throw e;
        }
    }
}
