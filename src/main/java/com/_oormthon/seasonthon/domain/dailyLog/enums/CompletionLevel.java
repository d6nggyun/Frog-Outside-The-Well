package com._oormthon.seasonthon.domain.dailyLog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompletionLevel {
    ZERO(0),
    TWENTY_FIVE(25),
    FIFTY(50),
    SEVENTY_FIVE(75),
    ONE_HUNDRED(100);

    private final int value;
}