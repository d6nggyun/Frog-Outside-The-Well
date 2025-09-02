package com._oormthon.seasonthon.domain.todo.repository;

import com._oormthon.seasonthon.domain.todo.domain.TodoStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoStepRepository extends JpaRepository<TodoStep, Long> {

    List<TodoStep> findByTodoId(Long todoId);
}
