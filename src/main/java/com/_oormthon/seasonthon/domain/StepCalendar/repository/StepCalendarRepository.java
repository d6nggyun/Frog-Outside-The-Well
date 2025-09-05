package com._oormthon.seasonthon.domain.StepCalendar.repository;

import com._oormthon.seasonthon.domain.StepCalendar.domain.StepCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StepCalendarRepository extends JpaRepository<StepCalendar, Long> {

    List<StepCalendar> findAllByUserIdAndCalendarDateBetween(Long userId, LocalDate calendarDateAfter, LocalDate calendarDateBefore);
    Optional<StepCalendar> findByUserIdAndCalendarDate(Long userId, LocalDate calendarDate);
}
