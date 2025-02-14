package com.example.mogakserver.external.socket;

import com.example.mogakserver.common.config.jwt.JwtService;
import com.example.mogakserver.external.redis.RedisService;
import com.example.mogakserver.external.socket.dto.MessageDTO;
import com.example.mogakserver.external.socket.service.ScreenShareService;
import com.example.mogakserver.external.socket.service.TimerService;
import lombok.RequiredArgsConstructor;
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

    private final WebSocketBroadCaster webSocketBroadcaster;
    private final RedisService redisService;
    private final JwtService jwtService;
    private final ScreenShareService screenShareService;
    private final TimerService timerService;

    public static final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = getRoomId(session);
        Long userId = getUserIdFromHeader(session);

        if (sessionMap.containsKey(userId)) {
            WebSocketSession existingSession = sessionMap.get(userId);
            existingSession.close();
            sessionMap.remove(userId);
        }

        sessionMap.put(userId, session);
        webSocketBroadcaster.addSession(roomId, session);

        redisService.subscribeToRoom(roomId);

        Set<String> screenShareUsers = screenShareService.getScreenShareUsers(roomId);
        // 새로운 사용자에게 현재 공유 중인 화면 정보를 보내줌
        if (!screenShareUsers.isEmpty()) {
            session.sendMessage(new TextMessage(screenShareService.createScreenShareMessage(screenShareUsers)));
        }

        // 새로운 사용자가 화면 공유를 해야 한다면(방 들어오기에서 미리 설정) 이벤트 트리거
        if (screenShareUsers.contains(userId.toString())) {
            redisService.publishEvent(roomId, "screen-share-start", userId);
            webSocketBroadcaster.broadcast(roomId, redisService.serializeMessage(createEventMessage("screen-share-start", userId)));
        }

        redisService.publishEvent(roomId, "participant-joined", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        MessageDTO messageDto = redisService.parseMessage(message.getPayload());
        if (messageDto == null) {
            session.sendMessage(new TextMessage("{\"type\":\"error\", \"message\":\"Invalid JSON format\"}"));
            return;
        }
        String messageType = messageDto.type();
        Long roomId = getRoomId(session);
        Long userId = getUserIdFromHeader(session);
        String eventMessage = redisService.serializeMessage(createEventMessage(messageType, userId));
        switch (messageDto.type()) {
            case "screen-share-start":
                screenShareService.addScreenShareUser(roomId, userId);
                redisService.publishEvent(roomId, "screen-share-start", userId);
                webSocketBroadcaster.broadcast(roomId, eventMessage);
                break;

            case "screen-share-stop":
                screenShareService.removeScreenShareUser(roomId, userId);
                redisService.publishEvent(roomId, "screen-share-stop", userId);
                webSocketBroadcaster.broadcast(roomId, eventMessage);
                break;

            case "timer-start":
                timerService.startTimer(roomId, userId);
                redisService.publishEvent(roomId, "timer-start", userId);
                webSocketBroadcaster.broadcast(roomId, eventMessage);
                break;

            case "timer-stop":
                timerService.stopTimer(roomId, userId);
                redisService.publishEvent(roomId, "timer-stop", userId);
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

        redisService.publishEvent(roomId, "participant-left", userId);
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
    private MessageDTO createEventMessage(String type, Long userId) {
        return new MessageDTO(type, userId);
    }
}


