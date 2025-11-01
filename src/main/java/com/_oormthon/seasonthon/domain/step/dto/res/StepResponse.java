package com._oormthon.seasonthon.domain.step.dto.res;

import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Step 정보 응답")
public record StepResponse(

        @Schema(description = "Step Id", example = "0")
        Long stepId,

        @Schema(description = "날짜", example = "2025-09-02")
        LocalDate stepDate,

        @Schema(description = "내용", example = "ToDo ERD 설계")
        String description,

        @Schema(description = "완료 여부", example = "false")
        Boolean isCompleted

) {
    public static StepResponse of(TodoStep todoStep) {
        return new StepResponse(
                todoStep.getId(), todoStep.getStepDate(),
                todoStep.getDescription(), todoStep.getIsCompleted());
    }
}
