package com.example.mogakserver.common.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenPair(
        String accessToken,

        @Schema(hidden = true)
        String refreshToken
) {
    public static TokenPair accessTokenResponse(String accessToken) {
        return new TokenPair(accessToken, null);
    }
}

