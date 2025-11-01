package com._oormthon.seasonthon.domain.dailyLog.service.impl;

import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogAfter;
import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogBefore;
import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogAfterRequest;
import com._oormthon.seasonthon.domain.dailyLog.dto.request.DailyLogBeforeRequest;
import com._oormthon.seasonthon.domain.dailyLog.dto.response.DailyLogAfterResponse;
import com._oormthon.seasonthon.domain.dailyLog.dto.response.DailyLogBeforeResponse;
import com._oormthon.seasonthon.domain.dailyLog.repository.DailyLogAfterRepository;
import com._oormthon.seasonthon.domain.dailyLog.repository.DailyLogBeforeRepository;
import com._oormthon.seasonthon.domain.member.entity.*;
import com._oormthon.seasonthon.domain.dailyLog.service.DailyLogService;
import com._oormthon.seasonthon.domain.dailyLog.enums.PlaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DailyLogServiceImpl implements DailyLogService {

        private final DailyLogBeforeRepository dailyLogBeforeRepository;
        private final DailyLogAfterRepository dailyLogAfterRepository;

        // ===== DailyLogBefore 생성 =====
        @Override
        public DailyLogBeforeResponse createBefore(User user, DailyLogBeforeRequest request) {
                DailyLogBefore entity = DailyLogBefore.createDailyLogBefore(user, request);

                DailyLogBefore saved = dailyLogBeforeRepository.save(entity);
                return DailyLogBeforeResponse.fromEntity(saved);
        }

        // ===== DailyLogAfter 생성 =====
        @Override
        public DailyLogAfterResponse createAfter(User user, DailyLogAfterRequest request) {
                DailyLogAfter entity = DailyLogAfter.createDailyLogAfter(user, request);

                DailyLogAfter saved = dailyLogAfterRepository.save(entity);
                return DailyLogAfterResponse.fromEntity(saved);
        }

        // ===== 이번 주 PlaceType 합계 =====
        @Override
        public Map<PlaceType, Long> getThisWeekPlaceTypeCount(Long userId) {
                LocalDate today = LocalDate.now();
                LocalDate start = today.with(DayOfWeek.MONDAY);
                LocalDate end = today.with(DayOfWeek.SUNDAY);

                List<DailyLogBefore> logs = dailyLogBeforeRepository.findByUserIdAndCreatedAtBetween(userId, start,
                                end);
                return calculatePlaceTypeCount(logs);
        }

        // ===== 이번 달 PlaceType 합계 =====
        @Override
        public Map<PlaceType, Long> getThisMonthPlaceTypeCount(Long userId) {
                YearMonth month = YearMonth.now();
                LocalDate start = month.atDay(1);
                LocalDate end = month.atEndOfMonth();

                List<DailyLogBefore> logs = dailyLogBeforeRepository.findByUserIdAndCreatedAtBetween(userId, start,
                                end);
                return calculatePlaceTypeCount(logs);
        }

        // ===== 오늘의 DailyLogBefore =====
        @Override
        public Optional<DailyLogBeforeResponse> getTodayBefore(Long userId) {
                LocalDate today = LocalDate.now();
                return dailyLogBeforeRepository.findByUserIdAndCreatedAtBetween(userId, today, today)
                                .stream()
                                .findFirst()
                                .map(DailyLogBeforeResponse::fromEntity);
        }

        // ===== 오늘의 DailyLogAfter =====
        @Override
        public Optional<DailyLogAfterResponse> getTodayAfter(Long userId) {
                LocalDate today = LocalDate.now();
                return dailyLogAfterRepository.findByUserIdAndCreatedAtBetween(userId, today, today)
                                .stream()
                                .findFirst()
                                .map(DailyLogAfterResponse::fromEntity);
        }

        // ===== Helper =====
        private Map<PlaceType, Long> calculatePlaceTypeCount(List<DailyLogBefore> logs) {
                return logs.stream()
                                .collect(Collectors.groupingBy(DailyLogBefore::getPlace, Collectors.counting()));
        }
}
