package com._oormthon.seasonthon.domain.step.service;

import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepService {

    private final TodoRepository todoRepository;
    private final TodoStepRepository todoStepRepository;

    @Transactional(readOnly = true)
    public TodoStepResponse getTodoSteps(Long todoId) {
        Todo todo = getTodoById(todoId);
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todoId);

        return TodoStepResponse.from(todo, "개구리가 햇빛을 보기 시작했어요!", todoSteps.stream().map(StepResponse::from).toList());
    }

    @Transactional
    public List<StepResponse> completeStep(Long stepId) {
        TodoStep todoStep = getTodoStepById(stepId);
        todoStep.completeStep();

        Todo todo = getTodoById(todoStep.getTodoId());
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());

        long completedStepsCount = todoSteps.stream().filter(TodoStep::isCompleted).count();
        int progress = (int) ((completedStepsCount * 100) / todoSteps.size());
        todo.updateProgress(progress);

        return newTodoStepResponse(todo);
    }

    @Transactional
    public List<StepResponse> updateStep(Long stepId, UpdateStepRequest updateStepRequest) {
        TodoStep todoStep = getTodoStepById(stepId);
        todoStep.updateStep(updateStepRequest);

        Todo todo = getTodoById(todoStep.getTodoId());

        return newTodoStepResponse(todo);
    }

    @Transactional
    public List<StepResponse> deleteStep(Long stepId) {
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

    private List<StepResponse> newTodoStepResponse(Todo todo) {
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());
        return todoSteps.stream().map(StepResponse::from).toList();
    }
}
