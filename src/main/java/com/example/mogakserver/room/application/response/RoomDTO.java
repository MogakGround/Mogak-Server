package com.example.mogakserver.room.application.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomDTO(
        Long roomId,
        String roomName,
        String roomExplain,
        Boolean isLocked,
        Boolean isHost,
        Integer userCnt,
        String roomImg,
        List<String> workHours
) {}
