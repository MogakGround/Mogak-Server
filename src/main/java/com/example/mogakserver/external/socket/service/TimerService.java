package com.example.mogakserver.external.socket.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JpaUserRepository userRepository;
    private static final String RANKING_KEY = "user_ranking";

    public void startTimer(Long roomId, Long userId) {
        String key = "timer-room-" + roomId;
        redisTemplate.opsForHash().put(key, userId + "-startTime", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().put(key, userId + "-isRunning", "true");
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void stopTimer(Long roomId, Long userId) {
        String key = "timer-room-" + roomId;
        long totalElapsedTime = getTotalElapsedTime(userId, key);
        updateElapsedTime(userId, key, totalElapsedTime);
        updateRanking(userId);

        redisTemplate.opsForHash().delete(key, userId + "-startTime");
        redisTemplate.opsForHash().put(key, userId + "-isRunning", "false");
    }

    private void updateElapsedTime(Long userId, String key, long totalElapsedTime) {
        redisTemplate.opsForHash().put(key, userId + "-elapsedTime", String.valueOf(totalElapsedTime));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        if (user.getVersion() == null) {
            user.setVersion(0);
        }

        user.updateTodayAddedTime(totalElapsedTime);

        try {
            userRepository.save(user);
        } catch (OptimisticLockException e) {
            log.warn("Optimistic lock conflict detected. Retrying updateElapsedTime for user: {}", userId);
            retryUpdateElapsedTime(userId, key, totalElapsedTime, 1); // 재귀 호출 제거 후 안전한 재시도 로직 추가
        }
    }
    private void retryUpdateElapsedTime(Long userId, String key, long totalElapsedTime, int attempt) {
        if (attempt > 3) { // 최대 3번 재시도 후 실패 로그 출력
            log.error("Failed to update elapsed time after 3 attempts for user: {}", userId);
            return;
        }

        try {
            updateElapsedTime(userId, key, totalElapsedTime);
        } catch (OptimisticLockException e) {
            log.warn("Retrying updateElapsedTime due to OptimisticLockException. Attempt: {}", attempt);
            retryUpdateElapsedTime(userId, key, totalElapsedTime, attempt + 1);
        }
    }
    private void updateRanking(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));

        redisTemplate.opsForZSet().add(RANKING_KEY, userId.toString(), user.getTodayAddedTime());
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
