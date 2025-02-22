package com.example.mogakserver.external.kakao.service;

import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.enums.SuccessCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.external.kakao.dto.response.KakaoAccessTokenResponse;
import com.example.mogakserver.external.kakao.dto.response.KakaoUserResponse;
import com.example.mogakserver.external.kakao.feign.KakaoApiClient;
import com.example.mogakserver.external.kakao.feign.KakaoAuthApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class KakaoSocialService extends SocialService {
    private static final String Bearer = "Bearer ";
    private static final String GRANT_TYPE = "authorization_code";

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private final KakaoAuthApiClient kakaoAuthApiClient;
    private final KakaoApiClient kakaoApiClient;

    @Override
    public SuccessResponse<Long> getIdFromKakao(String kakaoCode) {
        try {
            KakaoAccessTokenResponse tokenResponse = kakaoAuthApiClient.getOAuth2AccessToken(
                    GRANT_TYPE,
                    clientId,
                    redirectUri,
                    kakaoCode
            );

            if (tokenResponse.accessToken() == null || tokenResponse.accessToken().isEmpty()) {
                throw new NotFoundException(ErrorCode.INVALID_KAKAO_CODE_EXCEPTION);
            }

            KakaoUserResponse userResponse = kakaoApiClient.getUserInformation(Bearer + tokenResponse.accessToken());
            return SuccessResponse.success(SuccessCode.KAKAO_CODE_SUCCESS, userResponse.id());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new NotFoundException(ErrorCode.INVALID_KAKAO_CODE_EXCEPTION);
            }
            throw new NotFoundException(ErrorCode.KAKAO_SERVER_ERROR_EXCEPTION);
        } catch (Exception e) {
            throw new NotFoundException(ErrorCode.KAKAO_SERVER_ERROR_EXCEPTION);
        }
    }
}
