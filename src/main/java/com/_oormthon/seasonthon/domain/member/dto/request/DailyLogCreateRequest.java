package com._oormthon.seasonthon.domain.member.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogCreateRequest {
    @NotNull
    private LocalDate logDate;

    private String emotion;

    @Min(1)
    @Max(5)
    private Integer focusLevel;

    @Min(1)
    @Max(5)
    private Integer completionLevel;

    private String memo;
    private String photoUrl;
}
