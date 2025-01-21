package com.example.mogakserver.room.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ScreenShareUsersListDTO(
        List<Long> users,
        int currentPage,
        int totalPages
) {

}
