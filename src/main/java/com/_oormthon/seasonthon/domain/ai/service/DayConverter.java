package com._oormthon.seasonthon.domain.ai.service;

import java.util.*;
import java.util.stream.Collectors;

import com._oormthon.seasonthon.domain.todo.enums.Day;

public class DayConverter {

    private static final Map<String, Day> KOREAN_DAY_MAP = Map.of(
            "월", Day.MONDAY,
            "화", Day.TUESDAY,
            "수", Day.WEDNESDAY,
            "목", Day.THURSDAY,
            "금", Day.FRIDAY,
            "토", Day.SATURDAY,
            "일", Day.SUNDAY);

    public static List<Day> parseDays(String input) {
        if (input == null || input.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .map(KOREAN_DAY_MAP::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
