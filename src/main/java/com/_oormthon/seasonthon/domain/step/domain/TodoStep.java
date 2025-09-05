package com._oormthon.seasonthon.domain.step.domain;

import com._oormthon.seasonthon.domain.step.dto.req.StepRequest;
import com._oormthon.seasonthon.domain.step.dto.req.UpdateStepRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private TodoStep(Long todoId, Long userId, LocalDate stepDate, String description, Boolean isCompleted) {
        this.todoId = todoId;
        this.userId = userId;
        this.stepDate = stepDate;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    public static TodoStep createTodoStep(Long todoId, Long userId, StepRequest stepRequest) {
        return TodoStep.builder()
                .todoId(todoId)
                .userId(userId)
                .stepDate(stepRequest.stepDate())
                .description(stepRequest.description())
                .isCompleted(false)
                .build();
    }

    public void updateStep(UpdateStepRequest updateStepRequest) {
        this.description = updateStepRequest.description();
    }

    public Boolean isCompleted() {
        return this.isCompleted;
    }

    public void completeStep() {
        this.isCompleted = true;
    }
}
