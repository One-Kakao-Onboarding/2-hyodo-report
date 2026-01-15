package com.example.spring.oauth.service;

import com.example.spring.oauth.config.KakaoOAuthProperties;
import com.example.spring.oauth.dto.KakaoTokenResponse;
import com.example.spring.oauth.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final WebClient webClient;

    /**
     * 카카오 인증 코드로 액세스 토큰 요청
     */
    public KakaoTokenResponse getAccessToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoOAuthProperties.getClientId());
        params.add("client_secret", kakaoOAuthProperties.getClientSecret());
        params.add("redirect_uri", kakaoOAuthProperties.getRedirectUri());
        params.add("code", authorizationCode);

        try {
            KakaoTokenResponse response = webClient.post()
                    .uri(kakaoOAuthProperties.getTokenUri())
                    .headers(headers -> {
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();

            log.info("Successfully obtained Kakao access token");
            return response;
        } catch (Exception e) {
            log.error("Failed to get Kakao access token: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 액세스 토큰 발급 실패", e);
        }
    }

    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     */
    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserInfo userInfo = webClient.get()
                    .uri(kakaoOAuthProperties.getUserInfoUri())
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .retrieve()
                    .bodyToMono(KakaoUserInfo.class)
                    .block();

            log.info("Successfully obtained Kakao user info for kakaoId: {}", userInfo.id());
            return userInfo;
        } catch (Exception e) {
            log.error("Failed to get Kakao user info: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }

    /**
     * 카카오 Refresh Token으로 Access Token 갱신
     */
    public KakaoTokenResponse refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", kakaoOAuthProperties.getClientId());
        params.add("client_secret", kakaoOAuthProperties.getClientSecret());
        params.add("refresh_token", refreshToken);

        try {
            KakaoTokenResponse response = webClient.post()
                    .uri(kakaoOAuthProperties.getTokenUri())
                    .headers(headers -> {
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    })
                    .bodyValue(params)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();

            log.info("Successfully refreshed Kakao access token");
            return response;
        } catch (Exception e) {
            log.error("Failed to refresh Kakao access token: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 액세스 토큰 갱신 실패", e);
        }
    }

    /**
     * 카카오 로그아웃 (토큰 무효화)
     */
    public void logout(String accessToken) {
        try {
            webClient.post()
                    .uri("https://kapi.kakao.com/v1/user/logout")
                    .headers(headers -> {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                    })
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Successfully logged out from Kakao");
        } catch (Exception e) {
            log.error("Failed to logout from Kakao: {}", e.getMessage(), e);
            throw new RuntimeException("카카오 로그아웃 실패", e);
        }
    }
}
