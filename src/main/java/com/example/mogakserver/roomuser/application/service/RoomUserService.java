package com.example.mogakserver.roomuser.application.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.room.domain.entity.Room;
import com.example.mogakserver.room.domain.repository.JpaRoomRepository;
import com.example.mogakserver.roomuser.application.response.MyStatusResponseDTO;
import com.example.mogakserver.roomuser.application.response.UserRoomDTO;
import com.example.mogakserver.roomuser.application.response.UserRoomsListDTO;
import com.example.mogakserver.roomuser.application.response.RoomUserDTO;
import com.example.mogakserver.roomuser.application.response.RoomUserListDTO;
import com.example.mogakserver.roomuser.domain.entity.RoomUser;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import com.example.mogakserver.worktime.domain.entity.WorkTime;
import com.example.mogakserver.worktime.domain.repository.JpaWorkTimeRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class RoomUserService {
    private final JpaRoomUserRepository roomUserRepository;
    private final JpaRoomRepository roomRepository;
    private final JpaWorkTimeRepository workTimeRepository;
    private final JpaUserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public void updateIsScreenShareLargeAllowed(Long userId, Long roomId){
        RoomUser roomUser = roomUserRepository.findByUserIdAndRoomId(userId, roomId).orElseThrow(()->new NotFoundException(ErrorCode.NOT_FOUND_ROOM_EXCEPTION));
        roomUser.updateIsVideoLargeAllowed();
    }
    @Transactional(readOnly = true)
    public MyStatusResponseDTO getStatus(Long userId, Long roomId) {
        String timerKey = "timer-room-" + roomId;
        String screenShareKey = "screen-share-room-" + roomId;

        String nickName = getNickName(userId);

        boolean isTimerRunning = isTimerRunning(userId, timerKey);

        long elapsedTime = getElapsedTime(userId, timerKey);

        int hour = (int) (elapsedTime / 3600);
        int min = (int) ((elapsedTime % 3600) / 60);
        int sec = (int) (elapsedTime % 60);

        boolean isScreenSharing = redisTemplate.opsForSet().isMember(screenShareKey, String.valueOf(userId));

        RoomUser roomUser = roomUserRepository.findByUserIdAndRoomId(userId, roomId).orElseThrow(()->new NotFoundException(ErrorCode.NOT_FOUND_ROOM_EXCEPTION));

        return MyStatusResponseDTO.builder()
                .nickName(nickName)
                .isScreenSharing(isScreenSharing)
                .isScreenAllowedLarge(roomUser.isVideoLargeAllowed())
                .isTimerRunning(isTimerRunning)
                .hour(hour)
                .min(min)
                .sec(sec)
                .build();
    }

    private long getElapsedTime(Long userId, String timerKey) {
        String elapsedTimeStr = (String) redisTemplate.opsForHash().get(timerKey, userId + "-elapsedTime");
        long elapsedTime = (elapsedTimeStr != null) ? Long.parseLong(elapsedTimeStr) : 0;
        return elapsedTime;
    }

    private boolean isTimerRunning(Long userId, String timerKey) {
        String isRunningStr = (String) redisTemplate.opsForHash().get(timerKey, userId + "-isRunning");
        boolean isTimerRunning = "true".equals(isRunningStr);
        return isTimerRunning;
    }

    private String getNickName(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        String nickName = user.getNickName();
        return nickName;
    }

    @Transactional(readOnly = true)
    public UserRoomsListDTO getRoomListIMade(Long userId, int page, int size) {
        List<RoomUser> roomUserList = roomUserRepository.findAllByUserId(userId);

        if (roomUserList == null || roomUserList.isEmpty()) {
            return UserRoomsListDTO.builder()
                    .totalPage(0)
                    .currentPage(page)
                    .rooms(Collections.emptyList()) // 빈 리스트 반환
                    .build();
        }

        List<Long> hostRoomIds = roomUserList.stream()
                .filter(RoomUser::isHost)
                .map(RoomUser::getRoomId)
                .collect(Collectors.toList());

        // 방장인 방이 없을 경우 빈 리스트 반환
        if (hostRoomIds.isEmpty()) {
            return UserRoomsListDTO.builder()
                    .totalPage(0)
                    .currentPage(page)
                    .rooms(Collections.emptyList())
                    .build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Room> roomPage = roomRepository.findAllByIdIn(hostRoomIds, pageable);

        Map<Long, List<String>> workHourMap = getWorkHourMap(hostRoomIds);

        List<UserRoomDTO> roomDTOList = getUserMadeRoomDTOS(roomPage, workHourMap);

        return UserRoomsListDTO.builder()
                .totalPage(roomPage.getTotalPages())
                .currentPage(roomPage.getNumber())
                .rooms(roomDTOList)
                .build();
    }

    @NotNull
    private Map<Long, List<String>> getWorkHourMap(List<Long> hostRoomIds) {
        Map<Long, List<String>> workHourMap = workTimeRepository.findAllByRoomIdIn(hostRoomIds)
                .stream()
                .collect(Collectors.groupingBy(
                        WorkTime::getRoomId,
                        Collectors.mapping(workTime -> workTime.getWorkHour().getValue(), Collectors.toList()) // ✅ Enum을 String으로 변환
                ));
        return workHourMap;
    }

    @NotNull
    private static List<UserRoomDTO> getUserMadeRoomDTOS(Page<Room> roomPage, Map<Long, List<String>> workHourMap) {
        List<UserRoomDTO> roomDTOList = roomPage.getContent().stream()
                .map(room -> UserRoomDTO.builder()
                        .roomId(room.getId())
                        .roomName(room.getRoomName())
                        .roomExplain(room.getRoomExplain())
                        .workHour(workHourMap.getOrDefault(room.getId(), Collections.emptyList()))
                        .isLocked(room.isLocked())
                        .roomPassword(room.getRoomPassword())
                        .isHost(true)
                        .build())
                .collect(Collectors.toList());
        return roomDTOList;
    }
    public RoomUserListDTO getRoomUsers(Long userId, Long roomId) {
        boolean isParticipant = roomUserRepository.findByUserIdAndRoomId(userId, roomId).isPresent();
        if (!isParticipant) {
            throw new NotFoundException(ErrorCode.ROOM_PERMISSION_DENIED);
        }
        
        List<RoomUserDTO> users = roomUserRepository.findByRoomId(roomId).stream()
                .map(roomUser -> {
                    User user = userRepository.findById(roomUser.getUserId())
                            .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
                    return RoomUserDTO.builder()
                            .userId(user.getId())
                            .nickName(user.getNickName())
                            .build();
                })
                .collect(Collectors.toList());

        return RoomUserListDTO.builder()
                .users(users)
                .userCnt(users.size())
                .build();
    }
}

