package com._oormthon.seasonthon.domain.member.repository;

import com._oormthon.seasonthon.domain.member.entity.DailyLogAfter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;

public interface DailyLogAfterRepository extends JpaRepository<DailyLogAfter, Long> {
    List<DailyLogAfter> findByUserIdAndCreatedAtBetween(Long userId, LocalDate start, LocalDate end);
}
