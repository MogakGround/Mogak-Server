package com.example.mogakserver.roomuser.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserRoomDTO(
        Long roomId,
        String roomName,
        String roomImgUrl,
        String roomExplain,
        List<String> workHour,
        boolean isLocked,
        String roomPassword,
        boolean isHost
) {
}
