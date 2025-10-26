package com._oormthon.seasonthon.domain.dailyLog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Mood {
    HAPPY("즐거움"),
    EXCITED("설렘"),
    CALM("평온"),
    NORMAL("그저그래"),
    THRILLING("짜릿"),
    FRUSTRATED("답답"),
    DEPRESSED("우울"),
    EMPTY("허무"),
    ANGRY("화"),
    DISAPPOINTED("실망");

    private final String mood;
}