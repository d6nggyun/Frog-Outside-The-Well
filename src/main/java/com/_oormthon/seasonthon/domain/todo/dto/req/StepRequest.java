package com._oormthon.seasonthon.domain.todo.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StepRequest(

        @Schema(description = "날짜", example = "2025-09-02")
        @NotNull(message = "날짜가 비어있습니다.")
        LocalDate stepDate,

        @Schema(description = "순서", example = "1")
        @NotNull(message = "순서가 비어있습니다.")
        Integer stepOrder,

        @Schema(description = "내용", example = "ToDo ERD 설계")
        @NotBlank(message = "내용이 비어있습니다.")
        String description

) {
}
