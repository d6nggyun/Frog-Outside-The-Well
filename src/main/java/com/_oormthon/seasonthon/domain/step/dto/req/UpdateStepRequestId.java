package com._oormthon.seasonthon.domain.step.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Step 수정 요청")
public record UpdateStepRequestId(

        @Schema(description = "step Id", example = "ToDo ERD 설계") @NotNull(message = "내용이 비어있습니다.") Long stepId,

        @Schema(description = "내용", example = "ToDo ERD 설계") @NotBlank(message = "내용이 비어있습니다.") String description

) {
}
