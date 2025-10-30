package com._oormthon.seasonthon.domain.statistics.service;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.statistics.dto.res.AchievementRateResponse;
import com._oormthon.seasonthon.domain.statistics.dto.res.FocusTimeResponse;
import com._oormthon.seasonthon.domain.statistics.dto.res.MonthlyTodosResponse;
import com._oormthon.seasonthon.domain.statistics.util.FixedWeeks;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.service.StepQueryService;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.enums.TodoType;
import com._oormthon.seasonthon.domain.todo.service.TodoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TodoQueryService todoQueryService;
    private final StepQueryService stepQueryService;

    @Transactional(readOnly = true)
    public List<MonthlyTodosResponse> getTodosMonthly(User user, YearMonth yearMonth, TodoType todoType) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Todo> todos = todoQueryService.getTodosByUserIdAndTodoTypeAndMonth(user.getUserId(), todoType, startDate, endDate);

        return todos.stream().map(todo -> {
            List<TodoStep> todoSteps = stepQueryService.getAllTodoStepByTodoId(todo.getId());
            Long totalDuration = todoSteps.stream().mapToLong(TodoStep::getTotalDuration).sum();

            return MonthlyTodosResponse.of(todo.getTitle(), todo.getStartDate(), todo.getEndDate(), totalDuration);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementRateResponse> getAchievementRate(User user, YearMonth yearMonth) {
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<Todo> todos = todoQueryService.getTodosByUserIdAndMonth(user.getUserId(), monthStart, monthEnd);

        List<FixedWeeks.WeekBucket> buckets = FixedWeeks.getWeekBuckets(yearMonth);

        long[] sum = new long[6];
        int[] count = new int[6];

        for (Todo todo : todos) {
            LocalDate endDate = todo.getEndDate();
            Integer progress = todo.getProgress();
            if (endDate == null || progress == null) continue;

            int week = FixedWeeks.getWeekIndex(endDate);

            sum[week] += progress;
            count[week] += 1;
        }

        return buckets.stream().map(
                b -> {
                    int week = b.index();
                    Double rate = (count[week] == 0) ? null : round1(sum[week] * 1.0 / count[week]);
                    return AchievementRateResponse.of(b.index(), rate, b.startDate(), b.endDate());
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<FocusTimeResponse> getFocusTime(User user, YearMonth yearMonth) {
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<TodoStep> todoSteps = stepQueryService.getTodoStepsByUserIdAndMonth(user.getUserId(), monthStart, monthEnd);

        List<FixedWeeks.WeekBucket> buckets = FixedWeeks.getWeekBuckets(yearMonth);

        Long[] minDuration = new Long[6];
        Long[] maxDuration = new Long[6];

        for (TodoStep todoStep : todoSteps) {
            LocalDate stepDate = todoStep.getStepDate();
            long duration = todoStep.getTotalDuration();
            if (stepDate == null) continue;

            int week = FixedWeeks.getWeekIndex(stepDate);

            if (minDuration[week] == null || duration < minDuration[week]) {
                minDuration[week] = duration;
            }
            if (maxDuration[week] == null || duration > maxDuration[week]) {
                maxDuration[week] = duration;
            }
        }

        return buckets.stream().map(
                b -> {
                    int week = b.index();
                    return FocusTimeResponse.of(b.index(), minDuration[week], maxDuration[week] , b.startDate(), b.endDate());
                }).toList();
    }

    private Double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
