package com.example.mogakserver.roomuser.api.request;

public record RoomEnterRequestDTO(
        String password,
        boolean isScreenShared,
        boolean isVideoLargeAllowed
) {

}
