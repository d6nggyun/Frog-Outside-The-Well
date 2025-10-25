package com._oormthon.seasonthon.domain.stepCalendar.repository;

import com._oormthon.seasonthon.domain.stepCalendar.domain.StepCalendarTodoStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StepCalendarTodoStepRepository extends JpaRepository<StepCalendarTodoStep, Long> {

    List<StepCalendarTodoStep> findAllByStepCalendarId(Long stepCalendarId);

    boolean existsByStepCalendarIdAndTodoStepId(Long stepCalendarId, Long todoStepId);

    List<StepCalendarTodoStep> findByTodoStepIdIn(Collection<Long> todoStepIds);
}
