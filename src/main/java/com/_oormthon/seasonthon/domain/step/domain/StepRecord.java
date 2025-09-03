package com._oormthon.seasonthon.domain.step.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "step_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StepRecord {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "todo_id", nullable = false)
    private Long todoId;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private Long duration;
}
