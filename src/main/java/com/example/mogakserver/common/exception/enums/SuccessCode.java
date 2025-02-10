package com.example.mogakserver.common.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    SIGNUP_SUCCESS(HttpStatus.OK, "신규 회원 입니다."),
    SOCIAL_LOGIN_SUCCESS(HttpStatus.OK, "카카오 로그인 성공입니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공입니다."),
    REFRESH_SUCCESS(HttpStatus.OK, "토큰 갱신 성공입니다."),
    ROOM_CREATION_SUCCESS(HttpStatus.CREATED, "모각방 생성 성공입니다."),
    UPDATE_ROOM_SUCCESS(HttpStatus.OK, "모각방 정보 수정 성공입니다."),
    UPDATE_SCREEN_SHARE_LARGE_SUCCESS(HttpStatus.OK, "화면공유 확대 허용 여부 변경 성공입니다."),
    USER_DELETION_SUCCESS(HttpStatus.OK, "회원 탈퇴 성공입니다."),
    GET_SCREEN_SHARE_USERS_SUCCESS(HttpStatus.OK, "화면공유 사용자 리스트 반환 성공입니다."),
    GET_MY_STATUS_IN_ROOM_SUCCESS(HttpStatus.OK, "모각방 안에서 내 상태 조회 성공입니다"),
    GET_PAGED_TIMERS_SUCCESS(HttpStatus.OK, "타이머 리스트 반환 성공입니다."),
    GET_RANKING_SUCCESS(HttpStatus.OK, "랭킹 조회 성공입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

