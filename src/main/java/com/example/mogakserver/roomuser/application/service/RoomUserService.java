package com.example.mogakserver.roomuser.application.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.common.exception.model.UnAuthorizedException;
import com.example.mogakserver.external.redis.RedisService;
import com.example.mogakserver.external.socket.WebSocketBroadCaster;
import com.example.mogakserver.external.socket.service.ScreenShareService;
import com.example.mogakserver.external.socket.service.TimerService;
import com.example.mogakserver.room.domain.entity.Room;
import com.example.mogakserver.room.infra.repository.JpaRoomRepository;
import com.example.mogakserver.roomimg.domain.entity.RoomImg;
import com.example.mogakserver.roomimg.infra.repository.JpaRoomImgRepository;
import com.example.mogakserver.roomuser.api.request.RoomEnterRequestDTO;
import com.example.mogakserver.roomuser.application.response.*;
import com.example.mogakserver.roomuser.domain.entity.RoomUser;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import com.example.mogakserver.worktime.domain.entity.WorkHour;
import com.example.mogakserver.worktime.domain.entity.WorkTime;
import com.example.mogakserver.worktime.infra.repository.JpaWorkTimeRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.mogakserver.external.socket.WebRtcWebSocketHandler.sessionMap;

@RequiredArgsConstructor
@Service
@Transactional
public class RoomUserService {
    private final JpaRoomUserRepository roomUserRepository;
    private final JpaRoomRepository roomRepository;
    private final JpaWorkTimeRepository workTimeRepository;
    private final JpaUserRepository userRepository;
    private final JpaRoomImgRepository roomImgRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ScreenShareService screenShareService;
    private final TimerService timerService;
    private final RedisService redisService;
    private final WebSocketBroadCaster webSocketBroadcaster;

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
    public MyPageUserRoomsListDTO getRoomListIMade(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<RoomUser> hostRoomUserPage = roomUserRepository.findHostRoomsByUserId(userId, pageable);

        return buildMyPageUserRoomsListDTO(page, hostRoomUserPage, pageable);
    }

    private MyPageUserRoomsListDTO buildMyPageUserRoomsListDTO(int page, Page<RoomUser> roomUserPage, Pageable pageable) {
        if (roomUserPage.isEmpty()) {
            return MyPageUserRoomsListDTO.builder()
                    .totalPage(0)
                    .currentPage(page)
                    .rooms(Collections.emptyList())
                    .build();
        }

        List<Long> roomIds = roomUserPage.stream()
                .map(RoomUser::getRoomId)
                .distinct()
                .collect(Collectors.toList());

        Page<Room> roomPage = roomRepository.findAllByIdIn(roomIds, pageable);
        Map<Long, List<WorkHour>> workHourMap = getWorkHourMap(roomIds);
        Map<Long, Long> roomTotalTimeMap = getTotalTimeForRooms(roomIds);
        Map<Long, String> roomImgMap = getRoomImgUrls(roomIds);

        List<MyPageUserRoomDTO> roomDTOList = getMyPageRoomDTOs(roomPage, roomTotalTimeMap, roomImgMap, workHourMap);

        return MyPageUserRoomsListDTO.builder()
                .totalPage(roomUserPage.getTotalPages())
                .currentPage(roomUserPage.getNumber() + 1)
                .rooms(roomDTOList)
                .build();
    }


    @NotNull
    private static List<MyPageUserRoomDTO> getMyPageRoomDTOs(Page<Room> roomPage, Map<Long, Long> roomTotalTimeMap, Map<Long, String> roomImgMap, Map<Long, List<WorkHour>> workHourMap) {
        List<MyPageUserRoomDTO> roomDTOList = roomPage.stream()
                .map(room -> {
                    long totalSeconds = roomTotalTimeMap.getOrDefault(room.getId(), 0L);
                    int hour = (int) (totalSeconds / 3600);
                    int min = (int) ((totalSeconds % 3600) / 60);
                    int sec = (int) (totalSeconds % 60);

                    return new MyPageUserRoomDTO(
                            room.getId(),
                            room.getRoomName(),
                            roomImgMap.getOrDefault(room.getId(), ""),
                            room.getRoomExplain(),
                            workHourMap.getOrDefault(room.getId(), Collections.emptyList()),
                            room.isLocked(),
                            room.getRoomPassword(),
                            hour, min, sec
                    );
                })
                .collect(Collectors.toList());
        return roomDTOList;
    }

