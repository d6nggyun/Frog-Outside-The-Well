package com._oormthon.seasonthon.domain.todo.service;

import com._oormthon.seasonthon.domain.member.domain.Member;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.domain.TodoStep;
import com._oormthon.seasonthon.domain.todo.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com._oormthon.seasonthon.domain.todo.repository.TodoStepRepository;
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
    public Object getTodoSteps() {


    }

    @Transactional
    public Object addTodo() {
    }

    @Transactional
    public Object updateTodo() {


    }

    @Transactional
    public Object addEmotion() {


    }

    @Transactional(readOnly = true)
    public Object findTodoCalendar() {


    }
}