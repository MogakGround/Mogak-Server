package com.example.mogakserver.common.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    SOCIAL_LOGIN_SUCCESS(HttpStatus.OK, "카카오 로그인 성공입니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공입니다."),
    REFRESH_SUCCESS(HttpStatus.OK, "토큰 갱신 성공입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

