package com.example.mogakserver.common.exception.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400
    INVALID_TOKEN_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰을 입력했습니다."),
    INVALID_KAKAO_CODE_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 카카오 코드를 입력했습니다."),
    EXPIRE_VERIFICATION_CODE_EXCEPTION(HttpStatus.BAD_REQUEST, "만료된 인증 코드입니다."),
    INVALID_VALUE_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 타입 값을 입력했습니다."),
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청값이 유효하지 않습니다."),
    INVALID_ENUM_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "요청한 상수 값이 유효하지 않습니다."),
    INVALID_EMPTY_TYPE_EXCEPTION(HttpStatus.BAD_REQUEST, "해당 값은 null 또 상수 값이 유효하지 않습니다."),

    // 401
    EMPTY_KAKAO_CODE_EXCEPTION(HttpStatus.UNAUTHORIZED, "카카오 인증을 위해 유효한 코드를 제공해 주세요."),
    EMPTY_KAKAO_ID_EXCEPTION(HttpStatus.UNAUTHORIZED, "카카오 ID 값을 입력해 주세요."),
    TOKEN_NOT_CONTAINED_EXCEPTION(HttpStatus.UNAUTHORIZED, "Access Token이 필요합니다."),
    TOKEN_TIME_EXPIRED_EXCEPTION(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),

    //403
    ROOM_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "방 관련 권한이 없습니다."),
    ROOM_USER_NOT_MATCH(HttpStatus.FORBIDDEN, "유저와 방이 일치하지 않습니다"),

    //404
    USER_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 유저는 존재하지 않습니다."),
    KAKAO_ID_RETRIEVAL_FAILED_EXCEPTION(HttpStatus.NOT_FOUND, "카카오 ID 조회에 실패했습니다."),
    ROOM_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 방은 존재하지 않습니다."),
    NOT_FOUND_RESOURCE_EXCEPTION(HttpStatus.NOT_FOUND, "해당 자원을 찾을 수 없습니다."),
    NOT_FOUND_VERIFICATION_CODE_EXCEPTION(HttpStatus.NOT_FOUND, "인증 코드가 존재하지 않습니다."),
    NOT_FOUND_ROOM_EXCEPTION(HttpStatus.NOT_FOUND, "해당 모각방이 존재하지 않습니다."),

    // 405 METHOD_NOT_ALLOWED
    METHOD_NOT_ALLOWED_EXCEPTION(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 메소드 입니다."),

    // 409 Conflict
    ALREADY_EXIST_USER_EXCEPTION(HttpStatus.CONFLICT, "이미 존재하는 유저입니다."),
    ALREADY_EXIST_OFFER_EXCEPTION(HttpStatus.CONFLICT, "이미 존재하는 제안서입니다."),
    ALREADY_EXIST_NICKNAME_EXCEPTION(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    ALREADY_EXIST_ROOM_EXCEPTION(HttpStatus.CONFLICT, "이미 사용 중인 방 이름입니다."),

    // 500
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    KAKAO_SERVER_ERROR_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 서버와의 통신 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

