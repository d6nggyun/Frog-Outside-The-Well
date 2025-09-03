package com._oormthon.seasonthon.domain.step.dto.res;

import com._oormthon.seasonthon.domain.step.domain.StepRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record StepRecordResponse(

        @Schema(description = "Step Id", example = "1")
        Long stepId,

        @Schema(description = "User Id", example = "1")
        Long userId,

        @Schema(description = "시작 시간", example = "2025-09-03T10:00:00")
        LocalDateTime startTime,

        @Schema(description = "종료 시간", example = "2025-09-03T11:00:00")
        LocalDateTime endTime,

        @Schema(description = "총 수행 시간(단위: 초)", example = "3600")
        Long duration

) {
        public static StepRecordResponse from(StepRecord stepRecord) {
                return new StepRecordResponse(stepRecord.getStepId(), stepRecord.getUserId(),
                        stepRecord.getStartTime(), stepRecord.getEndTime(), stepRecord.getDuration());
        }
}
