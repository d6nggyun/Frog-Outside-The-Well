package com._oormthon.seasonthon.domain.dailyLog.dto.response;

import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogBefore;
import com._oormthon.seasonthon.domain.dailyLog.enums.PlaceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DailyLogBefore 응답")
public record DailyLogBeforeResponse(

        @Schema(description = "DailyLogBefore Id", example = "1") Long id,

        @Schema(description = "감정 (1~5)", example = "3") int emotion,

        @Schema(description = "사용자 ID", example = "1") Long userId,

        @Schema(description = "에너지 (1~5)", example = "4") int energy,

        @Schema(description = "장소", example = "HOME") PlaceType place,

        @Schema(description = "작성 날짜", example = "2025-09-07") LocalDate createdAt

) {
    // ===== Entity → DTO 변환 메서드 =====
    public static DailyLogBeforeResponse fromEntity(DailyLogBefore entity) {
        return new DailyLogBeforeResponse(
                entity.getId(),
                entity.getEmotion(),
                entity.getUserId(),
                entity.getEnergy(),
                entity.getPlace(),
                entity.getCreatedAt());
    }
}
