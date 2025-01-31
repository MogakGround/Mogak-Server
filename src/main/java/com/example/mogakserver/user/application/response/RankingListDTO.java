package com.example.mogakserver.user.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RankingListDTO(
        List<RankingDTO> rankings,
        int totalPage,
        int currentPage
) {
}
