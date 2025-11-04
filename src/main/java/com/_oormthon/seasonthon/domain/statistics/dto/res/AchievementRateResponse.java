package com._oormthon.seasonthon.domain.statistics.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record AchievementRateResponse(

        @Schema(description = "주차", example = "1")
        Integer weekOfMonth,

        @Schema(description = "평균 달성률", example = "75.5")
        Double rate,

        @Schema(description = "주차 시작일", example = "2025-10-15")
        LocalDate startDate,

        @Schema(description = "주차 종료일", example = "2025-10-28")
        LocalDate endDate

) {
    public static AchievementRateResponse of(Integer weekOfMonth, Double rate, LocalDate startDate, LocalDate endDate) {
        return new AchievementRateResponse(weekOfMonth, rate, startDate, endDate);
    }
}
