package com._oormthon.seasonthon.domain.todo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TodoType {

    PREVIEW_REVIEW("예습/복습"),
    HOMEWORK("숙제"),
    TEST_STUDY("시험공부"),
    PERFORMANCE_ASSESSMENT("수행평가"),
    CAREER_ACTIVITY("진로활동"),
    ETC("기타");

    private final String type;
}
