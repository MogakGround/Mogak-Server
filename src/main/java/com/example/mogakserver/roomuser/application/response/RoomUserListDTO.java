package com.example.mogakserver.roomuser.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomUserListDTO(
        List<RoomUserDTO> users,
        int userCnt
) {}