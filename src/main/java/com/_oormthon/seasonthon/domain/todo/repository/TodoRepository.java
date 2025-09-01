package com._oormthon.seasonthon.domain.todo.repository;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
}
