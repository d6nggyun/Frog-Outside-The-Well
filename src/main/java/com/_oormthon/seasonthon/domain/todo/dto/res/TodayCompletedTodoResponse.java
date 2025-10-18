package com._oormthon.seasonthon.domain.todo.dto.res;

import java.time.Duration;

public record TodayCompletedTodoResponse(

        Long todoId,

        String todoTitle,

        Duration processTime,

        Double ratio

) {
    public static TodayCompletedTodoResponse of(Long todoId, String todoTitle, Long totalDuration, Double ratio) {
        Duration processTime = Duration.ofSeconds(totalDuration == null ? 0L : totalDuration);
        return new TodayCompletedTodoResponse(todoId, todoTitle, processTime, ratio);
    }
}
