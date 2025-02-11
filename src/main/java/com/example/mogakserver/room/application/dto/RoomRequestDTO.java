package com.example.mogakserver.room.application.dto;

import com.example.mogakserver.roomimg.domain.entity.RoomImgType;
import com.example.mogakserver.worktime.domain.entity.WorkHour;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequestDTO {
    @NotNull
    private String roomName;

    private String roomExplain;

    @NotNull
    private RoomImgType roomImg;

    @NotNull
    private List<WorkHour> workHours;

    @NotNull
    private Boolean isLocked;

    private String roomPassword;

    @JsonIgnore
    @AssertTrue(message = "비밀번호를 입력해주세요.")
    public boolean isPasswordValid() {
        return !isLocked || (roomPassword != null && !roomPassword.isBlank());
    }
}
