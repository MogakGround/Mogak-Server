package com.example.mogakserver.common.exception.model;

import com.example.mogakserver.common.exception.enums.ErrorCode;

public class UnAuthorizedException extends MogakException{
    public UnAuthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
