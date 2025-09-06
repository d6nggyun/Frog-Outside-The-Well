package com._oormthon.seasonthon.domain.member.controller;

import com._oormthon.seasonthon.domain.member.dto.request.DailyLogBeforeRequest;
import com._oormthon.seasonthon.domain.member.dto.request.DailyLogAfterRequest;
import com._oormthon.seasonthon.domain.member.dto.response.DailyLogBeforeResponse;
import com._oormthon.seasonthon.domain.member.dto.response.DailyLogAfterResponse;
import com._oormthon.seasonthon.domain.member.enums.PlaceType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface DailyLogApiSpecification {

    // DailyLogBefore
    ResponseEntity<DailyLogBeforeResponse> createBefore(DailyLogBeforeRequest request, Long userId);

    ResponseEntity<DailyLogBeforeResponse> getTodayBefore(Long userId);

    ResponseEntity<Map<PlaceType, Long>> getThisWeekPlaceType(Long userId);

    ResponseEntity<Map<PlaceType, Long>> getThisMonthPlaceType(Long userId);

    // DailyLogAfter
    ResponseEntity<DailyLogAfterResponse> createAfter(DailyLogAfterRequest request, Long userId);

    ResponseEntity<DailyLogAfterResponse> getTodayAfter(Long userId);
}
