package com._oormthon.seasonthon.domain.dailyLog.repository;

import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogBefore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogBeforeRepository extends JpaRepository<DailyLogBefore, Long> {
    List<DailyLogBefore> findByUserIdAndCreatedAtBetween(Long userId, LocalDate start, LocalDate end);

    Optional<DailyLogBefore> findByUserIdAndCreatedAt(Long userId, LocalDate createdAt);
}
