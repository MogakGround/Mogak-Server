package com.example.mogakserver.external.redis;

import com.example.mogakserver.external.socket.WebSocketBroadCaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final WebSocketBroadCaster webSocketBroadcaster;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern); // Redis 채널 이름
        String body = new String(message.getBody()); // 메시지 내용

        Long roomId = extractRoomIdFromChannel(channel);
        System.out.println("Received message: " + body + " for room: " + roomId);
        webSocketBroadcaster.broadcast(roomId, body);
    }

    private Long extractRoomIdFromChannel(String channel) {
        // 채널 이름에서 "room-" 접두사를 제거하고 숫자만 추출
        if (channel.startsWith("room-")) {
            String roomIdStr = channel.replace("room-", "");

            // 숫자만 처리하기 위해 예외 처리
            try {
                return Long.parseLong(roomIdStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid room ID: " + roomIdStr, e);
            }
        }
        throw new IllegalArgumentException("Invalid channel name: " + channel);
    }
}


