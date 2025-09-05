package com._oormthon.seasonthon.domain.StepCalendar.dto.res;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ListStepCalendarResponse(

        @ArraySchema(schema = @Schema(implementation = StepCalendarResponse.class))
        List<StepCalendarResponse> calendar,

        @ArraySchema(schema = @Schema(implementation = StepCalendarResponse.class))
        List<StepCalendarResponse> todayToDo

) {
    public static ListStepCalendarResponse from(List<StepCalendarResponse> calendar, List<StepCalendarResponse> todayToDo) {
        return new ListStepCalendarResponse(calendar, todayToDo);
    }
}
