package com.example.mogakserver.auth.api.request;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequestDTO(
        @NotBlank String kakaoCode,
        @NotBlank String nickName,
        String portfolioUrl
) {
}