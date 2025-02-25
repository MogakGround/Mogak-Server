package com.example.mogakserver.auth.application.service;

import com.example.mogakserver.auth.api.request.SignUpRequestDTO;
import com.example.mogakserver.auth.api.request.TokenRequestDTO;
import com.example.mogakserver.auth.application.response.LoginResponseDto;
import com.example.mogakserver.common.exception.dto.TokenPair;
import com.example.mogakserver.common.config.jwt.JwtService;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.common.exception.model.UnAuthorizedException;
import com.example.mogakserver.external.kakao.service.KakaoSocialService;
import com.example.mogakserver.room.application.service.RoomRegisterService;
import com.example.mogakserver.roomuser.application.service.RoomUserService;
import com.example.mogakserver.roomuser.infra.repository.JpaRoomUserRepository;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.mogakserver.common.exception.enums.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final JwtService jwtService;
    private final KakaoSocialService kakaoSocialService;
    private final JpaUserRepository jpaUserRepository;
    private final JpaRoomUserRepository roomUserRepository;
    private final RoomRegisterService roomRegisterService;
    private final RoomUserService roomUserService;

    public LoginResponseDto login(final String kakaoCode) {
        if (kakaoCode == null || kakaoCode.isEmpty()) {
            throw new UnAuthorizedException(EMPTY_KAKAO_CODE_EXCEPTION);
        }

        Long kakaoId;
        try {
            kakaoId = kakaoSocialService.getIdFromKakao(kakaoCode).getData();
        } catch (Exception e) {
            throw new NotFoundException(INVALID_KAKAO_CODE_EXCEPTION);
        }

        User user = jpaUserRepository.findByKakaoId(kakaoId).orElse(null);

        if (user == null) {
            jpaUserRepository.save(User.builder().kakaoId(kakaoId).build());
            return LoginResponseDto.NewUserResponse(kakaoId);
        }

        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(user.getId()));
        return LoginResponseDto.ExistingUserResponse(tokenPair.accessToken(), tokenPair.refreshToken());
    }

    public LoginResponseDto signUp(SignUpRequestDTO signUpRequest) {
        if (signUpRequest.kakaoId() == null) {
            throw new UnAuthorizedException(EMPTY_KAKAO_ID_EXCEPTION);
        }

        jpaUserRepository.findByKakaoId(signUpRequest.kakaoId()).ifPresent(user -> {
            if (user.getNickName() != null) {
                throw new UnAuthorizedException(ALREADY_EXIST_USER_EXCEPTION);
            }
        });

        User user = jpaUserRepository.findByKakaoId(signUpRequest.kakaoId())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_EXCEPTION));

        if (user.getNickName() != null) {
            throw new UnAuthorizedException(ALREADY_EXIST_USER_EXCEPTION);
        }

        user.updateProfile(signUpRequest.nickName(), signUpRequest.portfolioUrl());
        jpaUserRepository.save(user);

        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(user.getId()));
        return LoginResponseDto.SignupResponse(tokenPair.accessToken(), tokenPair.refreshToken());
    }

    public void logout(final Long userId) {
        jpaUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_EXCEPTION));
        jwtService.deleteRefreshToken(String.valueOf(userId));
    }

    public TokenPair refresh(final TokenRequestDTO tokenRequestDto) {
        if (!jwtService.verifyToken(tokenRequestDto.refreshToken()))
            throw new UnAuthorizedException(TOKEN_TIME_EXPIRED_EXCEPTION);

        final String userId = jwtService.getUserIdInToken(tokenRequestDto.refreshToken());
        final User user = jpaUserRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_EXCEPTION));

        if (!jwtService.compareRefreshToken(userId, tokenRequestDto.refreshToken()))
            throw new UnAuthorizedException(TOKEN_TIME_EXPIRED_EXCEPTION);

        final TokenPair tokenPair = jwtService.generateTokenPair(userId);
        jwtService.saveRefreshToken(userId, tokenPair.refreshToken());
        return tokenPair;
    }

    public void deleteUser(Long userId) {
        User user = jpaUserRepository.findById(userId).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_EXCEPTION));
        jwtService.deleteRefreshToken(String.valueOf(userId));

        deleteHostedRooms(userId);
        quitUserFromRooms(userId);

        jpaUserRepository.delete(user);
    }

    private void deleteHostedRooms(Long userId) {
        List<Long> hostRoomIds = roomUserRepository.findHostRoomIdsByUserId(userId);

        for (Long roomId : hostRoomIds) {
            roomRegisterService.deleteRoom(userId, roomId);
        }
    }

    private void quitUserFromRooms(Long userId) {
        List<Long> joinedRoomIds = roomUserRepository.findJoinedRoomIdsByUserId(userId);

        for (Long roomId : joinedRoomIds) {
            roomUserService.quitRoom(userId, roomId);
        }

        roomUserRepository.deleteByUserId(userId);
    }
}
