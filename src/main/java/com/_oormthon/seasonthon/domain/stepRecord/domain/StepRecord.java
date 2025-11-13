package com._oormthon.seasonthon.domain.stepRecord.domain;

import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "step_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private long duration = 0L;

    @Column(name = "break_count")
    private int breakCount = 0;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Builder
    private StepRecord(Long stepId, Long userId, LocalDateTime startTime, LocalDateTime endTime, Boolean isCompleted) {
        this.stepId = stepId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isCompleted = isCompleted;
    }

    public static StepRecord createStepRecord(Long stepId, Long userId, LocalDateTime startTime) {
        return StepRecord.builder()
                .stepId(stepId)
                .userId(userId)
                .startTime(startTime)
                .endTime(null)
                .isCompleted(false)
                .build();
    }

    public void startStep(LocalDateTime startTime) {
        if (this.startTime == null) {
            this.startTime = startTime;
        }
        this.endTime = null;
    }

    public void stopStep(LocalDateTime endTime, Long duration) {
        if (this.startTime == null) {
            throw new CustomException(ErrorCode.STEP_NOT_STARTED);
        }
        this.endTime = endTime;
        this.duration = this.duration + duration;
        this.isCompleted = true;
    }

    public void pauseStep(LocalDateTime endTime, Long duration) {
        if (this.startTime == null) {
            throw new CustomException(ErrorCode.STEP_NOT_STARTED);
        }
        this.duration = this.duration + duration;
        this.endTime = endTime;
        this.breakCount += 1;
    }
}
