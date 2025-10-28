package com._oormthon.seasonthon.domain.statistics.service;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.statistics.dto.res.AchievementRateResponse;
import com._oormthon.seasonthon.domain.statistics.dto.res.FocusTimeResponse;
import com._oormthon.seasonthon.domain.statistics.dto.res.MonthlyTodosResponse;
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
            Long totalDuration = todoSteps.stream().mapToLong(step -> step.getTotalDuration() != null ? step.getTotalDuration() : 0L).sum();

            return MonthlyTodosResponse.of(todo.getTitle(), todo.getStartDate(), todo.getEndDate(), totalDuration);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementRateResponse> getAchievementRate(User user, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Todo> todos = todoQueryService.getTodosByUserIdAndMonth(user.getUserId(), startDate, endDate);


    }

    @Transactional(readOnly = true)
    public List<FocusTimeResponse> getFocusTime(User user, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

    }
}
