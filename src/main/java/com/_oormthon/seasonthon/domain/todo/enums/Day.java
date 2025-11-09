package com._oormthon.seasonthon.domain.todo.enums;

public enum Day {
    MONDAY("월요일"),
    TUESDAY("화요일"),
    WEDNESDAY("수요일"),
    THURSDAY("목요일"),
    FRIDAY("금요일"),
    SATURDAY("토요일"),
    SUNDAY("일요일");

    private final String koreanName;

    Day(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    // ✅ 예: step.day()로 얻은 enum을 한글로 바꾸기
    public static String toKorean(Day day) {
        if (day == null) {
            return "";
        }
        return day.getKoreanName();
    }

}
