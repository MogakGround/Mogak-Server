package com.example.mogakserver.auth.application.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignUpRequestDto(
        @NotNull Long kakaoId,
        @NotBlank String nickName,
        String portfolioUrl) {
}