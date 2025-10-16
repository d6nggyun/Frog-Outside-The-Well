package com._oormthon.seasonthon.domain.diary.service;

import com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.member.repository.DailyLogAfterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DailyLogAfterRepository dailyLogAfterRepository;

    @Transactional(readOnly = true)
    public List<DiaryResponse> findDiaries(User user, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return dailyLogAfterRepository.findAllMoodByUserIdAndCreatedAtBetween(user.getUserId(), startDate, endDate);
    }
}
