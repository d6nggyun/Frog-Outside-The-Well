package com._oormthon.seasonthon.domain.step.repository;

import com._oormthon.seasonthon.domain.step.domain.StepRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StepRecordRepository extends JpaRepository<StepRecord, Long> {

    Optional<StepRecord> findByStepId(Long stepId);
}
