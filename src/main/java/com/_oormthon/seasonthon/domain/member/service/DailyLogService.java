package com._oormthon.seasonthon.domain.member.service;

import com._oormthon.seasonthon.domain.member.dto.request.DailyLogCreateRequest;
import com._oormthon.seasonthon.domain.member.dto.request.DailyLogUpdateRequest;
import com._oormthon.seasonthon.domain.member.dto.response.DailyLogResponse;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogService {
    DailyLogResponse createDailyLog(Long memberId, DailyLogCreateRequest req);

    DailyLogResponse getDailyLog(Long memberId, LocalDate date);

    List<DailyLogResponse> listDailyLogs(Long memberId, LocalDate start, LocalDate end);

    DailyLogResponse updateDailyLog(Long memberId, LocalDate date, DailyLogUpdateRequest req);

    void deleteDailyLog(Long memberId, LocalDate date);
}
