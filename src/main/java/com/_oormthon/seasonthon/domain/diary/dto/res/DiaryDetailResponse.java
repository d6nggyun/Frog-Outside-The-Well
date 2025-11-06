package com._oormthon.seasonthon.domain.diary.dto.res;

import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogAfter;
import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogBefore;
import com._oormthon.seasonthon.domain.dailyLog.enums.CompletionLevel;
import com._oormthon.seasonthon.domain.dailyLog.enums.Mood;
import com._oormthon.seasonthon.domain.dailyLog.enums.WeatherType;
import com._oormthon.seasonthon.domain.todo.dto.res.TodayCompletedTodoResponse;

import java.time.LocalDate;
import java.util.List;

public record DiaryDetailResponse(

                LocalDate date,

                List<TodayCompletedTodoResponse> todayCompletedTodoResponses,

                Integer emotion,

                Integer energy,

                WeatherType weather,

                Mood mood,

                Integer focusLevel,

                CompletionLevel completionLevel,

                String memo,

                String photoUrl

) {
        public static DiaryDetailResponse of(
                        LocalDate date,
                        List<TodayCompletedTodoResponse> todayCompletedTodoResponses,
                        DailyLogBefore dailyLogBefore,
                        DailyLogAfter dailyLogAfter) {
                return new DiaryDetailResponse(
                                date,
                                todayCompletedTodoResponses,
                                dailyLogBefore.getEmotion(),
                                dailyLogBefore.getEnergy(),
                                dailyLogBefore.getWeather(),
                                dailyLogAfter != null ? dailyLogAfter.getMood() : null,
                                dailyLogAfter != null ? dailyLogAfter.getFocusLevel() : null,
                                dailyLogAfter != null ? dailyLogAfter.getCompletionLevel() : null,
                                dailyLogAfter != null ? dailyLogAfter.getMemo() : null,
                                dailyLogAfter != null ? dailyLogAfter.getPhotoUrl() : null
                );
        }
}
