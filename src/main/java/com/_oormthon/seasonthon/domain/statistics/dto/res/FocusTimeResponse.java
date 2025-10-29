package com._oormthon.seasonthon.domain.statistics.dto.res;

import com._oormthon.seasonthon.domain.statistics.util.TimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record FocusTimeResponse(

        @Schema(description = "주차", example = "1")
        Integer weekOfMonth,

        @Schema(description = "최소 집중 시간")
        Long minDuration,

        @Schema(description = "최대 집중 시간")
        Long maxDuration,

        @Schema(description = "최소 집중 시간 텍스트", example = "1시간 30분")
        String minDurationText,

        @Schema(description = "최대 집중 시간 텍스트", example = "2시간 30분")
        String maxDurationText,

        @Schema(description = "주차 시작일", example = "2025-10-15")
        LocalDate startDate,

        @Schema(description = "주차 종료일", example = "2025-10-28")
        LocalDate endDate

) {
    public static FocusTimeResponse of(Integer weekOfMonth, Long minDuration, Long maxDuration,
                                       LocalDate startDate, LocalDate endDate) {
        return new FocusTimeResponse(weekOfMonth, minDuration, maxDuration,
                TimeUtil.formatDuration(minDuration), TimeUtil.formatDuration(maxDuration), startDate, endDate);
    }
}
