package com._oormthon.seasonthon.domain.stepRecord.repository;

import com._oormthon.seasonthon.domain.step.domain.TodoDurationGroup;
import com._oormthon.seasonthon.domain.stepRecord.domain.StepRecord;
import com._oormthon.seasonthon.domain.stepRecord.dto.res.StepRecordDetailResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StepRecordRepository extends JpaRepository<StepRecord, Long> {

    Optional<StepRecord> findByUserIdAndStepId(Long userId, Long stepId);

    @Query("""
    select t.id as todoId,
           t.title as todoTitle,
           sum(sr.duration) as totalDuration
    from TodoStep ts
    join Todo t on t.id = ts.todoId
    join StepRecord sr on sr.stepId = ts.id
    where ts.userId = :userId
      and ts.stepDate = :date
    group by t.id, t.title
""")
    List<TodoDurationGroup> findTodoDurationGroup(@Param("userId") Long userId,
                                                @Param("date") LocalDate date);

    @Query("""
    SELECT new com._oormthon.seasonthon.domain.stepRecord.dto.res.StepRecordDetailResponse(
        sr.id,
        sr.stepId,
        sr.userId,
        sr.startTime,
        sr.endTime,
        sr.duration
    )
    FROM StepRecord sr
    WHERE sr.userId = :userId AND sr.stepId = :stepId
""")
    Optional<StepRecordDetailResponse> findStepRecordDetailResponseByUserIdAndStepId(Long userId, Long stepId);
}
