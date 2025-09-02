package com._oormthon.seasonthon.domain.todo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
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
}
