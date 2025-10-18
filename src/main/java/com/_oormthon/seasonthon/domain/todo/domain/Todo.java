package com._oormthon.seasonthon.domain.todo.domain;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoRequest;
import com._oormthon.seasonthon.domain.todo.enums.Day;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "todo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer progress;

    @Column(nullable = false)
    private Boolean isCompleted;

    @ElementCollection(targetClass = Day.class)
    @CollectionTable(name = "todo_days", joinColumns = @JoinColumn(name = "todo_id"))
    @Enumerated(EnumType.STRING)
    private List<Day> expectedDays;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Builder
    private Todo(Long userId, String title, String content, LocalDate startDate,
                 LocalDate endDate, Integer progress, List<Day> expectedDays, Boolean isCompleted) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.expectedDays = expectedDays;
        this.isCompleted = isCompleted;
    }

    public static Todo createTodo(User user, TodoRequest todoRequest) {
        return Todo.builder()
                .userId(user.getUserId())
                .title(todoRequest.title())
                .content(todoRequest.content())
                .startDate(todoRequest.startDate())
                .endDate(todoRequest.endDate())
                .progress(0)
                .expectedDays(todoRequest.expectedDays())
                .isCompleted(false)
                .build();
    }

    public void updateTodo(UpdateTodoRequest updateTodoRequest) {
        this.title = updateTodoRequest.title();
        this.content = updateTodoRequest.content();
        this.endDate = this.endDate.plusDays(updateTodoRequest.addDays());
    }

    public void updateProgress(int progress) {
        this.progress = progress;
    }

    public void completeTodo() {
        this.isCompleted = true;
    }
}