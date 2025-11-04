package com._oormthon.seasonthon.domain.todo.repository;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.enums.TodoType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

  Page<Todo> findByUserId(Long userId, Pageable pageable);

  Optional<Todo> findByUserIdAndTitle(Long userId, String title);

  boolean existsByIdAndUserId(Long id, Long userId);

  @Query("""
          SELECT t
          FROM Todo t
          WHERE t.userId = :userId
            AND t.todoType = :todoType
            AND t.startDate <= :endDate
            AND t.endDate >= :startDate
            AND t.isCompleted = true
      """)
  List<Todo> findAllTodoByTodoTypeOverlappingPeriod(Long userId, TodoType todoType, LocalDate startDate,
      LocalDate endDate);

  @Query("""
          SELECT t
          FROM Todo t
          WHERE t.userId = :userId
            AND t.endDate BETWEEN :monthStart AND :monthEnd
            AND t.isCompleted = true
      """)
  List<Todo> findAllTodoOverlappingPeriod(Long userId, LocalDate monthStart, LocalDate monthEnd);
}
