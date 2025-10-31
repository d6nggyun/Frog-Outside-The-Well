package com._oormthon.seasonthon.domain.step.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record OneStepResponse(

        @Schema(description = "오늘의 한 걸음")
        List<StepResponse> todayStepResponses,

        @Schema(description = "놓친 한 걸음")
        List<StepResponse> missedStepResponses,

        @Schema(description = "놓친 한 걸음 중 오늘 완료된 한 걸음")
        List<StepResponse> completedMissedStepResponses

) {
    public static OneStepResponse of(List<StepResponse> todayStepResponses, List<StepResponse> missedStepResponses,
                                     List<StepResponse> completedMissedStepResponses) {
        return new OneStepResponse(todayStepResponses, missedStepResponses, completedMissedStepResponses);
    }
}
