package com._oormthon.seasonthon.domain.stepRecord.dto.res;

import com._oormthon.seasonthon.domain.stepRecord.domain.StepRecord;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Step 기록 응답")
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
        Long duration,

        @Schema(description = "휴식 횟수", example = "1")
        Integer breakCount,

        @Schema(description = "Todo 진행률", example = "100")
        Integer progress,

        @Schema(description = "Step 완료 여부", example = "true")
        Boolean isCompleted,

        @Schema(description = "Today Step 완료 여부", example = "true")
        Boolean isCompletedTodaySteps

) {
        public static StepRecordResponse of(StepRecord stepRecord, Integer progress, Boolean isCompletedTodaySteps) {
                return new StepRecordResponse(stepRecord.getStepId(), stepRecord.getUserId(),
                        stepRecord.getStartTime(), stepRecord.getEndTime(), stepRecord.getDuration(),
                        stepRecord.getBreakCount(), progress, stepRecord.getIsCompleted(), isCompletedTodaySteps);
        }
}
