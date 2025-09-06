package com._oormthon.seasonthon.domain.member.service;

import com._oormthon.seasonthon.domain.member.dto.response.*;
import com._oormthon.seasonthon.domain.member.dto.request.*;
import com._oormthon.seasonthon.domain.member.enums.PlaceType;

import java.util.Map;
import java.util.Optional;

public interface DailyLogService {

    // DailyLogBefore
    DailyLogBeforeResponse createBefore(DailyLogBeforeRequest request);

    // DailyLogAfter
    DailyLogAfterResponse createAfter(DailyLogAfterRequest request);

    Optional<DailyLogBeforeResponse> getTodayBefore(Long userId);

    Optional<DailyLogAfterResponse> getTodayAfter(Long userId);

    // ===== PlaceType 합계 (userId별) =====
    Map<PlaceType, Long> getThisWeekPlaceTypeCount(Long userId);

    Map<PlaceType, Long> getThisMonthPlaceTypeCount(Long userId);
}
