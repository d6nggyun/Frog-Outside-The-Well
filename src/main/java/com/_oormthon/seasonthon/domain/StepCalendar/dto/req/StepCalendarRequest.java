package com._oormthon.seasonthon.domain.StepCalendar.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "캘린더 년 / 월 요청")
public record StepCalendarRequest(

        @NotNull
        Integer year,

        @NotNull
        @Min(1)
        @Max(12)
        Integer month

) {
}
