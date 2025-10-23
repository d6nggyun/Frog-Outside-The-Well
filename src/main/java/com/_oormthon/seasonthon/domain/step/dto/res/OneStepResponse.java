package com._oormthon.seasonthon.domain.step.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record OneStepResponse(

        @Schema(description = "오늘의 한 걸음")
        List<StepResponse> todayStepResponses,

        @Schema(description = "놓친 한 걸음")
        List<StepResponse> missedStepResponses

) {
    public static OneStepResponse of(List<StepResponse> todayStepResponses, List<StepResponse> missedStepResponses) {
        return new OneStepResponse(todayStepResponses, missedStepResponses);
    }
}
