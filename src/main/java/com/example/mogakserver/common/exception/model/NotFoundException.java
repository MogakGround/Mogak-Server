package com.example.mogakserver.common.exception.model;

import com.example.mogakserver.common.exception.enums.ErrorCode;

public class NotFoundException extends MogakException{
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
