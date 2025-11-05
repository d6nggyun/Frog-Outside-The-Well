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
import com._oormthon.seasonthon.domain.dailyLog.enums.WeatherType;
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
                LocalDate today = LocalDate.now();
                // 중복 방지 로직
                dailyLogBeforeRepository.findByUserIdAndCreatedAt(user.getUserId(), today)
                                .ifPresent(existing -> {
                                        throw new IllegalStateException("오늘의 일일 로그(Before)는 이미 작성되었습니다.");
                                });

                DailyLogBefore entity = DailyLogBefore.createDailyLogBefore(user, request);
                entity.setCreatedAt(today);
                DailyLogBefore saved = dailyLogBeforeRepository.save(entity);
                return DailyLogBeforeResponse.fromEntity(saved);
        }

        // ===== DailyLogAfter 생성 =====
        @Override
        public DailyLogAfterResponse createAfter(User user, DailyLogAfterRequest request) {
                LocalDate today = LocalDate.now();

                // 중복 방지 로직
                dailyLogAfterRepository.findByUserIdAndCreatedAt(user.getUserId(), today)
                                .ifPresent(existing -> {
                                        throw new IllegalStateException("오늘의 일일 로그(After)는 이미 작성되었습니다.");
                                });

                DailyLogAfter entity = DailyLogAfter.createDailyLogAfter(user, request);
                entity.setCreatedAt(today);
                DailyLogAfter saved = dailyLogAfterRepository.save(entity);
                return DailyLogAfterResponse.fromEntity(saved);
        }

        // ===== 이번 주 WeatherType 합계 =====
        @Override
        public Map<WeatherType, Long> getThisWeekWeatherTypeCount(Long userId) {
                LocalDate today = LocalDate.now();
                LocalDate start = today.with(DayOfWeek.MONDAY);
                LocalDate end = today.with(DayOfWeek.SUNDAY);

                List<DailyLogBefore> logs = dailyLogBeforeRepository.findByUserIdAndCreatedAtBetween(userId, start,
                                end);
                return calculateWeatherTypeCount(logs);
        }

        // ===== 이번 달 WeatherType 합계 =====
        @Override
        public Map<WeatherType, Long> getThisMonthWeatherTypeCount(Long userId) {
                YearMonth month = YearMonth.now();
                LocalDate start = month.atDay(1);
                LocalDate end = month.atEndOfMonth();

                List<DailyLogBefore> logs = dailyLogBeforeRepository.findByUserIdAndCreatedAtBetween(userId, start,
                                end);
                return calculateWeatherTypeCount(logs);
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

        @Override
        public Optional<DailyLogBeforeResponse> getBeforeByDate(Long userId, LocalDate date) {
                return dailyLogBeforeRepository.findByUserIdAndCreatedAtBetween(userId, date, date)
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

        @Override
        public DailyLogAfterResponse createAfterByDate(User user, LocalDate date, DailyLogAfterRequest request) {

                // 중복 방지 로직
                dailyLogAfterRepository.findByUserIdAndCreatedAt(user.getUserId(), date)
                                .ifPresent(existing -> {
                                        throw new IllegalStateException(date + "의 일일 로그(After)는 이미 작성되었습니다.");
                                });

                DailyLogAfter entity = DailyLogAfter.createDailyLogAfter(user, request);
                entity.setCreatedAt(date);
                DailyLogAfter saved = dailyLogAfterRepository.save(entity);
                return DailyLogAfterResponse.fromEntity(saved);
        }

        // ===== Helper =====
        private Map<WeatherType, Long> calculateWeatherTypeCount(List<DailyLogBefore> logs) {
                return logs.stream()
                                .collect(Collectors.groupingBy(DailyLogBefore::getWeather, Collectors.counting()));
        }
}
