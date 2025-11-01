package com._oormthon.seasonthon.domain.stepCalendar.service;

import com._oormthon.seasonthon.domain.stepCalendar.domain.StepCalendar;
import com._oormthon.seasonthon.domain.stepCalendar.domain.StepCalendarTodoStep;
import com._oormthon.seasonthon.domain.stepCalendar.dto.res.ListStepCalendarResponse;
import com._oormthon.seasonthon.domain.stepCalendar.dto.res.StepCalendarResponse;
import com._oormthon.seasonthon.domain.stepCalendar.dto.res.TodoAndStepResponse;
import com._oormthon.seasonthon.domain.stepCalendar.repository.StepCalendarRepository;
import com._oormthon.seasonthon.domain.stepCalendar.repository.StepCalendarTodoStepRepository;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.service.StepQueryService;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.service.TodoQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StepCalendarService {

    private final StepCalendarRepository stepCalendarRepository;
    private final StepCalendarTodoStepRepository stepCalendarTodoStepRepository;
    private final StepQueryService stepQueryService;
    private final TodoQueryService todoQueryService;

    @Transactional(readOnly = true)
    public ListStepCalendarResponse findTodoCalendar(User user, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<StepCalendar> calendars = stepCalendarRepository
                .findAllByUserIdAndCalendarDateBetween(user.getUserId(), startDate, endDate);

        List<StepCalendarResponse> stepCalendarResponses = createCalendarResponses(calendars);
        List<TodoAndStepResponse> TaskToDo = responseTaskToDo(LocalDate.now(), user.getUserId());

        return ListStepCalendarResponse.from(stepCalendarResponses, TaskToDo);
    }

    public StepCalendar saveAndUpdateStepCalendar(Long userId, LocalDate date) {
        StepCalendar stepCalendar = stepCalendarRepository.findByUserIdAndCalendarDate(userId, date)
                .orElseGet(() -> {
                    StepCalendar newStepCalendar = StepCalendar.builder()
                            .userId(userId)
                            .calendarDate(date)
                            .build();

                    return stepCalendarRepository.save(newStepCalendar);
                });

        int totalDailySteps = stepQueryService.countTodoStepsByUserIdAndStepDate(userId, date);
        int completedDailySteps = stepQueryService.countCompletedTodoStepsByUserIdAndStepDate(userId, date);
        int percentage = totalDailySteps == 0 ? 0 : (completedDailySteps * 100 / totalDailySteps);

        stepCalendar.updatePercentage(percentage);

        return stepCalendar;
    }

    public void saveStepCalendarTodoStep(Long userId, Long stepCalendarId, Long todoStepId) {
        boolean exists = stepCalendarTodoStepRepository
                .existsByUserIdAndStepCalendarIdAndTodoStepId(userId, stepCalendarId, todoStepId);

        if (!exists) {
            stepCalendarTodoStepRepository
                    .save(StepCalendarTodoStep.builder().userId(userId).stepCalendarId(stepCalendarId).todoStepId(todoStepId).build());
        }
    }

    private List<StepCalendarResponse> createCalendarResponses(List<StepCalendar> calendars) {
        return calendars.stream().map(stepCalendar ->
                StepCalendarResponse.from(stepCalendar, responseCalendarSteps(stepCalendar))).toList();
    }

    private List<TodoAndStepResponse> responseCalendarSteps(StepCalendar stepCalendar) {
        return stepCalendarTodoStepRepository
                .findAllByStepCalendarId(stepCalendar.getId())
                .stream()
                .map(stepCalendarTodoStep -> {
                    TodoStep todoStep = stepQueryService.getTodoStepById(stepCalendarTodoStep.getTodoStepId());
                    Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

                    return TodoAndStepResponse.of(todo, todoStep);
                })
                .toList();
    }

    private List<TodoAndStepResponse> responseTaskToDo(LocalDate date, Long userId) {
        List<TodoStep> todoSteps = stepQueryService.findAllByStepDateAndUserId(date, userId);

        return todoSteps.stream().map(todoStep -> {
            Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());
            return TodoAndStepResponse.of(todo, todoStep);
        }).toList();
    }
}
