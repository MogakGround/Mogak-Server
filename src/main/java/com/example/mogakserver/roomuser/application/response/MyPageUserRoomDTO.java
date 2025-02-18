package com.example.mogakserver.roomuser.application.response;

import com.example.mogakserver.worktime.domain.entity.WorkHour;
import lombok.Builder;

import java.util.List;

@Builder
public record MyPageUserRoomDTO(
        Long roomId,
        String roomName,
        String roomImgUrl,
        String roomExplain,
        List<WorkHour> workHour,
        boolean isLocked,
        String roomPassword,
        int hour,
        int min,
        int sec
) {
}
