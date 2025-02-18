package com.example.mogakserver.user.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDTO {
    private String nickName;
    private String portfolioUrl;
}
