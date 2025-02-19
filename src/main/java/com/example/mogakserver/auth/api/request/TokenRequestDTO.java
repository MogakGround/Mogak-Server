package com.example.mogakserver.auth.api.request;

public record TokenRequestDTO(String accessToken, String refreshToken) {
}
