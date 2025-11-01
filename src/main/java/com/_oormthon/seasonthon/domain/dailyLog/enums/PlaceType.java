package com._oormthon.seasonthon.domain.dailyLog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlaceType {
    HOME("집"),
    WORK("TV Show"),
    CAFE("카페"),
    LIBRARY("도서관"),
    CLASSROOM("강의실"),
    OTHER("기타장소");

    private final String name;
}