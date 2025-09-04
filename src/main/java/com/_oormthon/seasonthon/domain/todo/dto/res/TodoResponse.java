package com._oormthon.seasonthon.domain.todo.dto.res;

import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Schema(description = "ToDo 정보 응답")
public record TodoResponse(

        @Schema(description = "현재 날짜", example = "2024-09-03")
        LocalDate currentDate,

        @Schema(description = "Todo Id", example = "1")
        Long id,

        @Schema(description = "회원 Id", example = "1")
        Long userId,

        @Schema(description = "D-Day", example = "D-10")
        String dDay,

        @Schema(description = "제목", example = "우물밖개구리 프로젝트")
        String title,

        @Schema(description = "따뜻한 한마디", example = "화이팅!")
        String warmMessage,

        @Schema(description = "진행률", example = "50")
        Integer progress,

        @Schema(description = "완료 여부", example = "false")
        Boolean isCompleted,

        @Schema(description = "Step 리스트")
        List<StepResponse> stepResponses

) {
    public static TodoResponse from(Todo todo, String warmMessage, List<StepResponse> stepResponses) {
        int dDayValue = (int) ChronoUnit.DAYS.between(LocalDate.now(), todo.getEndDate());

        return new TodoResponse(LocalDate.now(), todo.getId(), todo.getUserId(),
                dDayValue > 0 ? "D-" + dDayValue : dDayValue == 0 ? "D-DAY" : "D+" + Math.abs(dDayValue),
                todo.getTitle(), warmMessage, todo.getProgress(), todo.getIsCompleted() , stepResponses);
    }
}
