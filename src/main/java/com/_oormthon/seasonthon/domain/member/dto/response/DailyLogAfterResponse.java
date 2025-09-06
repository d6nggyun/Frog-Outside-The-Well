package com._oormthon.seasonthon.domain.member.dto.response;

import com._oormthon.seasonthon.domain.member.enums.Mood;
import com._oormthon.seasonthon.domain.member.entity.DailyLogAfter;
import com._oormthon.seasonthon.domain.member.enums.CompletionLevel;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogAfterResponse {
    private Long id;
    private Mood mood;
    private Long userId;
    private int focusLevel;
    private CompletionLevel completionLevel;
    private String memo;
    private String photoUrl;
    private LocalDate createdAt;

    public static DailyLogAfterResponse fromEntity(DailyLogAfter entity) {
        return DailyLogAfterResponse.builder()
                .id(entity.getId())
                .mood(entity.getMood())
                .userId(entity.getUserId())
                .focusLevel(entity.getFocusLevel())
                .completionLevel(entity.getCompletionLevel())
                .memo(entity.getMemo())
                .photoUrl(entity.getPhotoUrl())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}