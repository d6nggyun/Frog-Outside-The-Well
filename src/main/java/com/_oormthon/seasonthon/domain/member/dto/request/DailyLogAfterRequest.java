package com._oormthon.seasonthon.domain.member.dto.request;

import com._oormthon.seasonthon.domain.member.enums.Mood;
import com._oormthon.seasonthon.domain.member.enums.CompletionLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DailyLogAfter 생성 요청")
public record DailyLogAfterRequest(

                @Schema(description = "기분", example = "HAPPY", allowableValues = {
                                "HAPPY", "EXCITED", "CALM", "NORMAL", "THRILLING", "FRUSTRATED", "DEPRESSED", "EMPTY",
                                "ANGRY", "DISAPPOINTED" }) @NotNull Mood mood,

                @Schema(description = "사용자 ID", example = "1") @NotNull Long userId,

                @Schema(description = "집중도 (1~5)", example = "3", minimum = "1", maximum = "5") @Min(1) @Max(5) @NotNull int focusLevel,

                @Schema(description = "완성도", example = "FIFTY", allowableValues = { "ZERO", "TWENTY_FIVE", "FIFTY",
                                "SEVENTY_FIVE", "ONE_HUNDRED" }) @NotNull CompletionLevel completionLevel,

                @Schema(description = "메모", example = "오늘 공부 집중 잘했음") String memo,

                @Schema(description = "사진 URL", example = "http://example.com/photo.jpg") String photoUrl

        ){
}
