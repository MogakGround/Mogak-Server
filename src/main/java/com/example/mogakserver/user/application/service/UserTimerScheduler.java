package com.example.mogakserver.user.application.service;

import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

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
        handleRunningTimers();
        clearRedisTimer();
    }

    private void resetUserTodayAddedTime() {
        userRepository.findAll().forEach(user -> {
            user.resetTodayAddedTime();
            userRepository.save(user);
        });
    }

    private void handleRunningTimers() {
        Set<String> timerKeys = redisTemplate.keys(TIMER_KEY_PREFIX + "*");
        if (timerKeys != null && !timerKeys.isEmpty()) {
            for (String key : timerKeys) {
                Set<Object> userFields = redisTemplate.opsForHash().keys(key);
                for (Object field : userFields) {
                    String fieldStr = (String) field;
                    if (fieldStr.endsWith("-isRunning")) {
                        String userIdStr = fieldStr.replace("-isRunning", "");
                        boolean isRunning = "true".equals(redisTemplate.opsForHash().get(key, fieldStr));

                        if (isRunning) {
                            redisTemplate.opsForHash().put(key, userIdStr + "-startTime", String.valueOf(System.currentTimeMillis()));
                        }
                    }
                }
            }
        }
    }

    private void clearRedisTimer() {
        Set<String> timerKeys = redisTemplate.keys(TIMER_KEY_PREFIX + "*");
        if (timerKeys != null && !timerKeys.isEmpty()) {
            for (String key : timerKeys) {
                Set<Object> userFields = redisTemplate.opsForHash().keys(key);
                for (Object field : userFields) {
                    String fieldStr = (String) field;
                    if (fieldStr.endsWith("-elapsedTime")) {
                        redisTemplate.opsForHash().delete(key, field);
                    }
                }
            }
        }
    }
}