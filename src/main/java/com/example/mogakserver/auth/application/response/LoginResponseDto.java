package com.example.mogakserver.auth.application.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDto(
        @JsonProperty("kakaoId")
        Long kakaoId,

        @JsonProperty("status")
        String status,

        String accessToken,
        String refreshToken
) {

    // 신규 사용자 로그인
    public static LoginResponseDto NewUserResponse(Long kakaoId) {
        return new LoginResponseDto(kakaoId, "fail", null, null);
    }

    // 기존 사용자 로그인
    public static LoginResponseDto ExistingUserResponse(String accessToken, String refreshToken) {
        return new LoginResponseDto(null, "success", accessToken, refreshToken);
    }

    // 회원가입
    public static LoginResponseDto SignupResponse(String accessToken, String refreshToken) {
        return new LoginResponseDto(null, null, accessToken, refreshToken);
    }
}