package com._oormthon.seasonthon.domain.ai.service;

import java.util.*;
import java.util.stream.Collectors;
import com._oormthon.seasonthon.domain.todo.enums.Day;

public class DayConverter {

    private static final Map<String, Day> KOREAN_DAY_MAP = Map.ofEntries(
            Map.entry("월", Day.MONDAY),
            Map.entry("월요일", Day.MONDAY),
            Map.entry("화", Day.TUESDAY),
            Map.entry("화요일", Day.TUESDAY),
            Map.entry("수", Day.WEDNESDAY),
            Map.entry("수요일", Day.WEDNESDAY),
            Map.entry("목", Day.THURSDAY),
            Map.entry("목요일", Day.THURSDAY),
            Map.entry("금", Day.FRIDAY),
            Map.entry("금요일", Day.FRIDAY),
            Map.entry("토", Day.SATURDAY),
            Map.entry("토요일", Day.SATURDAY),
            Map.entry("일", Day.SUNDAY),
            Map.entry("일요일", Day.SUNDAY));

    /**
     * 입력 문자열을 요일 리스트로 변환
     * 예: "월,수,금" 또는 "월요일, 수요일, 금요일" → [MONDAY, WEDNESDAY, FRIDAY]
     * 잘못된 요일이 포함된 경우 IllegalArgumentException 발생
     */
    public static List<Day> parseDays(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("요일을 입력해주세요. (예: 월,수,금 또는 월요일,수요일,금요일)");
        }

        List<String> tokens = Arrays.stream(input.split("[,\\s]+")) // 쉼표나 공백으로 구분
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // ❗ 유효하지 않은 요일 검출
        List<String> invalidDays = tokens.stream()
                .filter(day -> !KOREAN_DAY_MAP.containsKey(day))
                .collect(Collectors.toList());

        if (!invalidDays.isEmpty()) {
            throw new IllegalArgumentException("잘못된 요일 입력: " + String.join(", ", invalidDays));
        }

        return tokens.stream()
                .map(KOREAN_DAY_MAP::get)
                .collect(Collectors.toList());
    }
}
