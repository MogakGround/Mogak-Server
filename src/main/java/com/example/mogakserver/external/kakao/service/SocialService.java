package com.example.mogakserver.external.kakao.service;

public abstract class SocialService {
    public abstract Long getIdFromKakao(String baseUrl, String kakaoCode);

}
