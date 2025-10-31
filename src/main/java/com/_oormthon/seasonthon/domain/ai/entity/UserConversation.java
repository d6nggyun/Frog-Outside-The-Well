package com._oormthon.seasonthon.domain.ai.entity;

import java.time.LocalDate;

import com._oormthon.seasonthon.domain.ai.enums.ConversationState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_age")
    private Integer userAge;

    private String currentStep;

    @Enumerated(EnumType.STRING)
    private ConversationState state = ConversationState.ACTIVE;

    private String currentGoal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String studyDays;

    private String aiPlan;

    private int dailyMinutes;

    @Version
    private Long version;
}
