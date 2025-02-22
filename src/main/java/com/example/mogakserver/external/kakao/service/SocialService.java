package com.example.mogakserver.external.kakao.service;

import com.example.mogakserver.common.exception.dto.SuccessResponse;

public abstract class SocialService {
    public abstract SuccessResponse<Long> getIdFromKakao(String kakaoCode);

}
