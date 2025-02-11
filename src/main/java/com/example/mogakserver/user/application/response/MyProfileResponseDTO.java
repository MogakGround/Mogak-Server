package com.example.mogakserver.user.application.response;

import lombok.Builder;

@Builder
public record MyProfileResponseDTO(
        String nickName,
        String portfolioUrl,
        int rank,
        int hour,
        int min,
        int sec
) {
}
