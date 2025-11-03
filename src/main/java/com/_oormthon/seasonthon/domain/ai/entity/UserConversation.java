package com._oormthon.seasonthon.domain.ai.entity;

import java.time.LocalDate;
import java.util.List;

import com._oormthon.seasonthon.domain.ai.enums.ConversationState;
import com._oormthon.seasonthon.domain.todo.enums.Day;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_conversation")
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

    @Enumerated(EnumType.STRING)
    private ConversationState state;

    private String title;
    private String content;

    @Column(name = "todo_id", nullable = false)
    private Long todoId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private String studyDays;

    @ElementCollection(targetClass = Day.class)
    @CollectionTable(name = "todo_days", joinColumns = @JoinColumn(name = "todo_id"))
    @Enumerated(EnumType.STRING)
    private List<Day> expectedDays;

    private Integer dailyMinutes;

    private boolean planSaved = false; // 기본 false

    @Version
    private Long version;
}
