package com._oormthon.seasonthon.domain.dailyLog.repository;

import com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse;
import com._oormthon.seasonthon.domain.dailyLog.domain.DailyLogAfter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogAfterRepository extends JpaRepository<DailyLogAfter, Long> {
    List<DailyLogAfter> findByUserIdAndCreatedAtBetween(Long userId, LocalDate start, LocalDate end);

    @Query("""
    SELECT new com._oormthon.seasonthon.domain.diary.dto.res.DiaryResponse(d.createdAt, d.mood)
    FROM DailyLogAfter d
    WHERE d.userId = :userId
    AND d.createdAt BETWEEN :startDate AND :endDate
    ORDER BY d.createdAt ASC
""")
    List<DiaryResponse> findAllMoodByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Optional<DailyLogAfter> findByUserIdAndCreatedAt(Long userId, LocalDate createdAt);
}
