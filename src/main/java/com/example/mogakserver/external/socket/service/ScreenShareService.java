package com.example.mogakserver.external.socket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScreenShareService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SCREEN_SHARE_KEY_PREFIX = "screen-share-room-";

    public void addScreenShareUser(Long roomId, Long userId) {
        String key = SCREEN_SHARE_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().add(key, String.valueOf(userId));
    }

    public void removeScreenShareUser(Long roomId, Long userId) {
        String key = SCREEN_SHARE_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().remove(key, String.valueOf(userId));
    }

    public Set<String> getScreenShareUsers(Long roomId) {
        return redisTemplate.opsForSet().members(SCREEN_SHARE_KEY_PREFIX + roomId);
    }

    public String createScreenShareMessage(Set<String> screenShareUsers) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return String.format("{\"type\":\"screen-share-users\", \"users\":%s}", objectMapper.writeValueAsString(screenShareUsers));
    }
}
