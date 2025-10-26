package com._oormthon.seasonthon.domain.dailyLog.service;

import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogAfterRequest;
import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogBeforeRequest;
import com._oormthon.seasonthon.domain.dailyLog.dto.response.DailyLogAfterResponse;
import com._oormthon.seasonthon.domain.dailyLog.dto.response.DailyLogBeforeResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.dailyLog.enums.PlaceType;

import java.util.Map;
import java.util.Optional;

public interface DailyLogService {

    // DailyLogBefore
    DailyLogBeforeResponse createBefore(User user, DailyLogBeforeRequest request);

    // DailyLogAfter
    DailyLogAfterResponse createAfter(User user, DailyLogAfterRequest request);

    Optional<DailyLogBeforeResponse> getTodayBefore(Long userId);

    Optional<DailyLogAfterResponse> getTodayAfter(Long userId);

    // ===== PlaceType 합계 (userId별) =====
    Map<PlaceType, Long> getThisWeekPlaceTypeCount(Long userId);

    Map<PlaceType, Long> getThisMonthPlaceTypeCount(Long userId);
}
