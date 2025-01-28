package com.example.mogakserver.room.application.dto;

import lombok.Builder;

@Builder
public record TimerDTO(
        Long userId,
        int hour,
        int min,
        int sec,
        boolean isRunning
) {
}
