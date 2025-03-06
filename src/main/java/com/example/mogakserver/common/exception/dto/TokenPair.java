package com.example.mogakserver.common.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenPair(
        String accessToken, String refreshToken
) {
    public static TokenPair accessTokenResponse(String accessToken) {
        return new TokenPair(accessToken, null);
    }
}

