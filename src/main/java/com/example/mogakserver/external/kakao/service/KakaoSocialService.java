package com.example.mogakserver.external.kakao.service;

import com.example.mogakserver.external.kakao.dto.response.KakaoAccessTokenResponse;
import com.example.mogakserver.external.kakao.dto.response.KakaoUserResponse;
import com.example.mogakserver.external.kakao.feign.KakaoApiClient;
import com.example.mogakserver.external.kakao.feign.KakaoAuthApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoSocialService extends SocialService {
    @Value("${kakao.client-id}")
    private String clientId;
    private static final String Bearer = "Bearer ";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String KAKAO_ROUTER = "/login/oauth2/code/kakao";
    private final KakaoAuthApiClient kakaoAuthApiClient;
    private final KakaoApiClient kakaoApiClient;

    @Override
    public Long getIdFromKakao(String baseUrl, String kakaoCode) {
        String redirectUrl = baseUrl + KAKAO_ROUTER;
        KakaoAccessTokenResponse tokenResponse = kakaoAuthApiClient.getOAuth2AccessToken(
                GRANT_TYPE,
                clientId,
                redirectUrl,
                kakaoCode
        );

        KakaoUserResponse userResponse = kakaoApiClient.getUserInformation(Bearer + tokenResponse.accessToken());
        return userResponse.id();
    }
}
