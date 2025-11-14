package com._oormthon.seasonthon.domain.ai.service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import com._oormthon.seasonthon.domain.todo.enums.Day;

public class DayConverter {

    private static final Map<String, Day> KOREAN_DAY_MAP = Map.ofEntries(
            Map.entry("월", Day.MONDAY),
            Map.entry("화", Day.TUESDAY),
            Map.entry("수", Day.WEDNESDAY),
            Map.entry("목", Day.THURSDAY),
            Map.entry("금", Day.FRIDAY),
            Map.entry("토", Day.SATURDAY),
            Map.entry("일", Day.SUNDAY));

    private static final Map<Day, String> DAY_TO_KOREAN = Map.ofEntries(
            Map.entry(Day.MONDAY, "월"),
            Map.entry(Day.TUESDAY, "화"),
            Map.entry(Day.WEDNESDAY, "수"),
            Map.entry(Day.THURSDAY, "목"),
            Map.entry(Day.FRIDAY, "금"),
            Map.entry(Day.SATURDAY, "토"),
            Map.entry(Day.SUNDAY, "일"));

    /**
     * 입력 문자열을 요일 리스트로 변환
     * 예: "월,수,금" 또는 "월요일, 수요일, 금요일" → [MONDAY, WEDNESDAY, FRIDAY]
     * 잘못된 요일이 포함된 경우 IllegalArgumentException 발생
     */
    public static List<Day> parseDays(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("요일을 입력해주세요. (예: 월,수,금 또는 월요일,수요일,금요일)");
        }

        // 쉼표(,)나 공백 기준으로 분리하고, "요일" 접미사는 제거
        List<String> tokens = Arrays.stream(input.split("[,\\s]+"))
                .map(String::trim)
                .map(s -> s.replace("요일", "")) // 핵심 처리: '요일' 제거
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // 유효하지 않은 요일 검출
        List<String> invalidDays = tokens.stream()
                .filter(day -> !KOREAN_DAY_MAP.containsKey(day))
                .collect(Collectors.toList());

        if (!invalidDays.isEmpty()) {
            throw new IllegalArgumentException("잘못된 요일 입력: " + String.join(", ", invalidDays));
        }

        // 요일 Enum 리스트 반환
        return tokens.stream()
                .map(KOREAN_DAY_MAP::get)
                .collect(Collectors.toList());
    }

    public static List<Day> daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null)
            return List.of();
        if (end.isBefore(start))
            return List.of();

        Set<Day> result = new LinkedHashSet<>();
        LocalDate cur = start;

        while (!cur.isAfter(end)) {
            DayOfWeek dow = cur.getDayOfWeek();
            result.add(convertFromJavaDay(dow));
            cur = cur.plusDays(1);
        }
        return new ArrayList<>(result);
    }

    /** Java DayOfWeek → 프로젝트 Day Enum 매핑 */
    private static Day convertFromJavaDay(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> Day.MONDAY;
            case TUESDAY -> Day.TUESDAY;
            case WEDNESDAY -> Day.WEDNESDAY;
            case THURSDAY -> Day.THURSDAY;
            case FRIDAY -> Day.FRIDAY;
            case SATURDAY -> Day.SATURDAY;
            case SUNDAY -> Day.SUNDAY;
        };
    }

    /**
     * ✔ Day 리스트를 "월,수,금" 문자열로 변환
     */
    public static String formatDays(List<Day> days) {
        if (days == null || days.isEmpty())
            return "";

        return days.stream()
                .map(DAY_TO_KOREAN::get)
                .collect(Collectors.joining(","));
    }
}
