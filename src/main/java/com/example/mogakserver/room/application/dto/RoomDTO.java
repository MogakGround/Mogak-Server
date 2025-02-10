package com.example.mogakserver.room.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomDTO(
        Long roomId,
        String roomName,
        String roomExplain,
        Boolean isLocked,
        Integer userCnt,
        String roomImg,
        List<String> workHours
) {}
