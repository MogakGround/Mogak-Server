package com.example.mogakserver.common.exception.model;

import com.example.mogakserver.common.exception.dto.TokenPair;
import com.example.mogakserver.common.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class NotFoundUserException extends MogakException {
    private final TokenPair tokenPair;

    public NotFoundUserException(ErrorCode errorCode, TokenPair tokenPair) {
        super(errorCode);
        this.tokenPair = tokenPair;
    }
}
