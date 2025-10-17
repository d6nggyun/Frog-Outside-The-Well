package com._oormthon.seasonthon.domain.diary.service;

import com._oormthon.seasonthon.domain.diary.dto.res.DiaryDetailResponse;
import com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse;
import com._oormthon.seasonthon.domain.member.entity.DailyLogAfter;
import com._oormthon.seasonthon.domain.member.entity.DailyLogBefore;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.repository.DailyLogAfterRepository;
import com._oormthon.seasonthon.domain.member.repository.DailyLogBeforeRepository;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DailyLogBeforeRepository dailyLogBeforeRepository;
    private final DailyLogAfterRepository dailyLogAfterRepository;

    @Transactional(readOnly = true)
    public List<DiaryResponse> findDiaries(User user, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return dailyLogAfterRepository.findAllMoodByUserIdAndCreatedAtBetween(user.getUserId(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponse getDiaryDetail(User user, LocalDate date) {
        DailyLogBefore dailyLogBefore = getDailyLogBefore(user.getUserId(), date);
        DailyLogAfter dailyLogAfter = getDailyLogAfter(user.getUserId(), date);

        // Today의 StepCalendarTodoStep들을 불러오고 그 수와 그 Step에 해당하는 Todo를 불러와서 TodoRatios를 만들어야한다.

        return DiaryDetailResponse.of(date, todoRatios, dailyLogBefore, dailyLogAfter);
    }

    private DailyLogBefore getDailyLogBefore(Long userId, LocalDate date) {
        return dailyLogBeforeRepository.findByUserIdAndCreatedAt(userId, date)
                .orElseThrow(() -> {
                    log.warn("Daily Log Before을 찾을 수 없습니다. userId: {}, date: {}", userId, date);
                    return new CustomException(ErrorCode.DAILY_LOG_BEFORE_NOT_FOUND);
                });
    }

    private DailyLogAfter getDailyLogAfter(Long userId, LocalDate date) {
        return dailyLogAfterRepository.findByUserIdAndCreatedAt(userId, date)
                .orElseThrow(() -> {
                    log.warn("Daily Log After을 찾을 수 없습니다. userId: {}, date: {}", userId, date);
                    return new CustomException(ErrorCode.DAILY_LOG_AFTER_NOT_FOUND);
                });
    }
}
