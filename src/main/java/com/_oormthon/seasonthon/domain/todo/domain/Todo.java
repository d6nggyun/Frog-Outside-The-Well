package com._oormthon.seasonthon.domain.todo.domain;

import com._oormthon.seasonthon.domain.todo.enums.Day;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "todo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "expected_date", nullable = false)
    @Enumerated(EnumType.STRING)
    private Day expectedDay;

    @Column(nullable = false)
    private Long progress;

    @Column(nullable = false)
    private LocalDate deadline;
}