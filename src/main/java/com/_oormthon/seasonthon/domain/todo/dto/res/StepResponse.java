package com._oormthon.seasonthon.domain.todo.dto.res;

import com._oormthon.seasonthon.domain.todo.domain.TodoStep;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record StepResponse(

        @Schema(description = "날짜")
        LocalDate stepDate,

        @Schema(description = "순서")
        Integer stepOrder,

        @Schema(description = "내용")
        String description,

        @Schema(description = "완료 여부")
        Boolean isCompleted

) {
    public static StepResponse from(TodoStep todoStep) {
        return new StepResponse(todoStep.getStepDate(), todoStep.getStepOrder(), todoStep.getDescription(), todoStep.getIsCompleted());
    }
}
