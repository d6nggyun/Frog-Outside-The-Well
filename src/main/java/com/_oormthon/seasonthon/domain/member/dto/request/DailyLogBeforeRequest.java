package com._oormthon.seasonthon.domain.member.dto.request;

import com._oormthon.seasonthon.domain.member.enums.PlaceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DailyLogBefore 생성 요청")
public record DailyLogBeforeRequest(

                @Schema(description = "감정 (1~5)", example = "4", minimum = "1", maximum = "5") @Min(1) @Max(5) int emotion,

                @Schema(description = "에너지 (1~5)", example = "3", minimum = "1", maximum = "5") @Min(1) @Max(5) int energy,

                @Schema(description = "장소", example = "HOME", allowableValues = {
                                "HOME", "WORK", "CAFE", "LIBRARY", "CLASSROOM", "OTHER" }) @NotNull PlaceType place

        ){
}
