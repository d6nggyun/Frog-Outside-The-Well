package com._oormthon.seasonthon.domain.diary.dto.res;

import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogAfter;
import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogBefore;
import com._oormthon.seasonthon.domain.dailyLog.enums.CompletionLevel;
import com._oormthon.seasonthon.domain.dailyLog.enums.Mood;
import com._oormthon.seasonthon.domain.dailyLog.enums.WeatherType;
import com._oormthon.seasonthon.domain.todo.dto.res.TodayCompletedTodoResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
                Optional<DailyLogBefore> dailyLogBefore,
                Optional<DailyLogAfter> dailyLogAfter) {
                return new DiaryDetailResponse(
                                date,
                                todayCompletedTodoResponses,
                                dailyLogBefore.map(DailyLogBefore::getEmotion).orElse(null),
                                dailyLogBefore.map(DailyLogBefore::getEnergy).orElse(null),
                                dailyLogBefore.map(DailyLogBefore::getWeather).orElse(null),
                                dailyLogAfter.map(DailyLogAfter::getMood).orElse(null),
                                dailyLogAfter.map(DailyLogAfter::getFocusLevel).orElse(null),
                                dailyLogAfter.map(DailyLogAfter::getCompletionLevel).orElse(null),
                                dailyLogAfter.map(DailyLogAfter::getMemo).orElse(null),
                                dailyLogAfter.map(DailyLogAfter::getPhotoUrl).orElse(null)
                );
        }
}
