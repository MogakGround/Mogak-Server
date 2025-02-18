package com.example.mogakserver.roomimg.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoomImgType {
    NULL_NULL("https://mogak-bucket.s3.ap-northeast-2.amazonaws.com/null_null.png"),
    NO_EO("https://mogak-bucket.s3.ap-northeast-2.amazonaws.com/no_eo.png"),
    SHIT_GRASS("https://mogak-bucket.s3.ap-northeast-2.amazonaws.com/shit_grass.png"),
    WHY_PIG("https://mogak-bucket.s3.ap-northeast-2.amazonaws.com/why_pig.png");

    private final String roomImgUrl;
}