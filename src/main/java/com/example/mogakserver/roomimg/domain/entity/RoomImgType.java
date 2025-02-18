package com.example.mogakserver.roomimg.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoomImgType {
    IMG1("https://example.com/image1.jpg"),
    IMG2("https://example.com/image2.jpg"),
    IMG3("https://example.com/image3.jpg"),
    IMG4("https://example.com/image4.jpg");

    private final String roomImgUrl;
}