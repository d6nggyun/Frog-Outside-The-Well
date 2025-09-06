package com._oormthon.seasonthon.domain.member.service.impl;

import com._oormthon.seasonthon.domain.member.dto.response.*;
import com._oormthon.seasonthon.domain.member.dto.request.*;
import com._oormthon.seasonthon.domain.member.entity.*;
import com._oormthon.seasonthon.domain.member.repository.*;
import com._oormthon.seasonthon.domain.member.service.DailyLogService;
import com._oormthon.seasonthon.domain.member.enums.PlaceType;
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

        // ===== DailyLogBefore =====
        @Override
        public DailyLogBeforeResponse createBefore(DailyLogBeforeRequest request) {
                DailyLogBefore entity = DailyLogBefore.builder()
                                .emotion(request.getEmotion())
                                .userId(request.getUserId())
                                .energy(request.getEnergy())
                                .place(request.getPlace())
                                .build();

                DailyLogBefore saved = dailyLogBeforeRepository.save(entity);

                return DailyLogBeforeResponse.builder()
                                .id(saved.getId())
                                .emotion(saved.getEmotion())
                                .userId(saved.getUserId())
                                .energy(saved.getEnergy())
                                .place(saved.getPlace())
                                .createdAt(saved.getCreatedAt())
                                .build();
        }

        // ===== DailyLogAfter =====
        @Override
        public DailyLogAfterResponse createAfter(DailyLogAfterRequest request) {
                DailyLogAfter entity = DailyLogAfter.builder()
                                .mood(request.getMood())
                                .userId(request.getUserId())
                                .focusLevel(request.getFocusLevel())
                                .completionLevel(request.getCompletionLevel())
                                .memo(request.getMemo())
                                .photoUrl(request.getPhotoUrl())
                                .build();

                DailyLogAfter saved = dailyLogAfterRepository.save(entity);

                return DailyLogAfterResponse.builder()
                                .id(saved.getId())
                                .mood(saved.getMood())
                                .userId(saved.getUserId())
                                .focusLevel(saved.getFocusLevel())
                                .completionLevel(saved.getCompletionLevel())
                                .memo(saved.getMemo())
                                .photoUrl(saved.getPhotoUrl())
                                .createdAt(saved.getCreatedAt())
                                .build();
        }

        @Override
        public Map<PlaceType, Long> getThisWeekPlaceTypeCount(Long userId) {
                LocalDate today = LocalDate.now();
                LocalDate start = today.with(DayOfWeek.MONDAY);
                LocalDate end = today.with(DayOfWeek.SUNDAY);

                List<DailyLogBefore> logs = dailyLogBeforeRepository.findByUserIdAndCreatedAtBetween(userId, start,
                                end);

                return calculatePlaceTypeCount(logs);
        }

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

        private Map<PlaceType, Long> calculatePlaceTypeCount(List<DailyLogBefore> logs) {
                return logs.stream()
                                .collect(Collectors.groupingBy(DailyLogBefore::getPlace, Collectors.counting()));
        }
}
