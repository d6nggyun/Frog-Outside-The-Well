package com._oormthon.seasonthon.domain.StepCalendar.service;

import com._oormthon.seasonthon.domain.StepCalendar.domain.StepCalendar;
import com._oormthon.seasonthon.domain.StepCalendar.dto.res.StepCalendarResponse;
import com._oormthon.seasonthon.domain.StepCalendar.repository.StepCalendarRepository;
import com._oormthon.seasonthon.domain.StepCalendar.repository.StepCalendarTodoStepRepository;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.step.service.StepQueryService;
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

    @Transactional(readOnly = true)
    public List<StepCalendarResponse> findTodoCalendar(User user, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<StepCalendar> stepCalendars = stepCalendarRepository
                .findAllByUserIdAndCalendarDateBetween(user.getUserId(), startDate, endDate);

        return stepCalendars.stream().map(stepCalendar ->
                StepCalendarResponse.from(stepCalendar, responseCalendarSteps(stepCalendar))).toList();
    }

    public void saveStepCalendar(Long userId, LocalDate date) {
        StepCalendar stepCalendar = stepCalendarRepository.findByUserIdAndCalendarDate(userId, date)
                .orElseGet(() -> {
                    StepCalendar newStepCalendar = StepCalendar.builder()
                            .userId(userId)
                            .calendarDate(date)
                            .build();

                    return stepCalendarRepository.save(newStepCalendar);
                });

        int totalDailySteps = stepQueryService.countTodoStepsByUserIdAndDate(userId, date);
        int completedDailySteps = stepQueryService.countCompletedTodoStepsByUserIdAndDate(userId, date);
        int percentage = totalDailySteps == 0 ? 0 : (completedDailySteps * 100 / totalDailySteps);

        stepCalendar.updatePercentage(percentage);
    }

    private List<StepResponse> responseCalendarSteps(StepCalendar stepCalendar) {
        return stepCalendarTodoStepRepository
                .findAllByStepCalendarId(stepCalendar.getId())
                .stream()
                .map(stepCalendarTodoStep -> {
                    TodoStep todoStep = stepQueryService.getTodoStepById(stepCalendarTodoStep.getTodoStepId());

                    return StepResponse.from(todoStep);
                })
                .toList();
    }
}
