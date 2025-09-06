package com._oormthon.seasonthon.domain.member.repository;

import com._oormthon.seasonthon.domain.member.entity.DailyLogBefore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogBeforeRepository extends JpaRepository<DailyLogBefore, Long> {
    List<DailyLogBefore> findByUserIdAndCreatedAtBetween(Long userId, LocalDate start, LocalDate end);
}
