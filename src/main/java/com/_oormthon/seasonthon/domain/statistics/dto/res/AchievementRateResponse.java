package com._oormthon.seasonthon.domain.statistics.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record AchievementRateResponse(

        @Schema(description = "주차", example = "1")
        Integer weekOfMonth,

        @Schema(description = "주차 시작일", example = "2025-10-15")
        LocalDate startDate,

        @Schema(description = "주차 종료일", example = "2025-10-28")
        LocalDate endDate,

        @Schema(description = "평균 달성률", example = "75.5")
        Double rate

) {
    public static AchievementRateResponse of(Integer weekOfMonth, LocalDate startDate, LocalDate endDate, Double rate) {
        return new AchievementRateResponse(weekOfMonth, startDate, endDate, rate);
    }
}
