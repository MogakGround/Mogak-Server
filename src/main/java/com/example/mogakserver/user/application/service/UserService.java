package com.example.mogakserver.user.application.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.room.domain.repository.JpaRoomRepository;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.user.application.response.RankingDTO;
import com.example.mogakserver.user.application.response.RankingListDTO;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import com.example.mogakserver.worktime.domain.repository.JpaWorkTimeRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JpaUserRepository userRepository;
    private final JpaRoomRepository roomRepository;
    private final JpaWorkTimeRepository workTimeRepository;
    private static final String RANKING_KEY = "user_ranking";
    private final JpaRoomUserRepository roomUserRepository;

    @Transactional(readOnly=true)
    public RankingListDTO getPagedRanking(int page, int size) {
        long start = (long) (page - 1) * size;
        long end = start + size - 1;

        Set<ZSetOperations.TypedTuple<String>> rankingSet = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, start, end);

        Long totalCount = redisTemplate.opsForZSet().size(RANKING_KEY);
        int totalPages = (totalCount == null) ? 0 : (int) Math.ceil((double) totalCount / size);

        if (rankingSet == null) {
            return new RankingListDTO(Collections.emptyList(), totalPages, page);
        }

        List<RankingDTO> rankings = getRankingDTOS((int) start, rankingSet);

        return new RankingListDTO(rankings, totalPages, page);
    }

    @NotNull
    private List<RankingDTO> getRankingDTOS(int start, Set<ZSetOperations.TypedTuple<String>> rankingSet) {
        List<RankingDTO> rankings = new ArrayList<>();
        int rank = start + 1;
        for (ZSetOperations.TypedTuple<String> entry : rankingSet) {

            long totalSeconds = entry.getScore().longValue();
            int hour = (int) (totalSeconds / 3600);
            int min = (int) ((totalSeconds % 3600) / 60);
            int sec = (int) (totalSeconds % 60);

            Long userId = Long.parseLong(entry.getValue());
            User user = userRepository.findById(userId).orElseThrow(()->new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
            rankings.add(new RankingDTO(Long.parseLong(entry.getValue()), user.getNickName(),rank++, hour, min, sec));
        }
        return rankings;
    }

    @Transactional(readOnly = true)
    public RankingDTO getUserRanking(Long userId) {
        //0부터 시작
        Long rankIndex = redisTemplate.opsForZSet().reverseRank(RANKING_KEY, userId.toString());

        Double todayAddedTime = redisTemplate.opsForZSet().score(RANKING_KEY, userId.toString());

        if (rankIndex == null || todayAddedTime == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION);
        }

        int rank = rankIndex.intValue() + 1; //0based-> 1based
        long totalSeconds = todayAddedTime.longValue();
        int hour = (int) (totalSeconds / 3600);
        int min = (int) ((totalSeconds % 3600) / 60);
        int sec = (int) (totalSeconds % 60);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));

        return new RankingDTO(userId, user.getNickName(), rank, hour, min, sec);
    }
}

