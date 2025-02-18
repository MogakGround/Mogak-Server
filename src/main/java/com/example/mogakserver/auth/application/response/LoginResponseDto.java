package com.example.mogakserver.auth.application.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDto(
        @JsonProperty("userId")
        Long userId,

        String accessToken,
        String refreshToken,

        @JsonProperty("isNewUser")
        boolean isNewUser
) {

    // 신규 사용자 로그인
    public static LoginResponseDto NewUserResponse(Long userId, String accessToken, String refreshToken) {
        return new LoginResponseDto(userId, accessToken, refreshToken, true);
    }

    // 기존 사용자 로그인
    public static LoginResponseDto ExistingUserResponse(Long userId, String accessToken, String refreshToken) {
        return new LoginResponseDto(userId, accessToken, refreshToken, false);
    }

    // 회원가입
    public static LoginResponseDto SignupResponse(Long userId, String accessToken, String refreshToken) {
        return new LoginResponseDto(userId, accessToken, refreshToken, false);
    }
}