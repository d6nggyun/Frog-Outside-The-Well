package com._oormthon.seasonthon.domain.StepCalendar.dto.res;

import com._oormthon.seasonthon.domain.StepCalendar.domain.StepCalendar;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "캘린더 응답")
public record StepCalendarResponse(

        @Schema(description = "날짜", example = "2025-09-05")
        LocalDate calendarDate,

        @Schema(description = "Todo 실행 횟수", example = "15")
        Integer count

) {
    public static StepCalendarResponse from(StepCalendar stepCalendar) {
        return new StepCalendarResponse(stepCalendar.getCalendarDate(), stepCalendar.getCount());
    }
}
