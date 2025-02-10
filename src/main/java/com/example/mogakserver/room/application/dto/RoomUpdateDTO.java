package com.example.mogakserver.room.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateDTO {
    @NotNull
    private String roomName;

    @NotNull
    private Boolean isLocked;

    private String roomPassword;

    @JsonIgnore
    @AssertTrue(message = "비밀번호를 입력해주세요.")
    public boolean isPasswordValid() {
        return !isLocked || (roomPassword != null && !roomPassword.isBlank());
    }
}