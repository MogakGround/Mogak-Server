package com.example.mogakserver.roomuser.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserRoomsListDTO(
    int totalPage,
    int currentPage,
    List<UserRoomDTO> rooms
) {
}
