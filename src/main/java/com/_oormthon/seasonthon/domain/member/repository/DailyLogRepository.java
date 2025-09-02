package com._oormthon.seasonthon.domain.member.repository;

import com._oormthon.seasonthon.domain.member.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {
    Optional<DailyLog> findByMember_MemberIdAndLogDate(Long memberId, LocalDate logDate);

    List<DailyLog> findByMember_MemberIdAndLogDateBetween(Long memberId, LocalDate start, LocalDate end);

    boolean existsByMember_MemberIdAndLogDate(Long memberId, LocalDate logDate);
}