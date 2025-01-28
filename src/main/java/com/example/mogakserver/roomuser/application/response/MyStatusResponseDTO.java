package com.example.mogakserver.roomuser.application.response;

import lombok.Builder;

@Builder
public record MyStatusResponseDTO(
        String nickName,
        boolean isScreenSharing,
        boolean isScreenAllowedLarge,
        boolean isTimerRunning,
        int hour,
        int min,
        int sec

) {
}
