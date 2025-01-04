package com.example.mogakserver.common.exception.dto;

public record TokenPair(
        String accessToken, String refreshToken
) {
}

