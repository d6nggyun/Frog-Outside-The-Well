package com._oormthon.seasonthon.domain.diary.service;

import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogAfter;
import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogBefore;
import com._oormthon.seasonthon.domain.dailyLog.repository.DailyLogAfterRepository;
import com._oormthon.seasonthon.domain.dailyLog.repository.DailyLogBeforeRepository;
import com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryQueryService {

    private final DailyLogBeforeRepository dailyLogBeforeRepository;
    private final DailyLogAfterRepository dailyLogAfterRepository;

    public List<DiaryResponse> findAllDiaries(Long userId, LocalDate startDate, LocalDate endDate) {
        return dailyLogAfterRepository.findAllMoodByUserIdAndCreatedAtBetween(userId, startDate, endDate);
    }

    public Optional<DailyLogBefore> findDailyLogBefore(Long userId, LocalDate date) {
        return dailyLogBeforeRepository.findByUserIdAndCreatedAt(userId, date);
    }

    public Optional<DailyLogAfter> findDailyLogAfter(Long userId, LocalDate date) {
        return dailyLogAfterRepository.findByUserIdAndCreatedAt(userId, date);
    }
}
