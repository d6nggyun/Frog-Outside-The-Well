package com._oormthon.seasonthon.domain.diary.dto.res;

import com._oormthon.seasonthon.domain.member.entity.DailyLogAfter;
import com._oormthon.seasonthon.domain.member.entity.DailyLogBefore;
import com._oormthon.seasonthon.domain.member.enums.CompletionLevel;
import com._oormthon.seasonthon.domain.member.enums.Mood;
import com._oormthon.seasonthon.domain.member.enums.PlaceType;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoRatioByDay;

import java.time.LocalDate;
import java.util.List;

public record DiaryDetailResponse(

        LocalDate date,

        List<TodoRatioByDay> todoRatios,

        Integer emotion,

        Integer energy,

        PlaceType place,

        Mood mood,

        Integer focusLevel,

        CompletionLevel completionLevel,

        String memo,

        String photoUrl

) {
    public static DiaryDetailResponse of(
            LocalDate date,
            List<TodoRatioByDay> todoRatios,
            DailyLogBefore dailyLogBefore,
            DailyLogAfter dailyLogAfter
    ) {
        return new DiaryDetailResponse(
                date,
                todoRatios,
                dailyLogBefore.getEmotion(),
                dailyLogBefore.getEnergy(),
                dailyLogBefore.getPlace(),
                dailyLogAfter.getMood(),
                dailyLogAfter.getFocusLevel(),
                dailyLogAfter.getCompletionLevel(),
                dailyLogAfter.getMemo(),
                dailyLogAfter.getPhotoUrl()
        );
    }
}
