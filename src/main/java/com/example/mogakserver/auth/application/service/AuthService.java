package com.example.mogakserver.auth.application.service;

import com.example.mogakserver.auth.application.request.SignUpRequestDto;
import com.example.mogakserver.auth.application.response.LoginResponseDto;
import com.example.mogakserver.common.exception.dto.TokenPair;
import com.example.mogakserver.common.config.jwt.JwtService;
import com.example.mogakserver.external.kakao.service.KakaoSocialService;
import com.example.mogakserver.user.domain.entity.User;
import com.example.mogakserver.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.example.mogakserver.common.exception.enums.ErrorCode.USER_NOT_FOUND_EXCEPTION;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final KakaoSocialService kakaoSocialService;
    private final UserRepository userRepository;

    public LoginResponseDto login(final String baseUrl, final String kakaoCode) {
        Long kakaoId = kakaoSocialService.getIdFromKakao(baseUrl, kakaoCode);
        Optional<User> user = userRepository.findByKakaoId(kakaoId);

        if (user.isEmpty()) {
            TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(kakaoId));
            return new LoginResponseDto(
                    kakaoId,
                    tokenPair.accessToken(),
                    tokenPair.refreshToken(),
                    true
            );
        }

        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(user.get().getId()));
        return new LoginResponseDto(
                user.get().getId(),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                false
        );
    }

    public LoginResponseDto signUp(SignUpRequestDto signUpRequest) {
        User newUser = User.builder()
                .kakaoId(signUpRequest.kakaoId())
                .nickName(signUpRequest.nickName())
                .portfolioUrl(signUpRequest.portfolioUrl())
                .build();
        userRepository.save(newUser);

        TokenPair tokenPair = jwtService.generateTokenPair(String.valueOf(newUser.getId()));

        return new LoginResponseDto(
                newUser.getId(),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                true
        );
    }
}
