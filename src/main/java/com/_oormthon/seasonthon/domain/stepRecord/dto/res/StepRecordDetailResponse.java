package com._oormthon.seasonthon.domain.stepRecord.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Step 기록 상세 응답")
public record StepRecordDetailResponse(

        @Schema(description = "Step 기록 Id", example = "1")
        Long stepRecordId,

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
}
