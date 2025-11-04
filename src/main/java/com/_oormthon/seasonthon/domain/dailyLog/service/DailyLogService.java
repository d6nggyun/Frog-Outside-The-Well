package com._oormthon.seasonthon.domain.dailyLog.service;

import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogAfterRequest;
import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogBeforeRequest;
import com._oormthon.seasonthon.domain.dailyLog.dto.response.DailyLogAfterResponse;
import com._oormthon.seasonthon.domain.dailyLog.dto.response.DailyLogBeforeResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.dailyLog.enums.WeatherType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public interface DailyLogService {

    // DailyLogBefore
    DailyLogBeforeResponse createBefore(User user, DailyLogBeforeRequest request);

    // DailyLogAfter
    DailyLogAfterResponse createAfter(User user, DailyLogAfterRequest request);

    DailyLogAfterResponse createAfterByDate(User user, LocalDate date, DailyLogAfterRequest request);

    Optional<DailyLogBeforeResponse> getTodayBefore(Long userId);

    Optional<DailyLogBeforeResponse> getBeforeByDate(Long userId, LocalDate date);

    Optional<DailyLogAfterResponse> getTodayAfter(Long userId);

    // ===== WeatherType 합계 (userId별) =====
    Map<WeatherType, Long> getThisWeekWeatherTypeCount(Long userId);

    Map<WeatherType, Long> getThisMonthWeatherTypeCount(Long userId);
}
