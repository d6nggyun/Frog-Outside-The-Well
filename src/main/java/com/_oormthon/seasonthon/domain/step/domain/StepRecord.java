package com._oormthon.seasonthon.domain.step.domain;

import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "step_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRecord {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private Long duration = 0L;

    @Builder
    private StepRecord(Long stepId, Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        this.stepId = stepId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static StepRecord startStep(Long stepId, Long userId) {
        return StepRecord.builder()
                .stepId(stepId)
                .userId(userId)
                .startTime(LocalDateTime.now())
                .endTime(null)
                .build();
    }

    public void stopStep() {
        if (this.startTime == null) {
            throw new CustomException(ErrorCode.STEP_NOT_STARTED);
        }
        this.endTime = LocalDateTime.now();
        this.duration = this.duration + Duration.between(this.startTime, this.endTime).toSeconds();
    }
}
