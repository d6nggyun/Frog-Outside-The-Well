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

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "todo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private Long progress;

    @ElementCollection(targetClass = Day.class)
    @CollectionTable(name = "todo_days", joinColumns = @JoinColumn(name = "todo_id"))
    @Enumerated(EnumType.STRING)
    private List<Day> expectedDays;

    @Builder
    private Todo(Long userId, String title, String content, LocalDate startDate,
                 LocalDate endDate, Long progress, List<Day> expectedDays) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.expectedDays = expectedDays;
    }

    public static Todo createTodo(User user, TodoRequest todoRequest) {
        return Todo.builder()
                .userId(user.getUserId())
                .title(todoRequest.title())
                .content(todoRequest.content())
                .startDate(todoRequest.startDate())
                .endDate(todoRequest.endDate())
                .progress(0L)
                .expectedDays(todoRequest.expectedDays())
                .build();
    }

    public void updateTodo(UpdateTodoRequest updateTodoRequest) {
        this.title = updateTodoRequest.title();
        this.content = updateTodoRequest.content();
        this.endDate = LocalDate.now().plusDays(updateTodoRequest.addDays());
    }
}