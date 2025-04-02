package com.example.mogakserver.external.socket;

import com.example.mogakserver.common.config.jwt.JwtService;
import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.external.redis.RedisService;
import com.example.mogakserver.external.socket.dto.MessageDTO;
import com.example.mogakserver.external.socket.dto.MessageWrapper;
import com.example.mogakserver.external.socket.dto.ParticipantDTO;
import com.example.mogakserver.external.socket.service.ScreenShareService;
import com.example.mogakserver.external.socket.service.TimerService;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Arrays;
import java.util.List;
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
    private final JpaUserRepository jpaUserRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = getRoomId(session);
        Long userId = getUserIdFromQuery(session);

        if (sessionMap.containsKey(userId)) {
            WebSocketSession existingSession = sessionMap.get(userId);
            existingSession.close();
            sessionMap.remove(userId);
        }

        session.getAttributes().put("userId", userId);
        // sessionMap.put(userId, session);
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

        String userNickName = getUserNickName(userId);
        boolean isRunning = timerService.isTimerRunning(roomId, userId);
        ParticipantDTO participant = new ParticipantDTO(userId, userNickName, isRunning);

        String joinMessage = redisService.serializeObjectMessage(new MessageWrapper("participant-joined", participant));
        redisService.publishEvent(roomId, "participant-joined", userId);
        webSocketBroadcaster.broadcast(roomId, joinMessage);
    }

    private String getUserNickName(Long userId) {
        User user = jpaUserRepository.findById(userId).orElseThrow(()-> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        String userNickName = user.getNickName();
        return userNickName;
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
        Long userId = getUserIdFromSession(session);
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
        Long userId = getUserIdFromSession(session);
        if(timerService.isTimerRunning(roomId, userId)) {
            timerService.stopTimer(roomId, userId);
        }
        webSocketBroadcaster.removeSession(roomId, session);
        sessionMap.remove(userId);
        redisService.publishEvent(roomId, "participant-left", userId);
        redisService.unsubscribeToRoom(roomId);
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

    private Long getUserIdFromQuery(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query == null || !query.contains("token=")) {
            throw new IllegalArgumentException("Token query parameter missing");
        }

        String token = Arrays.stream(query.split("&"))
            .filter(param -> param.startsWith("token="))
            .findFirst()
            .map(param -> param.substring("token=".length()))
            .orElseThrow(() -> new IllegalArgumentException("Token parameter not found"));

        if (!jwtService.verifyToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }

        String decodedUserId = jwtService.getUserIdInToken(token);
        try {
            return Long.parseLong(decodedUserId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid userId in token");
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            throw new IllegalArgumentException("UserId not found in session");
        }
        return userId;
    }


    private MessageDTO createEventMessage(String type, Long userId) {
        return new MessageDTO(type, userId);
    }
}


