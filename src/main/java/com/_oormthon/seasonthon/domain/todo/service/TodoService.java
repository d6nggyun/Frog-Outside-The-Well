package com._oormthon.seasonthon.domain.todo.service;

import com._oormthon.seasonthon.domain.member.domain.Member;
import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;

    @Transactional(readOnly = true)
    public List<TodoResponse> findTodos(Member member) {
        List<Todo> todos = todoRepository.findByMemberId(member.getId());

        return todos.stream().map(TodoResponse::from).toList();
    }

    public Object getTodoSteps() {


    }

    public Object addTodo() {
    }

    public Object updateTodo() {


    }

    public Object addEmotion() {


    }

    public Object findTodoCalendar() {


    }


}
