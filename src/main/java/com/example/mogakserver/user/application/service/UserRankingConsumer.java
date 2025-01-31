package com.example.mogakserver.user.application.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserRankingConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final JpaUserRepository userRepository;
    private static final String RANKING_KEY = "user_ranking";

    @RabbitListener(queues = "user_ranking_queue", concurrency = "5-10")
    public void processRanking(String userIdStr, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));

            Double currentScore = redisTemplate.opsForZSet().score(RANKING_KEY, userIdStr);
            if (currentScore == null || !currentScore.equals(user.getTodayAddedTime().doubleValue())) {
                redisTemplate.opsForZSet().add(RANKING_KEY, userIdStr, user.getTodayAddedTime());
            }
            // 메시지 수동 ACK (더 빠르게 ACK 처리)
            channel.basicAck(tag, false);
        } catch (Exception e) {
            channel.basicNack(tag, false, true); // 메시지 재전송
        }
    }
}

