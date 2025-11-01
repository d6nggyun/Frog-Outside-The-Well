package com._oormthon.seasonthon.domain.stepRecord.dto.req;

import java.time.LocalDateTime;

public record StepStopRequest(

        LocalDateTime endTime,

        Long duration

) {
}
