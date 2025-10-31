package com._oormthon.seasonthon.domain.dailyLog.controller;

import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogAfterRequest;
import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogBeforeRequest;
import com._oormthon.seasonthon.domain.dailyLog.dto.response.DailyLogAfterResponse;
import com._oormthon.seasonthon.domain.dailyLog.dto.response.DailyLogBeforeResponse;
import com._oormthon.seasonthon.domain.dailyLog.enums.WeatherType;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.dailyLog.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/daily-log")
@RequiredArgsConstructor
public class DailyLogController implements DailyLogApiSpecification {

    private final DailyLogService dailyLogService;

    // ===== DailyLogBefore =====
    @PostMapping("/before")
    public ResponseEntity<DailyLogBeforeResponse> createBefore(
            @AuthenticationPrincipal User user,
            @RequestBody DailyLogBeforeRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(dailyLogService.createBefore(user, request));
    }

    // ===== DailyLogAfter =====
    @PostMapping("/after")
    public ResponseEntity<DailyLogAfterResponse> createAfter(
            @AuthenticationPrincipal User user,
            @RequestBody DailyLogAfterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(dailyLogService.createAfter(user, request));
    }

    // 오늘의 DailyLogBefore 조회
    @GetMapping("/before/today")
    public ResponseEntity<DailyLogBeforeResponse> getTodayBefore(@AuthenticationPrincipal User user) {
        return dailyLogService.getTodayBefore(user.getUserId())
                .map(log -> ResponseEntity.status(HttpStatus.OK).body(log))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    // 오늘의 DailyLogAfter 조회
    @GetMapping("/after/today")
    public ResponseEntity<DailyLogAfterResponse> getTodayAfter(@AuthenticationPrincipal User user) {
        return dailyLogService.getTodayAfter(user.getUserId())
                .map(log -> ResponseEntity.status(HttpStatus.OK).body(log))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    @GetMapping("/before/Weather/this-week")
    public ResponseEntity<Map<WeatherType, Long>> getThisWeekWeatherType(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dailyLogService.getThisWeekWeatherTypeCount(user.getUserId()));
    }

    // 이번 달 WeatherType 합계
    @GetMapping("/before/Weather/this-month")
    public ResponseEntity<Map<WeatherType, Long>> getThisMonthWeatherType(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dailyLogService.getThisMonthWeatherTypeCount(user.getUserId()));
    }

}
