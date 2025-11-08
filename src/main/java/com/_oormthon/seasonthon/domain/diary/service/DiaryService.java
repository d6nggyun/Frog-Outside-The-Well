package com._oormthon.seasonthon.domain.diary.service;

import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogAfter;
import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogBefore;
import com._oormthon.seasonthon.domain.diary.dto.res.DiaryDetailResponse;
import com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.TodoDurationGroup;
import com._oormthon.seasonthon.domain.stepRecord.service.StepRecordQueryService;
import com._oormthon.seasonthon.domain.todo.dto.res.TodayCompletedTodoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryQueryService diaryQueryService;
    private final StepRecordQueryService stepRecordQueryService;

    @Transactional(readOnly = true)
    public List<DiaryResponse> findDiaries(User user, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return diaryQueryService.findAllDiaries(user.getUserId(), startDate, endDate);
    }

    @Transactional(readOnly = true)
    public DiaryDetailResponse getDiaryDetail(User user, LocalDate date) {
        Optional<DailyLogBefore> dailyLogBefore = getDailyLogBefore(user.getUserId(), date);
        Optional<DailyLogAfter> dailyLogAfter = getDailyLogAfter(user.getUserId(), date);

        List<TodoDurationGroup> todoDurationGroups = stepRecordQueryService.findTodoDurationGroup(user.getUserId(), date);

        long total = todoDurationGroups.stream()
                .map(TodoDurationGroup::getTotalDuration)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        List<TodayCompletedTodoResponse> todayCompletedTodoResponses = todoDurationGroups.stream()
                .map(tdg -> {
                    Long secs = tdg.getTotalDuration() == null ? 0L : tdg.getTotalDuration();
                    Double ratio = (total == 0) ? 0.0 : (double) secs / total;
                    return TodayCompletedTodoResponse.of(
                            tdg.getTodoId(),
                            tdg.getTodoTitle(),
                            secs,
                            ratio);
                })
                .sorted(Comparator.comparingDouble(TodayCompletedTodoResponse::ratio).reversed())
                .toList();

        return DiaryDetailResponse.of(date, todayCompletedTodoResponses, dailyLogBefore, dailyLogAfter);
    }

    private Optional<DailyLogBefore> getDailyLogBefore(Long userId, LocalDate date) {
        return diaryQueryService.findDailyLogBefore(userId, date);
    }

    private Optional<DailyLogAfter> getDailyLogAfter(Long userId, LocalDate date) {
        return diaryQueryService.findDailyLogAfter(userId, date);
    }
}
