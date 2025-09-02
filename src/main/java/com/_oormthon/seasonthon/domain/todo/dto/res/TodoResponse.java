package com._oormthon.seasonthon.domain.todo.dto.res;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record TodoResponse(

        @Schema(description = "현재 날짜")
        LocalDate currentDate,

        @Schema(description = "Todo Id")
        Long id,

        @Schema(description = "회원 Id")
        Long userId,

        @Schema(description = "D-Day")
        String dDay,

        @Schema(description = "제목")
        String title,

        @Schema(description = "따뜻한 한마디")
        String warmMessage,

        @Schema(description = "진행률")
        Long progress,

        @Schema(description = "Step 리스트")
        List<StepResponse> stepResponses

) {
    public static TodoResponse from(Todo todo, String warmMessage, List<StepResponse> stepResponses) {
        int dDayValue = (int) ChronoUnit.DAYS.between(LocalDate.now(), todo.getEndDate());

        return new TodoResponse(LocalDate.now(), todo.getId(), todo.getUserId(),
                dDayValue > 0 ? "D-" + dDayValue : dDayValue == 0 ? "D-DAY" : "D+" + Math.abs(dDayValue),
                todo.getTitle(), warmMessage, todo.getProgress(), stepResponses);
    }
}
