package com.example.mogakserver.roomuser.application.response;

import lombok.Builder;

@Builder
public record RoomEnterResponseDTO(
        Long roomUserId
) {
}
