package com.example.mogakserver.user.application.service;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import com.example.mogakserver.common.exception.model.ConflictException;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.user.application.response.MyProfileResponseDTO;
import com.example.mogakserver.user.application.response.RankingDTO;
import com.example.mogakserver.user.application.response.RankingListDTO;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
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
    private static final String RANKING_KEY = "user_ranking";

    @Transactional(readOnly = true)
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
            Long userId = Long.parseLong(entry.getValue());
            rankings.add(convertToRankingDTO(userId, rank++, entry.getScore().longValue()));
        }
        return rankings;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION));
    }

    private RankingDTO convertToRankingDTO(Long userId, int rank, long totalSeconds) {
        User user = getUser(userId);
        return new RankingDTO(userId, user.getNickName(), rank,
                (int) (totalSeconds / 3600),
                (int) ((totalSeconds % 3600) / 60),
                (int) (totalSeconds % 60));
    }

    @Transactional(readOnly = true)
    public RankingDTO getUserRanking(Long userId) {
        UserRankAndTime rankAndTime = getUserRankAndTime(userId);
        return convertToRankingDTO(userId, rankAndTime.rank(), rankAndTime.totalSeconds());
    }

    @Transactional(readOnly = true)
    public MyProfileResponseDTO getUserProfile(Long userId) {
        User user = getUser(userId);
        UserRankAndTime rankAndTime = getUserRankAndTime(userId);

        return MyProfileResponseDTO.builder()
                .nickName(user.getNickName())
                .portfolioUrl(user.getPortfolioUrl())
                .rank(rankAndTime.rank())
                .hour((int) (rankAndTime.totalSeconds() / 3600))
                .min((int) ((rankAndTime.totalSeconds() % 3600) / 60))
                .sec((int) (rankAndTime.totalSeconds() % 60))
                .build();
    }

    private UserRankAndTime getUserRankAndTime(Long userId) {
        Long rankIndex = redisTemplate.opsForZSet().reverseRank(RANKING_KEY, userId.toString());
        Double todayAddedTime = redisTemplate.opsForZSet().score(RANKING_KEY, userId.toString());

        if (rankIndex == null || todayAddedTime == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND_EXCEPTION);
        }

        return new UserRankAndTime(rankIndex.intValue() + 1, todayAddedTime.longValue());
    }

    private record UserRankAndTime(int rank, long totalSeconds) {}
    public void isNicknameAvailable(String nickname) {
        boolean exists = userRepository.existsByNickName(nickname);
        if (exists) {
            throw new ConflictException(ErrorCode.ALREADY_EXIST_NICKNAME_EXCEPTION);
        }
    }

}
