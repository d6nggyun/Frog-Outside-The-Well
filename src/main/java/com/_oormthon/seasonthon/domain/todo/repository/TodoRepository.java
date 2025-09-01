package com._oormthon.seasonthon.domain.todo.repository;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByMemberId(Long memberId);
}
