package com._oormthon.seasonthon.domain.StepCalendar.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "step_calendar",
        uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "step_date"})
       }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StepCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "calendar_date", nullable = false)
    private LocalDate calendarDate;

    @Column(nullable = false)
    private Integer count = 0;

    @Builder
    private StepCalendar(Long userId, LocalDate calendarDate) {
        this.userId = userId;
        this.calendarDate = calendarDate;
    }

    public void incrementCount() {
        this.count++;
    }
}
