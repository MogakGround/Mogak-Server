package com.example.mogakserver.common.config.redis;

import com.example.mogakserver.external.socket.WebSocketBroadCaster;
import lombok.RequiredArgsConstructor;
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
        webSocketBroadcaster.broadcast(roomId, body);
    }

    private Long extractRoomIdFromChannel(String channel) {
        if (channel.startsWith("room-")) {
            return Long.parseLong(channel.replace("room-", ""));
        }
        throw new IllegalArgumentException("Invalid channel name: " + channel);
    }
}


