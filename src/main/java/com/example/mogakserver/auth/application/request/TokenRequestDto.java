package com.example.mogakserver.auth.application.request;

public record TokenRequestDto(String accessToken, String refreshToken) {
}
