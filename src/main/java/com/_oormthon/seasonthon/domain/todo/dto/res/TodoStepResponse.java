package com._oormthon.seasonthon.domain.todo.dto.res;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record TodoStepResponse(

        @Schema(description = "D-Day")
        String dDay,

        @Schema(description = "제목")
        String title,

        @Schema(description = "마감일")
        LocalDate endDate,

        @Schema(description = "진행률 문구")
        String progressText,

        @Schema(description = "진행률")
        Long progress,

        @Schema(description = "Step 리스트")
        List<StepResponse> steps

) {
    public static TodoStepResponse from(Todo todo, String progressText, List<StepResponse> stepResponses) {
        int dDayValue = (int) ChronoUnit.DAYS.between(LocalDate.now(), todo.getEndDate());

        return new TodoStepResponse(dDayValue > 0 ? "D-" + dDayValue : dDayValue == 0 ? "D-DAY" : "D+" + Math.abs(dDayValue),
                todo.getTitle(), todo.getEndDate(), progressText, todo.getProgress(), stepResponses);
    }
}
