package com._oormthon.seasonthon.domain.step.service;

import com._oormthon.seasonthon.domain.StepCalendar.domain.StepCalendar;
import com._oormthon.seasonthon.domain.StepCalendar.repository.StepCalendarRepository;
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
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
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

    private final TodoRepository todoRepository;
    private final TodoStepRepository todoStepRepository;
    private final StepRecordRepository stepRecordRepository;
    private final StepCalendarRepository stepCalendarRepository;

    @Transactional(readOnly = true)
    public TodoStepResponse getTodoSteps(User user, Long todoId) {
        validateTodoAndUser(user.getUserId(), todoId);
        Todo todo = getTodoById(todoId);
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todoId);

        return TodoStepResponse.from(todo, "개구리가 햇빛을 보기 시작했어요!", todoSteps.stream().map(StepResponse::from).toList());
    }

    @Transactional
    public StepRecordResponse startStep(User user, Long stepId) {
        validateStepAndUser(user.getUserId(), stepId);
        TodoStep todoStep = getTodoStepById(stepId);

        completeStep(todoStep);
        todoStep.incrementCount();
        saveStepCalendar(user.getUserId(), LocalDate.now());

        return StepRecordResponse.from(StepRecord.startStep(stepId, todoStep.getUserId()));
    }

    @Transactional
    public StepRecordResponse stopStep(User user, Long stepId) {
        validateStepAndUser(user.getUserId(), stepId);
        TodoStep todoStep = getTodoStepById(stepId);
        StepRecord stepRecord = getStepRecordByStepId(stepId);

        stepRecord.stopStep();

        return StepRecordResponse.from(stepRecord);
    }

    @Transactional
    public List<StepResponse> updateStep(User user, Long stepId, UpdateStepRequest updateStepRequest) {
        validateStepAndUser(user.getUserId(), stepId);
        TodoStep todoStep = getTodoStepById(stepId);
        todoStep.updateStep(updateStepRequest);

        Todo todo = getTodoById(todoStep.getTodoId());

        return newTodoStepResponse(todo);
    }

    @Transactional
    public List<StepResponse> deleteStep(User user, Long stepId) {
        validateStepAndUser(user.getUserId(), stepId);
        TodoStep todoStep = getTodoStepById(stepId);
        Todo todo = getTodoById(todoStep.getTodoId());
        todoStepRepository.deleteById(stepId);

        return newTodoStepResponse(todo);
    }

    private TodoStep getTodoStepById(Long stepId) {
        return todoStepRepository.findById(stepId)
                .orElseThrow(() -> {
                    log.warn("[Step 조회 실패] 존재하지 않는 stepId Id: {}", stepId);
                    return new CustomException(ErrorCode.STEP_NOT_FOUND);
                });
    }

    private Todo getTodoById(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> {
                    log.warn("[ToDo 조회 실패] 존재하지 않는 ToDo Id: {}", todoId);
                    return new CustomException(ErrorCode.TODO_NOT_FOUND);
                });
    }

    private StepRecord getStepRecordByStepId(Long stepId) {
        return stepRecordRepository.findByStepId(stepId)
                .orElseThrow(() -> {
                    log.warn("[StepRecord 조회 실패] 존재하지 않는 stepId Id: {}", stepId);
                    return new CustomException(ErrorCode.STEP_RECORD_NOT_FOUND);
                });
    }

    private void validateStepAndUser(Long userId, Long stepId) {
        if (!todoStepRepository.existsByIdAndUserId(stepId, userId)) {
            log.warn("[Step 작업 실패] Step Id: {}, User Id: {} - 권한 없음", stepId, userId);
            throw new CustomException(ErrorCode.STEP_ACCESS_DENIED);
        }
    }

    private void validateTodoAndUser(Long userId, Long todoId) {
        if (!todoRepository.existsByIdAndUserId(todoId, userId)) {
            log.warn("[ToDo 수정 실패] ToDo Id: {}, User Id: {} - 권한 없음", todoId, userId);
            throw new CustomException(ErrorCode.TODO_ACCESS_DENIED);
        }
    }

    private void completeStep(TodoStep todoStep) {
        if (todoStep.isCompleted()) return;
        todoStep.completeStep();

        Todo todo = getTodoById(todoStep.getTodoId());
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());

        long completedStepsCount = todoSteps.stream().filter(TodoStep::isCompleted).count();
        int progress = (int) ((completedStepsCount * 100) / todoSteps.size());
        todo.updateProgress(progress);
    }

    private List<StepResponse> newTodoStepResponse(Todo todo) {
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());
        return todoSteps.stream().map(com._oormthon.seasonthon.domain.step.dto.res.StepResponse::from).toList();
    }

    private void saveStepCalendar(Long userId, LocalDate date) {
        StepCalendar stepCalendar = stepCalendarRepository.findByUserIdAndCalendarDate(userId, date)
                .orElseGet(() -> {
                    StepCalendar newStepCalendar = StepCalendar.builder()
                            .userId(userId)
                            .calendarDate(date)
                            .build();
                    return stepCalendarRepository.save(newStepCalendar);
                });
        stepCalendar.incrementCount();
    }
}
