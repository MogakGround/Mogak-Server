package com.example.mogakserver.user.application.service;

import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTimerScheduler {
    private final JpaUserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TIMER_KEY_PREFIX = "timer-room-";

    @Scheduled(cron = "0 0 5 * * ?")
    public void resetUserTimers() {
        resetUserTodayAddedTime();
        clearRedisTimer();
    }

    private void resetUserTodayAddedTime() {
        userRepository.findAll().forEach(user -> {
            user.resetTodayAddedTime();
            userRepository.save(user);
        });
    }

    private void clearRedisTimer() {
        Set<String> timerKeys = redisTemplate.keys(TIMER_KEY_PREFIX + "*");
        if (timerKeys != null && !timerKeys.isEmpty()) {
            redisTemplate.delete(timerKeys);
        }
    }
}