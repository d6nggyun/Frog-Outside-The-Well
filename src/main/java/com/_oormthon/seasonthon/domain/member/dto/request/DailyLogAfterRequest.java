package com._oormthon.seasonthon.domain.member.dto.request;

import com._oormthon.seasonthon.domain.member.enums.Mood;
import com._oormthon.seasonthon.domain.member.enums.CompletionLevel;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogAfterRequest {
    private Mood mood;
    private Long userId;
    private int focusLevel;
    private CompletionLevel completionLevel;
    private String memo;
    private String photoUrl;
}