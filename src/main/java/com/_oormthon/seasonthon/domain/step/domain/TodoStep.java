package com._oormthon.seasonthon.domain.step.domain;

import com._oormthon.seasonthon.domain.step.dto.req.StepRequest;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequestId;
import com._oormthon.seasonthon.domain.todo.enums.Day;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "todo_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TodoStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "todo_id", nullable = false)
    private Long todoId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "step_date", nullable = false)
    private LocalDate stepDate;

    @Column(name = "day", nullable = false)
    private Day day;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(name = "is_paused", nullable = false)
    private boolean isPaused = false;

    @Column(name = "is_completed_on_time", nullable = false)
    private boolean isCompletedOnTime = false;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "total_duration")
    private long totalDuration = 0L;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT", name = "tips")
    @Convert(converter = StringListConverter.class)
    private List<String> tips;

    @Builder
    private TodoStep(Long todoId, Long userId, LocalDate stepDate, Day day, String description, List<String> tips) {
        this.todoId = todoId;
        this.userId = userId;
        this.stepDate = stepDate;
        this.day = day;
        this.description = description;
        this.tips = tips;
    }

    public static TodoStep createTodoStep(Long todoId, Long userId, StepRequest stepRequest) {
        return TodoStep.builder()
                .todoId(todoId)
                .userId(userId)
                .stepDate(stepRequest.stepDate())
                .description(stepRequest.description())
                .tips(stepRequest.tips())
                .build();
    }

    public void updateStep(UpdateStepRequest updateStepRequest) {
        this.description = updateStepRequest.description();
    }

    public void updateStepwithId(UpdateStepRequestId updateStepRequestId) {
        this.description = updateStepRequestId.description();
    }

    public void completeStep(LocalDateTime endTime, boolean isOnTime) {
        this.completedDate = LocalDate.from(endTime);
        this.isCompleted = true;
        this.isPaused = false;
        this.isCompletedOnTime = isOnTime;
    }

    public void pauseStep() {
        this.isPaused = true;
    }

    public void updateTotalDuration(Long duration) {
        if (duration == null || duration < 0) {
            throw new CustomException(ErrorCode.STEP_INVALID_DURATION);
        }
        this.totalDuration += duration;
    }
}
