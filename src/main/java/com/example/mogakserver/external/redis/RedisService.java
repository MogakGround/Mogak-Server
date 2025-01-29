package com.example.mogakserver.external.redis;

import com.example.mogakserver.external.socket.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String CHANNEL_PREFIX = "room-";

    public void subscribeToRoom(Long roomId) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.addMessageListener(new MessageListenerAdapter(), new PatternTopic(getChannelName(roomId)));
    }

    public void publishEvent(Long roomId, String eventType, Long userId) {
        String message = serializeMessage(new MessageDTO(eventType, userId));
        redisTemplate.convertAndSend(getChannelName(roomId), message);
    }

    private String getChannelName(Long roomId) {
        return CHANNEL_PREFIX + roomId;
    }

    public MessageDTO parseMessage(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, MessageDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String serializeMessage(MessageDTO message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }
}
