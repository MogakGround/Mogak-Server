package com.example.mogakserver.external.socket;

import com.example.mogakserver.common.config.jwt.JwtService;
import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.external.socket.dto.MessageDTO;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebRtcWebSocketHandler extends TextWebSocketHandler {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;
    private final MessageListenerAdapter listenerAdapter;
    private final WebSocketBroadCaster webSocketBroadcaster;
    private final JpaUserRepository userRepository;
    private final JpaRoomUserRepository roomUserRepository;

    private static final String CHANNEL_PREFIX = "room-";
    private static final String SCREEN_SHARE_KEY_PREFIX = "screen-share-room-";
    private static final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = getRoomId(session);
        Long userId = getUserIdFromHeader(session);

        // 중복 연결 체크
        if (sessionMap.containsKey(userId)) {
            WebSocketSession existingSession = sessionMap.get(userId);
            existingSession.close();
            sessionMap.remove(userId);
        }

        sessionMap.put(userId, session);
        webSocketBroadcaster.addSession(roomId, session);

        // Redis 구독 동적으로 추가
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.addMessageListener(listenerAdapter, new PatternTopic(getChannelName(roomId)));

        // 화면 공유 중인 사용자 목록
        Set<String> screenShareUsers = getScreenShareUsers(roomId);

        if (!screenShareUsers.isEmpty()) {
            String screenShareMessage = String.format(
                    "{\"type\":\"screen-share-users\", \"users\":%s}",
                    new ObjectMapper().writeValueAsString(screenShareUsers)
            );
            session.sendMessage(new TextMessage(screenShareMessage));
        }

        redisTemplate.convertAndSend(getChannelName(roomId), serializeMessage(createEventMessage("participant-joined", userId)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        MessageDTO messageDto = parseMessage(payload);
        if (messageDto == null) {
            session.sendMessage(new TextMessage("{\"type\":\"error\", \"message\":\"Invalid JSON format\"}"));
            return;
        }

        String messageType = messageDto.type();
        Long roomId = getRoomId(session);
        Long userId = getUserIdFromHeader(session);

        String eventMessage = serializeMessage(createEventMessage(messageType, userId));

        switch (messageType) {
            case "screen-share-start":
                publishToRedis(roomId, eventMessage);
                addScreenShareUser(roomId, userId);
                webSocketBroadcaster.broadcast(roomId, eventMessage);
                break;

            case "screen-share-stop":
                publishToRedis(roomId, eventMessage);
                removeScreenShareUser(roomId, userId);
                webSocketBroadcaster.broadcast(roomId, eventMessage);
                break;
            case "timer-start":
                publishToRedis(roomId, eventMessage);
                startTimer(roomId, userId);
                webSocketBroadcaster.broadcast(roomId, eventMessage);
                break;
            case "timer-stop":
                publishToRedis(roomId, eventMessage);
                stopTimer(roomId, userId);
                webSocketBroadcaster.broadcast(roomId, eventMessage);
                break;

            default:
                session.sendMessage(new TextMessage("{\"type\":\"error\", \"message\":\"Unknown message type\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = getRoomId(session);
        Long userId = getUserIdFromHeader(session);

        webSocketBroadcaster.removeSession(roomId, session);
        sessionMap.remove(userId);

        redisTemplate.convertAndSend(getChannelName(roomId), serializeMessage(createEventMessage("participant-left", userId)));
    }

    // 화면 공유 상태 추가
    private void addScreenShareUser(Long roomId, Long userId) {
        String key = SCREEN_SHARE_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().add(key, String.valueOf(userId));
    }

    // 화면 공유 상태 제거
    private void removeScreenShareUser(Long roomId, Long userId) {
        String key = SCREEN_SHARE_KEY_PREFIX + roomId;
        redisTemplate.opsForSet().remove(key, String.valueOf(userId));
    }

    private Long getRoomId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        try {
            String roomIdStr = query.split("roomId=")[1].split("&")[0];
            return Long.parseLong(roomIdStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid roomId in URL query: " + query);
        }
    }

    private Long getUserIdFromHeader(WebSocketSession session) {
        String token = session.getHandshakeHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header missing or invalid");
        }

        String encodedUserId = token.substring("Bearer ".length());
        if (!jwtService.verifyToken(encodedUserId)) {
            throw new IllegalArgumentException("Invalid token");
        }

        String decodedUserId = jwtService.getUserIdInToken(encodedUserId);
        try {
            return Long.parseLong(decodedUserId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid userId in token");
        }
    }

    private void publishToRedis(Long roomId, String message) {
        redisTemplate.convertAndSend(getChannelName(roomId), message);
    }

    private String getChannelName(Long roomId) {
        return CHANNEL_PREFIX + roomId;
    }
    private Set<String> getScreenShareUsers(Long roomId) {
        String key = SCREEN_SHARE_KEY_PREFIX + roomId;
        return redisTemplate.opsForSet().members(key);
    }

    private MessageDTO parseMessage(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, MessageDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private String serializeMessage(MessageDTO message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    private MessageDTO createEventMessage(String type, Long userId) {
        return new MessageDTO(type, userId);
    }

    private void startTimer(Long roomId, Long userId) {
        String key = "timer-room-" + roomId;

        //경과시간 초기화
        String elapsedTimeStr = (String) redisTemplate.opsForHash().get(key, userId + "-elapsedTime");
        long elapsedTime = (elapsedTimeStr != null) ? Long.parseLong(elapsedTimeStr) : 0;

        long startTime = System.currentTimeMillis();
        redisTemplate.opsForHash().put(key, userId + "-startTime", String.valueOf(startTime));
        redisTemplate.opsForHash().put(key, userId + "-elapsedTime", String.valueOf(elapsedTime));
        redisTemplate.opsForHash().put(key, userId + "-isRunning", "true");
    }

    private void stopTimer(Long roomId, Long userId) {
        String key = "timer-room-" + roomId;

        long totalElapsedTime = getTotalElapsedTime(userId, key);
        updateElapsedTime(userId, key, totalElapsedTime);

        redisTemplate.opsForHash().delete(key, userId + "-startTime");
        redisTemplate.opsForHash().put(key, userId + "-isRunning", "false");
    }

    private void updateElapsedTime(Long userId, String key, long totalElapsedTime) {
        //방에서 총 경과 시간
        redisTemplate.opsForHash().put(key, userId + "-elapsedTime", String.valueOf(totalElapsedTime));

        //오늘 유저의 모든 방 총 경과 시간
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        user.updateTodayAddedTime(totalElapsedTime);
        userRepository.save(user);
    }

    private long getTotalElapsedTime(Long userId, String key) {
        String startTimeStr = (String) redisTemplate.opsForHash().get(key, userId + "-startTime");
        String elapsedTimeStr = (String) redisTemplate.opsForHash().get(key, userId + "-elapsedTime");
        if (startTimeStr == null) {
            throw new IllegalArgumentException("Timer not started for userId: " + userId);
        }

        long startTime = Long.parseLong(startTimeStr);
        long elapsedTime = (elapsedTimeStr != null) ? Long.parseLong(elapsedTimeStr) : 0;

        // 현재까지 경과 시간 계산
        long currentElapsedTime = (System.currentTimeMillis() - startTime) / 1000; // 초 단위
        long totalElapsedTime = elapsedTime + currentElapsedTime;
        return totalElapsedTime;
    }
}


