package com._oormthon.seasonthon.domain.member.dto.request;

import com._oormthon.seasonthon.domain.member.enums.PlaceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DailyLogBefore 생성 요청")
public record DailyLogBeforeRequest(

        @Schema(description = "감정 (1~5)", example = "3") @Min(value = 1, message = "emotion 최소값은 1입니다.") @Max(value = 5, message = "emotion 최대값은 5입니다.") int emotion,

        @Schema(description = "사용자 ID", example = "1") @NotNull(message = "userId가 비어있습니다.") Long userId,

        @Schema(description = "에너지 (1~5)", example = "4") @Min(value = 1, message = "energy 최소값은 1입니다.") @Max(value = 5, message = "energy 최대값은 5입니다.") int energy,

        @Schema(description = "장소", example = "HOME") @NotNull(message = "place가 비어있습니다.") PlaceType place

) {
}
