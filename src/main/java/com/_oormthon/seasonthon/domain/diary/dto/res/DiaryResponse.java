package com._oormthon.seasonthon.domain.diary.dto.res;

import com._oormthon.seasonthon.domain.dailyLog.enums.Mood;

import java.time.LocalDate;

public record DiaryResponse(

        LocalDate date,

        Mood mood

) {
}
