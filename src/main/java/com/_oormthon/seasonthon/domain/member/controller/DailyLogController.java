package com._oormthon.seasonthon.domain.member.controller;

import com._oormthon.seasonthon.domain.member.enums.PlaceType;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.dto.response.*;
import com._oormthon.seasonthon.domain.member.dto.request.*;
import com._oormthon.seasonthon.domain.member.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/daily-log")
@RequiredArgsConstructor
public class DailyLogController implements DailyLogApiSpecification{

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

    @GetMapping("/before/place/this-week")
    public ResponseEntity<Map<PlaceType, Long>> getThisWeekPlaceType(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dailyLogService.getThisWeekPlaceTypeCount(user.getUserId()));
    }

    // 이번 달 PlaceType 합계
    @GetMapping("/before/place/this-month")
    public ResponseEntity<Map<PlaceType, Long>> getThisMonthPlaceType(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dailyLogService.getThisMonthPlaceTypeCount(user.getUserId()));
    }

}
