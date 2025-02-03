package com.example.mogakserver.user.application.service;

import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserRankingScheduler {

    private final JpaUserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String RANKING_KEY = "user_ranking";
    private static final int PAGE_SIZE = 100; //DB에서 한 번에 가져올 유저 수
    private static final String QUEUE_NAME = "user_ranking_queue";

    @Scheduled(cron = "0 0/30 * * * ?") // 30분마다 실행
    public void syncRanking() {
        Long rankingSize = redisTemplate.opsForZSet().size(RANKING_KEY);

        if (rankingSize == null || rankingSize == 0) {
            initializeRanking(); //랭킹이 처음 비어있을때 초기화
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        int page = 0;
        Page<User> userPage;

        do {
            PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
            userPage = userRepository.findUsersUpdatedLast30Minutes(cutoff, pageRequest); // 변경된 유저만 가져오기

            for (User user : userPage.getContent()) {
                rabbitTemplate.convertAndSend(QUEUE_NAME, user.getId().toString());
            }
            page++;

        } while (userPage.hasNext());
    }

    private void initializeRanking() {
        int page = 0;
        Page<User> userPage;

        do {
            PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
            userPage = userRepository.findAll(pageRequest);

            for (User user : userPage.getContent()) {
                rabbitTemplate.convertAndSend(QUEUE_NAME, user.getId().toString());
            }
            page++;

        } while (userPage.hasNext());
    }

    @Scheduled(cron = "0 0 5 * * ?") // 매일 새벽 5시에 랭킹 초기화
    public void cleanupOldRanking() {
        redisTemplate.delete(RANKING_KEY);
    }
}
