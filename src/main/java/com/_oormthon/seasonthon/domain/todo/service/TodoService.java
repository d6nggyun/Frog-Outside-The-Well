package com._oormthon.seasonthon.domain.todo.service;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.domain.TodoStep;
import com._oormthon.seasonthon.domain.todo.dto.req.StepRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com._oormthon.seasonthon.domain.todo.repository.TodoStepRepository;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import com._oormthon.seasonthon.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoStepRepository todoStepRepository;

    @Transactional(readOnly = true)
    public PageResponse<TodoResponse> findTodos(User user) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("endDate").ascending());
        Page<Todo> todos = todoRepository.findByUserId(user.getUserId(), pageable);
        String warmMessage = "힘내세요!"; // ex

        List<TodoResponse> todoResponses = todos.stream()
                .map(todo -> {
                    List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());
                    List<StepResponse> stepResponses = todoSteps.stream()
                            .map(StepResponse::from)
                            .toList();

                    return TodoResponse.from(todo, warmMessage, stepResponses);
                }).toList();

        return PageResponse.from(todos.getTotalElements(), todos.getTotalPages(), todoResponses);
    }

    @Transactional(readOnly = true)
    public TodoStepResponse getTodoSteps(Long todoId) {
        Todo todo = getTodoById(todoId);
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todoId);

        return TodoStepResponse.from(todo, "개구리가 햇빛을 보기 시작했어요!", todoSteps.stream().map(StepResponse::from).toList());
    }

    @Transactional
    public TodoResponse addTodo(User user, TodoRequest todoRequest) {
        Todo todo = Todo.createTodo(user, todoRequest);
        todoRepository.save(todo);

        List<TodoStep> todoStepList = getAndSaveTodoStep(todo.getId(), todoRequest.todoSteps());
        List<StepResponse> stepResponses = getStepResponses(todoStepList);

        return TodoResponse.from(todo, "개구리가 햇빛을 보기 시작했어요!", stepResponses);
    }

    @Transactional
    public TodoResponse updateTodo(User user, Long todoId, UpdateTodoRequest updateTodoRequest) {
        validateUser(user.getUserId(), todoId);
        Todo todo = getTodoById(todoId);
        todo.updateTodo(updateTodoRequest);

        todoStepRepository.deleteAll(todoStepRepository.findByTodoId(todoId));
        List<TodoStep> todoStepList = getAndSaveTodoStep(todo.getId(), updateTodoRequest.todoSteps());
        List<StepResponse> stepResponses = getStepResponses(todoStepList);

        return TodoResponse.from(todo, "개구리가 햇빛을 보기 시작했어요!", stepResponses);
    }

    @Transactional(readOnly = true)
    public Object findTodoCalendar() {

        return null;
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

    private Todo getTodoById(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> {
                    log.warn("[ToDo 조회 실패] 존재하지 않는 ToDo Id: {}", todoId);
                    return new CustomException(ErrorCode.TODO_NOT_FOUND);
                });
    }

    private TodoStep getTodoStepById(Long stepId) {
        return todoStepRepository.findById(stepId)
                .orElseThrow(() -> {
                    log.warn("[Step 조회 실패] 존재하지 않는 stepId Id: {}", stepId);
                    return new CustomException(ErrorCode.STEP_NOT_FOUND);
                });
    }

    private void validateUser(Long userId, Long todoId) {
        if (!todoRepository.existsByIdAndUserId(todoId, userId)) {
            log.warn("[ToDo 수정 실패] ToDo Id: {}, User Id: {} - 권한 없음", todoId, userId);
            throw new CustomException(ErrorCode.TODO_ACCESS_DENIED);
        }
    }

    private List<TodoStep> getAndSaveTodoStep(Long todoId, List<StepRequest> stepList) {
        return todoStepRepository.saveAll(stepList.stream()
                .map(stepRequest -> TodoStep.createTodoStep(todoId, stepRequest)).toList()
        );
    }

    private List<StepResponse> getStepResponses(List<TodoStep> todoStepList) {
        return todoStepList.stream().map(StepResponse::from).toList();
    }

    private List<StepResponse> newTodoStepResponse(Todo todo) {
        List<TodoStep> todoSteps = todoStepRepository.findByTodoId(todo.getId());
        return todoSteps.stream().map(StepResponse::from).toList();
    }
}
