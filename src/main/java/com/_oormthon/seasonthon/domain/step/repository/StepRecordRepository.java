package com._oormthon.seasonthon.domain.step.repository;

import com._oormthon.seasonthon.domain.step.domain.StepRecord;
import com._oormthon.seasonthon.domain.step.domain.TodoDurationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StepRecordRepository extends JpaRepository<StepRecord, Long> {

    Optional<StepRecord> findByStepId(Long stepId);
    Optional<StepRecord> findByUserIdAndStepId(Long userId, Long stepId);

    @Query("""
    select t.id as todoId,
           t.title as todoTitle,
           coalesce(sum(sr.duration), 0) as totalDuration
    from TodoStep ts
    join Todo t on t.id = ts.todoId
    left join StepRecord sr on sr.stepId = ts.id
    where ts.userId = :userId
      and ts.stepDate = :date
    group by t.id, t.title
""")
    List<TodoDurationGroup> findTodoDurationGroup(@Param("userId") Long userId,
                                                @Param("date") LocalDate date);

    List<StepRecord> findAllByUserIdAndCreatedAt(Long userId, LocalDate createdAt);
}
