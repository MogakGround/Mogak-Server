package com.example.mogakserver.room.application.response;

import com.example.mogakserver.room.application.dto.TimerDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record TimerListDTO(
        List<TimerDTO> timers,
        int totalPage,
        int currentPage
) {
}
