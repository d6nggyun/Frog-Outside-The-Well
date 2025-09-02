package com._oormthon.seasonthon.domain.member.dto.response;

import com._oormthon.seasonthon.domain.member.entity.DailyLog;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogResponse {
    private Long logId;
    private Long memberId;
    private LocalDate logDate;
    private String emotion;
    private Integer focusLevel;
    private Integer completionLevel;
    private String memo;
    private String photoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DailyLogResponse fromEntity(DailyLog d) {
        if (d == null)
            return null;
        return DailyLogResponse.builder()
                .logId(d.getLogId())
                .memberId(d.getUser().getUserId())
                .logDate(d.getLogDate())
                .emotion(d.getEmotion())
                .focusLevel(d.getFocusLevel())
                .completionLevel(d.getCompletionLevel())
                .memo(d.getMemo())
                .photoUrl(d.getPhotoUrl())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
