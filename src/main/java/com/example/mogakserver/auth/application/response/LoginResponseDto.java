package com.example.mogakserver.auth.application.response;

public record LoginResponseDto(
        Long kakaoId,
        String accessToken,
        String refreshToken,
        boolean isNewUser
) {
}

