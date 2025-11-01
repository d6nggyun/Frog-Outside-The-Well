package com._oormthon.seasonthon.domain.dailyLog.dto.response;

import com._oormthon.seasonthon.domain.dailyLog.enums.Mood;
import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogAfter;
import com._oormthon.seasonthon.domain.dailyLog.enums.CompletionLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DailyLogAfter 응답")
public record DailyLogAfterResponse(

        @Schema(description = "DailyLogAfter Id", example = "1") Long id,

        @Schema(description = "기분", example = "HAPPY") Mood mood,

        @Schema(description = "사용자 ID", example = "1") Long userId,

        @Schema(description = "집중도 (1~5)", example = "4") int focusLevel,

        @Schema(description = "완성도", example = "COMPLETE") CompletionLevel completionLevel,

        @Schema(description = "메모", example = "오늘 공부 집중 잘했음") String memo,

        @Schema(description = "사진 URL", example = "http://example.com/photo.jpg") String photoUrl,

        @Schema(description = "작성 날짜", example = "2025-09-07") LocalDate createdAt

) {
    // ===== Entity → DTO 변환 메서드 =====
    public static DailyLogAfterResponse fromEntity(DailyLogAfter entity) {
        return new DailyLogAfterResponse(
                entity.getId(),
                entity.getMood(),
                entity.getUserId(),
                entity.getFocusLevel(),
                entity.getCompletionLevel(),
                entity.getMemo(),
                entity.getPhotoUrl(),
                entity.getCreatedAt());
    }
}
