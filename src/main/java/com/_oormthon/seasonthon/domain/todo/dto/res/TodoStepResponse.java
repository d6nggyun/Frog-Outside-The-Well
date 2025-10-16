package com._oormthon.seasonthon.domain.todo.dto.res;

import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Schema(description = "ToDo / Step 리스트 정보 응답")
public record TodoStepResponse(

        @Schema(description = "D-Day", example = "D-10")
        String dDay,

        @Schema(description = "제목", example = "우물밖개구리 프로젝트")
        String title,

        @Schema(description = "마감일", example = "2025-09-05")
        LocalDate endDate,

        @Schema(description = "진행률 문구", example = "개구리가 햇빛을 보기 시작했어요!")
        String progressText,

        @Schema(description = "진행률", example = "50")
        Integer progress,

        @Schema(description = "Step 리스트")
        List<StepResponse> steps

) {
    public static TodoStepResponse of(Todo todo, String progressText, List<StepResponse> stepResponses) {
        int dDayValue = (int) ChronoUnit.DAYS.between(LocalDate.now(), todo.getEndDate());

        return new TodoStepResponse(dDayValue > 0 ? "D-" + dDayValue : dDayValue == 0 ? "D-DAY" : "D+" + Math.abs(dDayValue),
                todo.getTitle(), todo.getEndDate(), progressText, todo.getProgress(), stepResponses);
    }
}
