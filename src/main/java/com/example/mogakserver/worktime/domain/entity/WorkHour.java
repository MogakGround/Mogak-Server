package com.example.mogakserver.worktime.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WorkHour {
    LATE_NIGHT("야간 오후 10시 ~ 오전 5시"), MORNING("오전 시간대 아침 6시 ~ 정오"), AFTERNOON("오후 시간대 정오 ~ 오후 6시"), NIGHT("저녁 시간대 오후 6시 ~ 오후 10시");

    private final String value;
}
