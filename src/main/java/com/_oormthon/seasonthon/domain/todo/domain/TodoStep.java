package com._oormthon.seasonthon.domain.todo.domain;

import com._oormthon.seasonthon.domain.todo.dto.req.StepRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateStepRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "todo_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "todo_id", nullable = false)
    private Long todoId;

    @Column(nullable = false)
    private LocalDate stepDate;

    @Column(nullable = false)
    private Integer stepOrder;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isCompleted;

    @Builder
    private TodoStep(Long todoId, LocalDate stepDate, Integer stepOrder, String description, Boolean isCompleted) {
        this.todoId = todoId;
        this.stepDate = stepDate;
        this.stepOrder = stepOrder;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    public static TodoStep createTodoStep(Long todoId, StepRequest stepRequest) {
        return TodoStep.builder()
                .todoId(todoId)
                .stepDate(stepRequest.stepDate())
                .stepOrder(stepRequest.stepOrder())
                .description(stepRequest.description())
                .isCompleted(false)
                .build();
    }

    public void updateStep(UpdateStepRequest updateStepRequest) {
        this.stepOrder = updateStepRequest.stepOrder();
        this.description = updateStepRequest.description();
    }
}
