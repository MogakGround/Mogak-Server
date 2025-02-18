package com.example.mogakserver.room.application.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.ConflictException;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.room.application.dto.TimerDTO;
import com.example.mogakserver.room.application.response.RoomDTO;
import com.example.mogakserver.room.application.response.RoomListDTO;
import com.example.mogakserver.room.application.response.ScreenShareUsersListDTO;
import com.example.mogakserver.room.application.response.TimerListDTO;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import com.example.mogakserver.room.domain.entity.Room;
import com.example.mogakserver.room.infra.repository.JpaRoomRepository;
import com.example.mogakserver.roomimg.domain.entity.RoomImgType;
import com.example.mogakserver.roomimg.infra.repository.JpaRoomImgRepository;
import com.example.mogakserver.roomuser.domain.entity.RoomUser;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.worktime.domain.entity.WorkHour;
import com.example.mogakserver.worktime.infra.repository.JpaWorkTimeRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class RoomRetrieveService {
    private static final String SCREEN_SHARE_KEY_PREFIX = "screen-share-room-";
    private final JpaRoomRepository roomRepository;
    private final JpaRoomImgRepository roomImgRepository;
    private final JpaWorkTimeRepository workTimeRepository;
    private final JpaRoomUserRepository roomUserRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JpaUserRepository jpaUserRepository;

    @Transactional(readOnly = true)
    public ScreenShareUsersListDTO getScreenShareUsers(Long roomId, int page, int size) {
        String key = SCREEN_SHARE_KEY_PREFIX + roomId;
        Set<String> userIds = redisTemplate.opsForSet().members(key);

        if (userIds == null || userIds.isEmpty()) {
            return createEmptyScreenShareUsersDTO(page, 0);
        }

        List<Long> sortedUsers = userIds.stream()
                .map(Long::valueOf)
                .sorted()
                .collect(Collectors.toList());

        return createPagedScreenShareUsersDTO(sortedUsers, page, size);
    }

    private ScreenShareUsersListDTO createEmptyScreenShareUsersDTO(int currentPage, int totalPages) {
        return ScreenShareUsersListDTO.builder()
                .users(Collections.emptyList())
                .currentPage(currentPage)
                .totalPages(totalPages)
                .build();
    }

    private ScreenShareUsersListDTO createPagedScreenShareUsersDTO(List<Long> users, int page, int size) {
        int totalUsers = users.size();
        int totalPages = (int) Math.ceil((double) totalUsers / size);

        // 페이지 범위를 초과한 경우 빈 결과
        if (page >= totalPages) {
            return createEmptyScreenShareUsersDTO(page, totalPages);
        }

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalUsers);
        List<Long> pagedUsers = users.subList(fromIndex, toIndex);

        return ScreenShareUsersListDTO.builder()
                .users(pagedUsers)
                .currentPage(page)
                .totalPages(totalPages)
                .build();
    }
    @Transactional(readOnly = true)
    public TimerListDTO getPagedTimers(Long roomId, int page, int size) {
        String key = "timer-room-" + roomId;

        Map<Object, Object> timers = redisTemplate.opsForHash().entries(key);
        List<TimerDTO> timerList = new ArrayList<>();

        getTimerList(key, timers, timerList);

        int totalPage = (int) Math.ceil((double) timerList.size() / size);
        List<TimerDTO> pagedTimerList = getPagedTimerList(page, size, timerList);

        return TimerListDTO.builder()
                .timers(pagedTimerList)
                .totalPage(totalPage)
                .currentPage(page)
                .build();
    }
    private void getTimerList(String key, Map<Object, Object> timers, List<TimerDTO> timerList) {
        for (Object field : timers.keySet()) {
            String fieldStr = (String) field;

            if (fieldStr.endsWith("-elapsedTime")) {
                //userId 추출
                String userIdStr = fieldStr.replace("-elapsedTime", "");
                Long userId = Long.parseLong(userIdStr);
                User user = jpaUserRepository.findById(userId).orElseThrow(()-> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
                String userNickName = user.getNickName();


                String elapsedTimeStr = (String) redisTemplate.opsForHash().get(key, userId + "-elapsedTime");
                String isRunningStr = (String) redisTemplate.opsForHash().get(key, userId + "-isRunning");

                long elapsedTime = (elapsedTimeStr != null) ? Long.parseLong(elapsedTimeStr) : 0;

                boolean isRunning = "true".equals(isRunningStr);

                int hour = (int) (elapsedTime / 3600);
                int min = (int) ((elapsedTime % 3600) / 60);
                int sec = (int) (elapsedTime % 60);

                timerList.add(TimerDTO.builder()
                        .userId(userId)
                        .userNickname(userNickName)
                        .hour(hour)
                        .min(min)
                        .sec(sec)
                        .isRunning(isRunning)
                        .build());
            }
        }
    }

    @NotNull
    private static List<TimerDTO> getPagedTimerList(int page, int size, List<TimerDTO> timerList) {
        int start = (page - 1) * size;
        int end = Math.min(start + size, timerList.size());
        List<TimerDTO> pagedTimerList = (start >= timerList.size())
                ? Collections.emptyList()
                : timerList.subList(start, end);
        return pagedTimerList;
    }

    @Transactional(readOnly = true)
    public RoomListDTO getAllRooms(int page, int size, List<String> workHourList) {
        size = size > 0 ? size : 12;
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<Room> roomPage;

        if (workHourList == null || workHourList.isEmpty()) {
            roomPage = roomRepository.findAll(pageRequest);
        } else {
            List<WorkHour> workHours = workHourList.stream()
                    .map(WorkHour::valueOf)
                    .collect(Collectors.toList());
            roomPage = roomRepository.findRoomsByWorkHours(workHours, pageRequest);

        }

        List<RoomDTO> roomDTOs = roomPage.getContent().stream()
                .map(room -> toRoomDTO(null, room))
                .collect(Collectors.toList());

        return RoomListDTO.builder()
                .rooms(roomDTOs)
                .totalPages(roomPage.getTotalPages())
                .currentPage(page)
                .totalRooms(roomPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public RoomDTO getRoomById(Long userId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_ROOM_EXCEPTION));

        return toRoomDTO(userId, room);
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> getRecentRooms() {
        return roomRepository.findTop4ByOrderByCreatedAtDesc().stream()
                .map(room -> toRoomDTO(null, room))
                .collect(Collectors.toList());
    }

    private RoomDTO toRoomDTO(Long userId, Room room) {
        return RoomDTO.builder()
                .roomId(room.getId())
                .roomName(room.getRoomName())
                .roomExplain(room.getRoomExplain())
                .isLocked(room.isLocked())
                .userCnt(room.getUserCnt())
                .roomImg(getRoomImgUrl(room.getId()))
                .workHours(workTimeRepository.findWorkHoursByRoomId(room.getId()))
                .isHost(userId != null && isUserHost(userId, room.getId()))
                .build();
    }

    private String getRoomImgUrl(Long roomId) {
        RoomImgType roomImgType = roomImgRepository.findRoomImgTypeByRoomId(roomId);
        return roomImgType.getRoomImgUrl();
    }

    private boolean isUserHost(Long userId, Long roomId) {
        return roomUserRepository.findByUserIdAndRoomId(userId, roomId)
                .map(RoomUser::isHost)
                .orElse(false);
    }

    public void isRoomNameAvailable(String roomName) {
        boolean exists = roomRepository.existsByRoomName(roomName);
        if (exists) {
            throw new ConflictException(ErrorCode.ALREADY_EXIST_ROOM_EXCEPTION);
        }
    }

}
