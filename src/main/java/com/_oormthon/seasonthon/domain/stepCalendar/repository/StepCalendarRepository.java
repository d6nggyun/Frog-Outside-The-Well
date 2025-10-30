package com._oormthon.seasonthon.domain.stepCalendar.repository;

import com._oormthon.seasonthon.domain.stepCalendar.domain.StepCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StepCalendarRepository extends JpaRepository<StepCalendar, Long> {

    List<StepCalendar> findAllByUserIdAndCalendarDateBetween(Long userId, LocalDate calendarDateAfter, LocalDate calendarDateBefore);
    Optional<StepCalendar> findByUserIdAndCalendarDate(Long userId, LocalDate calendarDate);

    @Query("SELECT s.calendarDate FROM StepCalendar s WHERE s.id = :id")
    LocalDate findCalendarDateById(Long id);
}
