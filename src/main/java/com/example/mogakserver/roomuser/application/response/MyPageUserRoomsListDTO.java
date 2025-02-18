package com.example.mogakserver.roomuser.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record MyPageUserRoomsListDTO(
    int totalPage,
    int currentPage,
    List<MyPageUserRoomDTO> rooms
) {
}
