package com.example.mogakserver.room.application.dto;

import lombok.Builder;

@Builder
public record TimerDTO(
        Long userId,
        String userNickname,
        int hour,
        int min,
        int sec,
        boolean isRunning
) {
}
