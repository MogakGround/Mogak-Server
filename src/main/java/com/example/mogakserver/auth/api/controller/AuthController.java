package com.example.mogakserver.auth.api.controller;

import com.example.mogakserver.common.exception.dto.SuccessResponse;
import com.example.mogakserver.common.config.resolver.kakao.KakaoCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.mogakserver.auth.application.response.LoginResponseDto;
import com.example.mogakserver.auth.application.service.AuthService;

import static com.example.mogakserver.common.exception.enums.SuccessCode.SOCIAL_LOGIN_SUCCESS;

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
        return SuccessResponse.success(SOCIAL_LOGIN_SUCCESS, authService.login(originHeader, kakaoCode));
    }
}

