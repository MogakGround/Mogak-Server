package com.example.mogakserver.auth.api.controller;

import com.example.mogakserver.auth.api.request.SignUpRequestDto;
import com.example.mogakserver.auth.api.request.TokenRequestDto;
import com.example.mogakserver.auth.application.response.LoginResponseDto;
import com.example.mogakserver.auth.application.service.AuthService;
import com.example.mogakserver.common.util.resolver.kakao.KakaoCode;
import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.exception.dto.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.mogakserver.common.exception.enums.SuccessCode.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String ORIGIN = "origin";
    private final AuthService authService;

    @PostMapping("/login")
    public SuccessResponse<LoginResponseDto> login(
            @KakaoCode String kakaoCode,
            HttpServletRequest request) {
        String originHeader = request.getHeader(ORIGIN);
        LoginResponseDto responseDto = authService.login(originHeader, kakaoCode);

        if (responseDto.isNewUser()) {
            return SuccessResponse.success(SIGNUP_SUCCESS, responseDto);
        }
        return SuccessResponse.success(SOCIAL_LOGIN_SUCCESS, responseDto);
    }

    @PostMapping("/signup")
    public SuccessResponse<LoginResponseDto> signUp(
            @RequestBody SignUpRequestDto signUpRequest) {
        return SuccessResponse.success(SOCIAL_LOGIN_SUCCESS, authService.signUp(signUpRequest));
    }

    @PostMapping("/refresh")
    public SuccessResponse<TokenPair> refresh(@RequestBody final TokenRequestDto tokenRequestDto) {
        return SuccessResponse.success(REFRESH_SUCCESS, authService.refresh(tokenRequestDto));
    }
}

