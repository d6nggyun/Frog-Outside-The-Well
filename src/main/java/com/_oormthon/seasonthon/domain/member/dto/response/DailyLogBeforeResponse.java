package com._oormthon.seasonthon.domain.member.dto.response;

import com._oormthon.seasonthon.domain.member.entity.DailyLogBefore;
import com._oormthon.seasonthon.domain.member.enums.PlaceType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogBeforeResponse {
    private Long id;
    private int emotion;
    private Long userId;
    private int energy;
    private PlaceType place;
    private LocalDate createdAt;

    public static DailyLogBeforeResponse fromEntity(DailyLogBefore entity) {
        return DailyLogBeforeResponse.builder()
                .id(entity.getId())
                .emotion(entity.getEmotion())
                .userId(entity.getUserId())
                .energy(entity.getEnergy())
                .place(entity.getPlace())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}