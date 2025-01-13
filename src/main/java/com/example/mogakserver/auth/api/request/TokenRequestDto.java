package com.example.mogakserver.auth.api.request;

public record TokenRequestDto(String accessToken, String refreshToken) {
}
