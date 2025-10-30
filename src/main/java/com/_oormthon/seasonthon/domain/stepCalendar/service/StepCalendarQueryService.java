package com._oormthon.seasonthon.domain.stepCalendar.service;

import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.stepCalendar.domain.StepCalendarTodoStep;
import com._oormthon.seasonthon.domain.stepCalendar.repository.StepCalendarRepository;
import com._oormthon.seasonthon.domain.stepCalendar.repository.StepCalendarTodoStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepCalendarQueryService {

    private final StepCalendarRepository stepCalendarRepository;
    private final StepCalendarTodoStepRepository stepCalendarTodoStepRepository;

    public void deleteByTodoSteps(List<TodoStep> todoStepList) {
        List<StepCalendarTodoStep> stepCalendarTodoSteps =
                stepCalendarTodoStepRepository.findByTodoStepIdIn(todoStepList.stream().map(TodoStep::getId).toList());

        stepCalendarTodoStepRepository.deleteAll(stepCalendarTodoSteps);
    }

    public Long findStepCalendarIdByUserIdAndTodoStepId(Long userId, Long todoStepId) {
        return stepCalendarTodoStepRepository.findStepCalendarIdByUserIdAndTodoStepId(userId, todoStepId);
    }

    public LocalDate findStepCalendarDateById(Long stepCalendarId) {
        return stepCalendarRepository.findCalendarDateById(stepCalendarId);
    }
}
