package com._oormthon.seasonthon.domain.stepRecord.service;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.domain.step.service.StepQueryService;
import com._oormthon.seasonthon.domain.stepCalendar.domain.StepCalendar;
import com._oormthon.seasonthon.domain.stepCalendar.service.StepCalendarService;
import com._oormthon.seasonthon.domain.stepRecord.domain.StepRecord;
import com._oormthon.seasonthon.domain.stepRecord.dto.req.StepStartRequest;
import com._oormthon.seasonthon.domain.stepRecord.dto.req.StepStopRequest;
import com._oormthon.seasonthon.domain.stepRecord.dto.res.StepRecordResponse;
import com._oormthon.seasonthon.domain.stepRecord.repository.StepRecordRepository;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.service.TodoQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepRecordService {

    private final StepRecordRepository stepRecordRepository;
    private final TodoStepRepository todoStepRepository;
    private final StepRecordQueryService stepRecordQueryService;
    private final StepQueryService stepQueryService;
    private final TodoQueryService todoQueryService;
    private final StepCalendarService stepCalendarService;

    @Transactional
    public StepRecordResponse startStep(User user, Long stepId, StepStartRequest request) {
        stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        Boolean isCompletedTodaySteps = checkAllStepsCompletedToday(user);

        return StepRecordResponse.of(startOrGetStepRecord(todoStep.getUserId(), stepId, request.startTime()), todo.getProgress(), isCompletedTodaySteps);
    }

    @Transactional
    public StepRecordResponse pauseStep(User user, Long stepId, StepStopRequest request) {
        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        StepRecord stepRecord = stepRecordQueryService.getStepRecordByUserIdAndStepId(user.getUserId(), stepId);

        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        stepRecord.pauseStep(request.endTime(), request.duration());
        todoStep.updateTotalDuration(request.duration());

        Boolean isCompletedTodaySteps = checkAllStepsCompletedToday(user);

        return StepRecordResponse.of(stepRecord, todo.getProgress(), isCompletedTodaySteps);
    }

    @Transactional
    public StepRecordResponse stopStep(User user, Long stepId, StepStopRequest request) {
        Long userId = user.getUserId();
        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(userId, stepId);

        StepRecord stepRecord = stepRecordQueryService.getStepRecordByUserIdAndStepId(userId, stepId);

        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        stepRecord.stopStep(request.endTime(), request.duration());

        completeStep(todoStep, request.endTime());
        todoStep.updateTotalDuration(request.duration());

        StepCalendar stepCalendar = stepCalendarService.saveAndUpdateStepCalendar(userId, LocalDate.from(request.endTime()));
        stepCalendarService.saveStepCalendarTodoStep(userId, stepCalendar.getId(), stepId);

        Boolean isCompletedTodaySteps = checkAllStepsCompletedToday(user);

        return StepRecordResponse.of(stepRecord, todo.getProgress(), isCompletedTodaySteps);
    }

    private void completeStep(TodoStep todoStep, LocalDateTime endTime) {
        if (todoStep.isCompleted())
            return;
        todoStep.completeStep(endTime);

        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());

        long completedStepsCount = todoSteps.stream().filter(TodoStep::isCompleted).count();
        int progress = (int) ((completedStepsCount * 100) / todoSteps.size());

        todo.updateProgress(progress);
        if (progress == 100) todo.completeTodo();
    }

    private StepRecord startOrGetStepRecord(Long userId, Long stepId, LocalDateTime startTime) {
        return stepRecordRepository.findByUserIdAndStepId(userId, stepId)
                .map(existingRecord -> {
                    existingRecord.startStep(startTime);

                    return existingRecord;
                })
                .orElseGet(() -> stepRecordRepository.save(StepRecord.createStepRecord(stepId, userId, startTime)));
    }

    private Boolean checkAllStepsCompletedToday(User user) {
        List<TodoStep> todoSteps = stepQueryService.findAllByStepDateAndUserId(LocalDate.now(), user.getUserId());
        return todoSteps.stream().allMatch(TodoStep::getIsCompleted);
    }
}
