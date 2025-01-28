package com.example.mogakserver.roomuser.application.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.roomuser.application.response.MyStatusResponseDTO;
import com.example.mogakserver.roomuser.domain.entity.RoomUser;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class RoomUserService {
    private final JpaRoomUserRepository roomUserRepository;
    private final JpaUserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public void updateIsScreenShareLargeAllowed(Long userId, Long roomId){
        RoomUser roomUser = roomUserRepository.findByUserIdAndRoomId(userId, roomId).orElseThrow(()->new NotFoundException(ErrorCode.NOT_FOUND_ROOM_EXCEPTION));
        roomUser.updateIsVideoLargeAllowed();
    }

    public MyStatusResponseDTO getStatus(Long userId, Long roomId) {
        String timerKey = "timer-room-" + roomId;
        String screenShareKey = "screen-share-room-" + roomId;

        String nickName = getNickName(userId);

        boolean isTimerRunning = isTimerRunning(userId, timerKey);

        long elapsedTime = getElapsedTime(userId, timerKey);

        int hour = (int) (elapsedTime / 3600);
        int min = (int) ((elapsedTime % 3600) / 60);
        int sec = (int) (elapsedTime % 60);

        boolean isScreenSharing = redisTemplate.opsForSet().isMember(screenShareKey, String.valueOf(userId));

        RoomUser roomUser = roomUserRepository.findByUserIdAndRoomId(userId, roomId).orElseThrow(()->new NotFoundException(ErrorCode.NOT_FOUND_ROOM_EXCEPTION));

        return MyStatusResponseDTO.builder()
                .nickName(nickName)
                .isScreenSharing(isScreenSharing)
                .isScreenAllowedLarge(roomUser.isVideoLargeAllowed())
                .isTimerRunning(isTimerRunning)
                .hour(hour)
                .min(min)
                .sec(sec)
                .build();
    }

    private long getElapsedTime(Long userId, String timerKey) {
        String elapsedTimeStr = (String) redisTemplate.opsForHash().get(timerKey, userId + "-elapsedTime");
        long elapsedTime = (elapsedTimeStr != null) ? Long.parseLong(elapsedTimeStr) : 0;
        return elapsedTime;
    }

    private boolean isTimerRunning(Long userId, String timerKey) {
        String isRunningStr = (String) redisTemplate.opsForHash().get(timerKey, userId + "-isRunning");
        boolean isTimerRunning = "true".equals(isRunningStr);
        return isTimerRunning;
    }

    private String getNickName(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
        String nickName = user.getNickName();
        return nickName;
    }
}

