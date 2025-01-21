package com.example.mogakserver.room.application.service;

import com.example.mogakserver.room.application.response.ScreenShareUsersListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoomService {
    private static final String SCREEN_SHARE_KEY_PREFIX = "screen-share-room-";
    private final RedisTemplate<String, String> redisTemplate;
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
}
