package com._oormthon.seasonthon.domain.todo.dto.req;

import com._oormthon.seasonthon.domain.todo.enums.TodoType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "ToDo 수정 요청")
public record UpdateTodoDetailRequest(

                @Schema(description = "업무명", example = "우물밖개구리 프로젝트") @NotBlank(message = "업무명이 비어있습니다.") String title,

                @Schema(description = "ToDo Type", example = "PREVIEW_REVIEW") @NotNull(message = "ToDo Type이 비어있습니다.") TodoType todoType) {
}
