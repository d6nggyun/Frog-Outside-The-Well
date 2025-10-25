package com._oormthon.seasonthon.domain.stepCalendar.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "step_calendar",
        uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "calendar_date"})
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
    private Integer percentage;

    @Builder
    private StepCalendar(Long userId, LocalDate calendarDate) {
        this.userId = userId;
        this.calendarDate = calendarDate;
        this.percentage = 0;
    }

    public void updatePercentage(Integer percentage) {
        this.percentage = percentage;
    }
}
