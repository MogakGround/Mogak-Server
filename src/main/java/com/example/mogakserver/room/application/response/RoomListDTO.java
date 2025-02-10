package com.example.mogakserver.room.application.response;

import com.example.mogakserver.room.application.dto.RoomDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record RoomListDTO(
        List<RoomDTO> rooms,
        int totalPages,
        int currentPage,
        long totalRooms
) {
}