package com._oormthon.seasonthon.domain.step.service;

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
import com._oormthon.seasonthon.domain.todo.service.TodoQueryService;
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

    @Transactional(readOnly = true)
    public TodoStepResponse getTodoSteps(User user, Long todoId) {
        todoQueryService.getTodoById(todoId);
        todoQueryService.validateTodoOwnership(user.getUserId(), todoId);

        Todo todo = todoQueryService.getTodoById(todoId);
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todoId);

        return TodoStepResponse.from(todo, "개구리가 햇빛을 보기 시작했어요!", todoSteps.stream().map(StepResponse::from).toList());
    }

    @Transactional
    public StepRecordResponse startStep(User user, Long stepId) {
        stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);

        completeStep(todoStep);
        todoStep.incrementCount();
        stepCalendarService.saveStepCalendar(user.getUserId(), LocalDate.now());

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
                .orElseGet(() -> stepRecordRepository.save(StepRecord.createStepRecord(userId, stepId)));
    }

    private List<StepResponse> newTodoStepResponse(Todo todo) {
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());

        return todoSteps.stream().map(StepResponse::from).toList();
    }
}
