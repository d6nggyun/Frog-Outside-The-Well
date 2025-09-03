package com._oormthon.seasonthon.domain.member.controller;

import com._oormthon.seasonthon.global.response.DataResponseDto;
import com._oormthon.seasonthon.global.response.ResponseDto;
import com._oormthon.seasonthon.domain.member.dto.request.DailyLogCreateRequest;
import com._oormthon.seasonthon.domain.member.dto.request.DailyLogUpdateRequest;
import com._oormthon.seasonthon.domain.member.dto.response.DailyLogResponse;
import com._oormthon.seasonthon.domain.member.service.DailyLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/daily-logs")
@RequiredArgsConstructor
public class DailyLogController {

    private final DailyLogService dailyLogService;

    @PostMapping
    public ResponseDto<DailyLogResponse> create(@PathVariable Long userId,
            @Valid @RequestBody DailyLogCreateRequest req) {
        DailyLogResponse res = dailyLogService.createDailyLog(userId, req);
        return DataResponseDto.of(res);
    }

    @GetMapping
    public ResponseDto<List<DailyLogResponse>> list(@PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<DailyLogResponse> res = dailyLogService.listDailyLogs(userId, start, end);
        return DataResponseDto.of(res);
    }

    @GetMapping("/{date}")
    public ResponseDto<DailyLogResponse> get(@PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailyLogResponse res = dailyLogService.getDailyLog(userId, date);
        return DataResponseDto.of(res);
    }

    @PutMapping("/{date}")
    public ResponseDto<DailyLogResponse> update(@PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody DailyLogUpdateRequest req) {
        DailyLogResponse res = dailyLogService.updateDailyLog(userId, date, req);
        return DataResponseDto.of(res);
    }

    @DeleteMapping("/{date}")
    public ResponseDto<Void> delete(@PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        dailyLogService.deleteDailyLog(userId, date);
        return DataResponseDto.of(null);
    }
}
