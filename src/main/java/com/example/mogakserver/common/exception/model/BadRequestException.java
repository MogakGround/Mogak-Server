package com.example.mogakserver.common.exception.model;

import com.example.mogakserver.common.exception.enums.ErrorCode;

public class BadRequestException extends MogakException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
