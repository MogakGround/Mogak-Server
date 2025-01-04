package com.example.mogakserver.common.exception.model;

import com.example.mogakserver.common.exception.enums.ErrorCode;

public class ConflictException extends MogakException{
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}
