package com.example.mogakserver.room.application.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.common.exception.model.UnAuthorizedException;
import com.example.mogakserver.room.application.dto.RoomRequestDTO;
import com.example.mogakserver.room.application.dto.RoomUpdateDTO;
import com.example.mogakserver.room.domain.entity.Room;
import com.example.mogakserver.room.infra.repository.JpaRoomRepository;
import com.example.mogakserver.roomimg.domain.entity.RoomImg;
import com.example.mogakserver.roomimg.infra.repository.JpaRoomImgRepository;
import com.example.mogakserver.roomuser.domain.entity.RoomUser;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.worktime.domain.entity.WorkTime;
import com.example.mogakserver.worktime.infra.repository.JpaWorkTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {
    private final JpaRoomRepository roomRepository;
    private final JpaRoomImgRepository roomImgRepository;
    private final JpaWorkTimeRepository workTimeRepository;
    private final JpaRoomUserRepository roomUserRepository;

    @Transactional
    public long createRoom(Long userId, RoomRequestDTO roomRequest) {
        Room room = Room.builder()
                .roomName(roomRequest.getRoomName())
                .roomExplain(roomRequest.getRoomExplain())
                .isLocked(roomRequest.getIsLocked())
                .roomPassword(roomRequest.getRoomPassword())
                .userCnt(1)
                .build();

        roomRepository.save(room);

        RoomUser roomUser = RoomUser.builder()
                .userId(userId)
                .roomId(room.getId())
                .isHost(true)
                .isVideoLargeAllowed(false)
                .build();

        roomUserRepository.save(roomUser);

        RoomImg roomImg = RoomImg.builder()
                .roomId(room.getId())
                .roomImgType(roomRequest.getRoomImg())
                .build();

        roomImgRepository.save(roomImg);

        List<WorkTime> workTimes = roomRequest.getWorkHours().stream()
                .map(workHour -> WorkTime.builder()
                        .roomId(room.getId())
                        .workHour(workHour)
                        .build())
                .collect(Collectors.toList());

        workTimeRepository.saveAll(workTimes);

        return room.getId();
    }

    public void updateRoom(Long userId, Long roomId, RoomUpdateDTO roomUpdate) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ROOM_EXCEPTION));

        roomUserRepository.findByUserIdAndRoomId(userId, roomId)
                .orElseThrow(() -> new UnAuthorizedException(ErrorCode.ROOM_PERMISSION_DENIED));

        room.updateRoom(roomUpdate.getRoomName(), roomUpdate.getIsLocked(), roomUpdate.getRoomPassword());
    }
}
