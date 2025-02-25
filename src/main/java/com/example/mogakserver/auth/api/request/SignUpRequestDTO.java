package com.example.mogakserver.auth.api.request;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequestDTO(
        @NotBlank Long kakaoId,
        @NotBlank String nickName,
        String portfolioUrl
) {
}