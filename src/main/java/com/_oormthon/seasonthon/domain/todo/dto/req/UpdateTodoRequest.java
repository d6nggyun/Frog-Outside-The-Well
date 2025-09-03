package com._oormthon.seasonthon.domain.todo.dto.req;

import com._oormthon.seasonthon.domain.step.dto.req.StepRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateTodoRequest(

        @Schema(description = "업무명", example = "우물밖개구리 프로젝트")
        @NotBlank(message = "업무명이 비어있습니다.")
        String title,

        @Schema(description = "업무 내용", example = "TODO API 개발하기, 회원 API 개발하기")
        @NotBlank(message = "업무 내용이 비어있습니다.")
        String content,

        @Schema(description = "추가 일 수", example = "7")
        @NotNull(message = "추가 일 수가 비어있습니다.")
        Integer addDays,

        @Schema(description = "ToDo Step 리스트")
        @NotEmpty(message = "ToDo Step 리스트가 비어있습니다.")
        List<StepRequest> todoSteps

) {
}
