package com._oormthon.seasonthon.domain.stepCalendar.dto.res;

import com._oormthon.seasonthon.domain.stepCalendar.domain.StepCalendar;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "캘린더 응답")
public record StepCalendarResponse(

        @Schema(description = "캘린더 ID", example = "1")
        Long stepCalendarId,

        @Schema(description = "날짜", example = "2025-09-05")
        LocalDate calendarDate,

        @Schema(description = "해당 날짜의 업무 완료 퍼센트", example = "76")
        Integer percentage,

        @Schema(description = "해당 날짜의 업무 리스트")
        List<TodoAndStepResponse> stepResponses

) {
    public static StepCalendarResponse from(StepCalendar stepCalendar, List<TodoAndStepResponse> stepResponses) {
        return new StepCalendarResponse(stepCalendar.getId(), stepCalendar.getCalendarDate(), stepCalendar.getPercentage(), stepResponses);
    }
}