    private Map<Long, String> getRoomImgUrls(List<Long> roomIds) {
        List<RoomImg> roomImgs = roomImgRepository.findByRoomIdIn(roomIds);
        return roomImgs.stream()
                .collect(Collectors.toMap(
                        RoomImg::getRoomId,
                        roomImg -> roomImg.getRoomImgType().getRoomImgUrl()
                ));
    }

    private Map<Long, Long> getTotalTimeForRooms(List<Long> roomIds) {
        Map<Long, Long> totalTimeMap = new HashMap<>();

        for (Long roomId : roomIds) {
            String key = "timer-room-" + roomId;
            Map<Object, Object> timers = redisTemplate.opsForHash().entries(key);

            long totalElapsedTime = 0;

            for (Object field : timers.keySet()) {
                String fieldStr = (String) field;
                if (fieldStr.endsWith("-elapsedTime")) {
                    String elapsedTimeStr = (String) redisTemplate.opsForHash().get(key, fieldStr);
                    long elapsedTime = (elapsedTimeStr != null) ? Long.parseLong(elapsedTimeStr) : 0;
                    totalElapsedTime += elapsedTime;
                }
            }
            totalTimeMap.put(roomId, totalElapsedTime);
        }
        return totalTimeMap;
    }

    @NotNull
    private Map<Long, List<WorkHour>> getWorkHourMap(List<Long> hostRoomIds) {
        return workTimeRepository.findAllByRoomIdIn(hostRoomIds)
                .stream()
                .collect(Collectors.groupingBy(
                        WorkTime::getRoomId,
                        Collectors.mapping(WorkTime::getWorkHour, Collectors.toList())
                ));
    }


    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public MyPageUserRoomsListDTO get7DaysEnteredRooms(Long userId, int page, int size) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<RoomUser> recentRoomUserPage = roomUserRepository.findRecentRoomUsers(userId, sevenDaysAgo, pageable);

        return buildMyPageUserRoomsListDTO(page, recentRoomUserPage, pageable);
    }

    public RoomEnterResponseDTO enterRoom(Long userId, Long roomId, RoomEnterRequestDTO request) {
        Room room = validateAndGetRoom(userId, roomId);

        validatePassword(request, room);

        Long roomUserId = getRoomUserId(userId, roomId, request);

        // 화면 공유 상태 저장
        if (request.isScreenShared()) {
            screenShareService.addScreenShareUser(roomId, userId);
        }

        return RoomEnterResponseDTO.builder()
                .roomUserId(roomUserId)
                .build();
    }

    private Room validateAndGetRoom(Long userId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_NOT_FOUND_EXCEPTION));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        return room;
    }

    private static void validatePassword(RoomEnterRequestDTO request, Room room) {
        // 비밀번호 검증
        if (room.getRoomPassword() != null && !room.getRoomPassword().isEmpty()) {
            if (request.password() == null || !room.getRoomPassword().equals(request.password())) {
                throw new UnAuthorizedException(ErrorCode.ROOM_PERMISSION_DENIED);
            }
        }
    }


    private Long getRoomUserId(Long userId, Long roomId, RoomEnterRequestDTO request) {
        Long roomUserId;
        boolean exists = roomUserRepository.existsByRoomIdAndUserId(roomId, userId);
        if (!exists) {
            RoomUser roomUser = RoomUser.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .isHost(false)
                    .isVideoLargeAllowed(request.isVideoLargeAllowed())
                    .build();
            roomUserRepository.save(roomUser);
            roomUserId = roomUser.getId();
        }else{
            roomUserId = roomUserRepository.findByUserIdAndRoomId(userId, roomId).get().getRoomId();
        }
        return roomUserId;
    }

    @Transactional
    public void quitRoom(Long userId, Long roomId) {
        validateAndGetRoom(userId, roomId);

        String timerKey = "timer-room-" + roomId;
        String isRunning = (String) redisTemplate.opsForHash().get(timerKey, userId + "-isRunning");

        if ("true".equals(isRunning)) {
            timerService.stopTimer(roomId, userId);
        }

        // 화면 공유 중이면 자동으로 중지
        Set<String> screenShareUsers = screenShareService.getScreenShareUsers(roomId);
        if (screenShareUsers.contains(userId.toString())) {
            screenShareService.removeScreenShareUser(roomId, userId);
            redisService.publishEvent(roomId, "screen-share-stop", userId);
        }

        // 세션 정리 (사용자 정보는 유지)
        webSocketBroadcaster.removeSession(roomId, sessionMap.get(userId));
        sessionMap.remove(userId);
    }
}

