package com._oormthon.seasonthon.domain.member.dto.request;

import com._oormthon.seasonthon.domain.member.enums.Mood;
import com._oormthon.seasonthon.domain.member.enums.CompletionLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DailyLogAfter 생성 요청")
public record DailyLogAfterRequest(

        @Schema(description = "기분", example = "HAPPY") @NotNull(message = "Mood가 비어있습니다.") Mood mood,

        @Schema(description = "사용자 ID", example = "1") @NotNull(message = "userId가 비어있습니다.") Long userId,

        @Schema(description = "집중도 (1~5)", example = "4") @NotNull(message = "focusLevel이 비어있습니다.") int focusLevel,

        @Schema(description = "완성도", example = "COMPLETE") @NotNull(message = "completionLevel이 비어있습니다.") CompletionLevel completionLevel,

        @Schema(description = "메모", example = "오늘 공부 집중 잘했음") String memo,

        @Schema(description = "사진 URL", example = "http://example.com/photo.jpg") String photoUrl

) {
}
