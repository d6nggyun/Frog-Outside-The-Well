package com._oormthon.seasonthon.domain.todo.dto.req;

import com._oormthon.seasonthon.domain.todo.enums.Day;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "ToDo 생성 요청")
public record TodoRequest(

        @Schema(description = "업무명", example = "우물밖개구리 프로젝트")
        @NotBlank(message = "업무명이 비어있습니다.")
        String title,

        @Schema(description = "업무 내용", example = "TODO API 개발하기, 회원 API 개발하기")
        @NotBlank(message = "업무 내용이 비어있습니다.")
        String content,

        @Schema(description = "업무 수행 시작일", example = "2025-09-02")
        @NotNull(message = "업무 수행 시작일이 비어있습니다.")
        LocalDate startDate,

        @Schema(description = "업무 수행 마감일", example = "2025-09-03")
        @NotNull(message = "업무 수행 마감일이 비어있습니다.")
        LocalDate endDate,

        @Schema(description = "업무 수행 예정일", example = "[\"MONDAY\", \"TUESDAY\"]")
        @NotEmpty(message = "업무 수행 예정일이 비어있습니다.")
        List<Day> expectedDays

) {
}
