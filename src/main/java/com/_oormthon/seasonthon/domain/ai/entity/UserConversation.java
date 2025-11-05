package com._oormthon.seasonthon.domain.ai.entity;

import java.time.LocalDate;
import java.util.List;

import com._oormthon.seasonthon.domain.ai.enums.ConversationState;
import com._oormthon.seasonthon.domain.todo.enums.Day;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_conversation", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")
})
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

    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;

    private String studyDays;

    @ElementCollection(targetClass = Day.class)
    @CollectionTable(name = "todo_days", joinColumns = @JoinColumn(name = "todo_id"))
    @Enumerated(EnumType.STRING)
    private List<Day> expectedDays;

    private Integer dailyMinutes;

    private boolean planSaved; // 실제 DB 저장 여부 (Todo 생성 완료 시 true)

    /**
     * Gemini가 생성한 Todo/Steps JSON을 임시로 보관하는 필드.
     * CONFIRM_PLAN 단계에서 사용자가 "좋아"라고 확정하면 실제 DB에 저장됩니다.
     */
    @Column(columnDefinition = "TEXT")
    private String pendingPlanJson;
}
