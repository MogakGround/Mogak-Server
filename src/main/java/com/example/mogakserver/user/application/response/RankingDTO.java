package com.example.mogakserver.user.application.response;

import lombok.Builder;

@Builder
public record RankingDTO(
        Long userId,
        String nickName,
        int rank,
        int hour,
        int min,
        int sec

) {
}
