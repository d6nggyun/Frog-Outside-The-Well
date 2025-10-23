package com._oormthon.seasonthon.domain.StepCalendar.dto.res;

import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record TodoAndStepResponse(

        @Schema(description = "Todo Id", example = "0")
        Long todoId,

        @Schema(description = "Todo 제목", example = "0")
        String todoTitle,

        @Schema(description = "Step Id", example = "0")
        Long stepId,

        @Schema(description = "날짜", example = "2025-09-02")
        LocalDate stepDate,

        @Schema(description = "내용", example = "ToDo ERD 설계")
        String description,

        @Schema(description = "완료 여부", example = "false")
        Boolean isCompleted

) {
    public static TodoAndStepResponse of(Todo todo, TodoStep todoStep) {
        return new TodoAndStepResponse(todo.getId(), todo.getTitle(),
                todoStep.getId(), todoStep.getStepDate(), todoStep.getDescription(), todoStep.getIsCompleted());
    }
}
