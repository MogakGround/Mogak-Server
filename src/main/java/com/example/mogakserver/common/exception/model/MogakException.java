package com.example.mogakserver.common.exception.model;

import com.example.mogakserver.common.exception.enums.ErrorCode;
import lombok.Getter;

@Getter
public class MogakException extends RuntimeException {
    private final ErrorCode errorCode;

    public MogakException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

