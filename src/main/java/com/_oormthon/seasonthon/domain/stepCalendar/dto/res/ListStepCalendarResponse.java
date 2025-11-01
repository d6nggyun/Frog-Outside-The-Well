package com._oormthon.seasonthon.domain.stepCalendar.dto.res;

import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ListStepCalendarResponse(

        @ArraySchema(schema = @Schema(implementation = StepCalendarResponse.class))
        List<StepCalendarResponse> calendar,

        @ArraySchema(schema = @Schema(implementation = StepResponse.class))
        List<TodoAndStepResponse> todayToDo

) {
    public static ListStepCalendarResponse from(List<StepCalendarResponse> calendar, List<TodoAndStepResponse> todayToDo) {
        return new ListStepCalendarResponse(calendar, todayToDo);
    }
}
