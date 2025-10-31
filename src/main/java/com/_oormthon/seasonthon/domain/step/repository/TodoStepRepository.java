package com._oormthon.seasonthon.domain.step.repository;

import com._oormthon.seasonthon.domain.step.domain.TodoStep;
import com._oormthon.seasonthon.domain.step.dto.res.StepResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TodoStepRepository extends JpaRepository<TodoStep, Long> {

    List<TodoStep> findByTodoId(Long todoId);

    boolean existsByIdAndUserId(Long id, Long userId);

    List<TodoStep> findAllByStepDateAndUserId(LocalDate stepDate, Long userId);

    List<TodoStep> findAllByTodoId(Long todoId);

    @Query("""
                SELECT new com._oormthon.seasonthon.domain.step.dto.res.StepResponse(
                    ts.id,
                    ts.stepDate,
                    ts.description,
                    ts.isCompleted
                )
                FROM TodoStep ts
                WHERE ts.userId = :userId AND ts.stepDate = :stepDate
            """)
    List<StepResponse> findAllStepResponseByUserIdAndStepDate(Long userId, LocalDate stepDate);

    @Query("""
    SELECT new com._oormthon.seasonthon.domain.step.dto.res.StepResponse(
        ts.id,
        ts.stepDate,
        ts.description,
        ts.isCompleted
    )
    FROM TodoStep ts
    WHERE ts.userId = :userId
    AND (
        (ts.stepDate < :stepDate AND ts.isCompleted = false)
        OR (ts.stepDate = :stepDate AND ts.isCompleted = true)
        )
""")
    List<StepResponse> findAllMissedStepResponseByUserIdAndStepDate(Long userId, LocalDate stepDate);

    @Query("""
    SELECT ts
    FROM TodoStep ts
    WHERE ts.userId = :userId
      AND ts.stepDate BETWEEN :monthStart AND :monthEnd
""")
    List<TodoStep> findAllTodoStepOverlappingPeriod(Long userId, LocalDate monthStart, LocalDate monthEnd);

    @Query("""
                SELECT new com._oormthon.seasonthon.domain.step.dto.res.StepResponse(
                    ts.id,
                    ts.stepDate,
                    ts.description,
                    ts.isCompleted
                )
                FROM TodoStep ts
                WHERE ts.userId = :userId AND ts.todoId =:todoId AND ts.stepDate = :stepDate
            """)
    List<StepResponse> findAllStepResponseByUserIdAndStepDateAndTodoId(Long userId, Long todoId, LocalDate stepDate);

    @Query("""
                SELECT new com._oormthon.seasonthon.domain.step.dto.res.StepResponse(
                    ts.id,
                    ts.stepDate,
                    ts.description,
                    ts.isCompleted
                )
                FROM TodoStep ts
                WHERE ts.userId = :userId
                AND ts.todoId =:todoId
                    AND (
                        (ts.stepDate < :stepDate AND ts.isCompleted = false)
                        OR (ts.stepDate = :stepDate AND ts.isCompleted = true)
                        )
            """)
    List<StepResponse> findAllMissedStepResponseByUserIdAndStepDateAndTodoId(Long userId,
            Long todoId, LocalDate stepDate);

    int countByUserIdAndStepDate(Long userId, LocalDate stepDate);

    int countByUserIdAndIsCompletedTrueAndStepDate(Long userId, LocalDate stepDate);
}
