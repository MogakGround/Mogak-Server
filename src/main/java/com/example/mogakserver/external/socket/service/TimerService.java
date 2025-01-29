package com.example.mogakserver.external.socket.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JpaUserRepository userRepository;

    public void startTimer(Long roomId, Long userId) {
        String key = "timer-room-" + roomId;
        redisTemplate.opsForHash().put(key, userId + "-startTime", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().put(key, userId + "-isRunning", "true");
    }

    public void stopTimer(Long roomId, Long userId) {
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
