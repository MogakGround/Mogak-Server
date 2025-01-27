package com.example.mogakserver.room.application.service;

import com.example.mogakserver.room.application.dto.TimerDTO;
import com.example.mogakserver.room.application.response.ScreenShareUsersListDTO;
import com.example.mogakserver.room.application.response.TimerListDTO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
    private final RedisTemplate<String, String> redisTemplate;

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

                String elapsedTimeStr = (String) redisTemplate.opsForHash().get(key, userId + "-elapsedTime");
                String isRunningStr = (String) redisTemplate.opsForHash().get(key, userId + "-isRunning");

                long elapsedTime = (elapsedTimeStr != null) ? Long.parseLong(elapsedTimeStr) : 0;

                boolean isRunning = "true".equals(isRunningStr);

                int hour = (int) (elapsedTime / 3600);
                int min = (int) ((elapsedTime % 3600) / 60);
                int sec = (int) (elapsedTime % 60);

                timerList.add(TimerDTO.builder()
                        .userId(userId)
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
}
