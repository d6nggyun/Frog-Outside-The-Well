package com._oormthon.seasonthon.domain.stepCalendar.repository;

import com._oormthon.seasonthon.domain.stepCalendar.domain.StepCalendarTodoStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface StepCalendarTodoStepRepository extends JpaRepository<StepCalendarTodoStep, Long> {

    List<StepCalendarTodoStep> findAllByStepCalendarId(Long stepCalendarId);

    List<StepCalendarTodoStep> findByTodoStepIdIn(Collection<Long> todoStepIds);

    @Query("""
    SELECT scts.stepCalendarId
    FROM StepCalendarTodoStep scts
    WHERE scts.userId = :userId
      AND scts.todoStepId = :todoStepId
""")
    Long findStepCalendarIdByUserIdAndTodoStepId(Long userId, Long todoStepId);

    boolean existsByUserIdAndStepCalendarIdAndTodoStepId(Long userId, Long stepCalendarId, Long todoStepId);
}
