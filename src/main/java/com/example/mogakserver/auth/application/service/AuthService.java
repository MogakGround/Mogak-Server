package com.example.mogakserver.auth.application.service;

import com.example.mogakserver.auth.api.request.SignUpRequestDto;
import com.example.mogakserver.auth.api.request.TokenRequestDto;
import com.example.mogakserver.auth.application.response.LoginResponseDto;
import com.example.mogakserver.common.exception.dto.TokenPair;
import com.example.mogakserver.common.config.jwt.JwtService;
import com.example.mogakserver.common.exception.model.NotFoundException;
import com.example.mogakserver.common.exception.model.UnAuthorizedException;
import com.example.mogakserver.external.kakao.service.KakaoSocialService;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.infra.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.mogakserver.common.exception.enums.ErrorCode.TOKEN_TIME_EXPIRED_EXCEPTION;
import static com.example.mogakserver.common.exception.enums.ErrorCode.USER_NOT_FOUND_EXCEPTION;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final JwtService jwtService;
    private final KakaoSocialService kakaoSocialService;
    private final JpaUserRepository jpaUserRepository;

    public LoginResponseDto login(final String baseUrl, final String kakaoCode) {
        Long kakaoId = kakaoSocialService.getIdFromKakao(baseUrl, kakaoCode);
        Optional<User> user = jpaUserRepository.findByKakaoId(kakaoId);

        if (user.isEmpty()) {
            TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(kakaoId));
            return LoginResponseDto.NewUserResponse(kakaoId, tokenPair.accessToken(), tokenPair.refreshToken());
        }

        User existingUser = user.get();
        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(user.get().getId()));
        return LoginResponseDto.ExistingUserResponse(existingUser.getId(), tokenPair.accessToken(), tokenPair.refreshToken());
    }

    public LoginResponseDto signUp(SignUpRequestDto signUpRequest) {
        User newUser = User.builder()
                .kakaoId(signUpRequest.kakaoId())
                .nickName(signUpRequest.nickName())
                .portfolioUrl(signUpRequest.portfolioUrl())
                .build();
        jpaUserRepository.save(newUser);

        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(newUser.getId()));
        return LoginResponseDto.SignupResponse(newUser.getId(), tokenPair.accessToken(), tokenPair.refreshToken());
    }

    public void logout(final Long userId) {
        jpaUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_EXCEPTION));
        jwtService.deleteRefreshToken(String.valueOf(userId));
    }

    public TokenPair refresh(final TokenRequestDto tokenRequestDto) {
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
        jpaUserRepository.delete(user);
    }
}
