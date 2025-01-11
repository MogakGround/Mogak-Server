package com.example.mogakserver.common.exception.model;

import com.example.mogakserver.common.exception.dto.TokenPair;
import com.example.mogakserver.common.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class NotFoundUserException extends MogakException {
    private final TokenPair tokenPair;
    private final Long kakaoId;

    public NotFoundUserException(ErrorCode errorCode, TokenPair tokenPair, Long kakaoId) {
        super(errorCode);
        this.tokenPair = tokenPair;
        this.kakaoId = kakaoId;
    }
}
