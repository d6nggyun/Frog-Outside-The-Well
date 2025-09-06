package com._oormthon.seasonthon.domain.step.service;

import com._oormthon.seasonthon.domain.StepCalendar.domain.StepCalendar;
import com._oormthon.seasonthon.domain.StepCalendar.service.StepCalendarQueryService;
import com._oormthon.seasonthon.domain.StepCalendar.service.StepCalendarService;
import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.step.domain.StepRecord;
import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequestId;
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
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        completeStep(todoStep);
        StepCalendar stepCalendar = stepCalendarService.saveStepCalendar(user.getUserId(), LocalDate.now());
        stepCalendarService.saveStepCalendarTodoStep(stepCalendar.getId(), stepId);

        List<TodoStep> todoSteps = stepQueryService.findAllByStepDateAndUserId(LocalDate.now(), user.getUserId());
        Boolean isCompletedTodaySteps = todoSteps.stream().allMatch(TodoStep::getIsCompleted);

        return StepRecordResponse.from(startOrGetStepRecord(todoStep.getUserId(), stepId), todo.getProgress(), isCompletedTodaySteps);
    }

    @Transactional
    public StepRecordResponse stopStep(User user, Long stepId) {
        TodoStep todoStep = stepQueryService.getTodoStepById(stepId);
        stepQueryService.validateStepOwnership(user.getUserId(), stepId);

        StepRecord stepRecord = stepQueryService.getStepRecordByStepId(stepId);

        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());

        stepRecord.stopStep();

        List<TodoStep> todoSteps = stepQueryService.findAllByStepDateAndUserId(LocalDate.now(), user.getUserId());
        Boolean isCompletedTodaySteps = todoSteps.stream().allMatch(TodoStep::getIsCompleted);

        return StepRecordResponse.from(stepRecord, todo.getProgress(), isCompletedTodaySteps);
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
    public List<StepResponse> updateSteps(User user, Long todoId, List<UpdateStepRequestId> updateStepRequestIdList) {
        // 1) 입력 검사
        if (updateStepRequestIdList == null || updateStepRequestIdList.isEmpty()) {
            log.warn("updateSteps called with empty request by user {}", user.getUserId());
            throw new CustomException(ErrorCode.STEP_NOT_FOUND);
        }

        // 2) id 목록 수집
        List<Long> ids = updateStepRequestIdList.stream()
                .map(UpdateStepRequestId::stepId)
                .toList();

        // 3) TodoId + UserId 기반으로 Step 조회 → 다른 Todo/사용자의 Step은 애초에 안 불러옴
        List<TodoStep> steps = todoStepRepository.findByTodoId(todoId);

        // 4) 존재 여부 확인
        if (steps.size() != ids.size()) {
            Set<Long> found = steps.stream().map(TodoStep::getId).collect(Collectors.toSet());
            List<Long> missing = ids.stream().filter(id -> !found.contains(id)).toList();
            log.warn("updateSteps - missing step ids {} requested by user {}", missing, user.getUserId());
            throw new CustomException(ErrorCode.STEP_NOT_FOUND);
        }

        // 5) id -> entity 맵 생성
        Map<Long, TodoStep> stepMap = steps.stream()
                .collect(Collectors.toMap(TodoStep::getId, Function.identity()));

        // 6) 순서대로 업데이트
        for (UpdateStepRequestId usri : updateStepRequestIdList) {
            TodoStep step = stepMap.get(usri.stepId());
            step.updateStepwithId(usri); // 내부 validation 필요
        }

        // 7) Dirty Checking으로 flush (명시적으로 saveAll 해도 무방)
        todoStepRepository.saveAll(steps);

        // 8) Todo 조회
        Todo todo = todoQueryService.getTodoById(todoId);

        // 9) 응답 생성
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
        if (todoStep.isCompleted())
            return;
        todoStep.completeStep();

        Todo todo = todoQueryService.getTodoById(todoStep.getTodoId());
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());

        long completedStepsCount = todoSteps.stream().filter(TodoStep::isCompleted).count();
        int progress = (int) ((completedStepsCount * 100) / todoSteps.size());

        todo.updateProgress(progress);
        if (progress == 100) todo.completeTodo();
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

        return todoSteps.stream().map(todoStep -> StepResponse.from(todo, todoStep)).toList();
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
