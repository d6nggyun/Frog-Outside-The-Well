package com._oormthon.seasonthon.domain.step.service;

import com._oormthon.seasonthon.domain.StepCalendar.domain.StepCalendar;
import com._oormthon.seasonthon.domain.StepCalendar.service.StepCalendarQueryService;
import com._oormthon.seasonthon.domain.StepCalendar.service.StepCalendarService;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.StepRecord;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.res.StepRecordResponse;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.step.repository.StepRecordRepository;
import com._oormthon.seasonthon.domain.step.repository.TodoStepRepository;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.enums.TodoText;
import com._oormthon.seasonthon.domain.todo.service.TodoQueryService;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepService {

    private final TodoStepRepository todoStepRepository;
    private final StepRecordRepository stepRecordRepository;
    private final StepQueryService stepQueryService;
    private final TodoQueryService todoQueryService;
    private final StepCalendarService stepCalendarService;
    private final StepCalendarQueryService stepCalendarQueryService;

    @Transactional(readOnly = true)
    public TodoStepResponse getTodoSteps(User user, Long todoId) {
        todoQueryService.getTodoById(todoId);
        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        Todo todo = todoQueryService.getTodoById(todoId);
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todoId);
        String progressText = createProgressText(todo.getProgress());

        return TodoStepResponse.from(todo, progressText,
                todoSteps.stream().map(todoStep ->  StepResponse.from(todo, todoStep)).toList());
    }

    @Transactional
    public StepRecordResponse startStep(User user, Long stepId) {
        stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);

        completeStep(todoStep);
        StepCalendar stepCalendar = stepCalendarService.saveStepCalendar(user.getUserId(), LocalDate.now());
        stepCalendarService.saveStepCalendarTodoStep(stepCalendar.getId(), stepId);

        return StepRecordResponse.from(startOrGetStepRecord(todoStep.getUserId(), stepId));
    }

    @Transactional
    public StepRecordResponse stopStep(User user, Long stepId) {
        stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        StepRecord stepRecord = stepQueryService.getStepRecordByStepId(stepId);

        stepRecord.stopStep();

        return StepRecordResponse.from(stepRecord);
    }

    @Transactional
    public List<StepResponse> updateStep(User user, Long stepId, UpdateStepRequest updateStepRequest) {
        stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        todoStep.updateStep(updateStepRequest);
        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        return newTodoStepResponse(todo);
    }

    @Transactional
    public List<StepResponse> deleteStep(User user, Long stepId) {
        stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        stepCalendarQueryService.deleteByTodoSteps(List.of(todoStep));

        todoStepRepository.deleteById(stepId);

        return newTodoStepResponse(todo);
    }

    private void completeStep(TodoStep todoStep) {
        if (todoStep.isCompleted()) return;
        todoStep.completeStep();

        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());

        long completedStepsCount = todoSteps.stream().filter(TodoStep::isCompleted).count();
        int progress = (int) ((completedStepsCount * 100) / todoSteps.size());

        todo.updateProgress(progress);
    }

    private StepRecord startOrGetStepRecord(Long userId, Long stepId) {
        return stepRecordRepository.findByUserIdAndStepId(userId, stepId)
                .map(existingRecord -> {
                    existingRecord.startStep(stepId, userId);

                    return existingRecord;
                })
                .orElseGet(() -> stepRecordRepository.save(StepRecord.createStepRecord(stepId, userId)));
    }

    private List<StepResponse> newTodoStepResponse(Todo todo) {
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());

        return todoSteps.stream().map(todoStep ->StepResponse.from(todo, todoStep)).toList();
    }

    private String createProgressText(Integer progress) {
        if (progress == 100) return TodoText.PROGRESS_100.getText();
        else if (progress > 80) return TodoText.PROGRESS_80.getText();
        else if (progress > 50) return TodoText.PROGRESS_50.getText();
        else if (progress > 20) return TodoText.PROGRESS_20.getText();
        else if (progress >= 0) return TodoText.PROGRESS_0.getText();
        else throw new CustomException(ErrorCode.TODO_PROGRESS_NOT_VALID);
    }
}
