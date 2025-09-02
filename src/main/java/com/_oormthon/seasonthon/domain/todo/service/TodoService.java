package com._oormthon.seasonthon.domain.todo.service;

import com._oormthon.seasonthon.domain.member.domain.Member;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.domain.TodoStep;
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
    public PageResponse<TodoResponse> findTodos(Member member) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("endDate").ascending());
        Page<Todo> todos = todoRepository.findByMemberId(member.getId(), pageable);
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
    public Object addTodo() {

        return null;
    }

    @Transactional
    public Object updateTodo() {

        return null;
    }

    @Transactional
    public Object addEmotion() {

        return null;
    }

    @Transactional(readOnly = true)
    public Object findTodoCalendar() {

        return null;
    }

    private Todo getTodoById(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> {
                    log.warn("[ToDo 조회 실패] 존재하지 않는 ToDo Id: {}", todoId);
                    return new CustomException(ErrorCode.TODO_NOT_FOUND);
                });
    }
}