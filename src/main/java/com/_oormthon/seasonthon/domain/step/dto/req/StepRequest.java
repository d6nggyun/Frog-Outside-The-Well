package com._oormthon.seasonthon.domain.step.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Step 생성 요청")
public record StepRequest(

        @Schema(description = "날짜", example = "2025-09-02") @NotNull(message = "날짜가 비어있습니다.") LocalDate stepDate,

        @Schema(description = "내용", example = "ToDo ERD 설계") @NotBlank(message = "내용이 비어있습니다.") String description,

        @Schema(description = "내용", example = "[\"하루 목표를 명확히 세우기\", \"쉬는 시간엔 스트레칭하기\"]") @NotBlank(message = "내용이 비어있습니다.") List<String> tips

) {
}
