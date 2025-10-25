package com._oormthon.seasonthon.domain.stepCalendar.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "step_calendar_todo_step")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StepCalendarTodoStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_calendar_id", nullable = false)
    private Long stepCalendarId;

    @Column(name = "todo_step_id", nullable = false)
    private Long todoStepId;

    @Builder
    private StepCalendarTodoStep(Long stepCalendarId, Long todoStepId) {
        this.stepCalendarId = stepCalendarId;
        this.todoStepId = todoStepId;
    }
}
