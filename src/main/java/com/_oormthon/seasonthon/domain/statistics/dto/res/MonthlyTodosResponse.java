package com._oormthon.seasonthon.domain.statistics.dto.res;

import java.time.LocalDate;

public record MonthlyTodosResponse(

        String title,

        LocalDate startDate,

        LocalDate endDate,

        Long totalDuration

) {
    public static MonthlyTodosResponse of(String title, LocalDate startDate, LocalDate endDate, Long totalDuration) {
        return new MonthlyTodosResponse(title, startDate, endDate, totalDuration);
    }
}
