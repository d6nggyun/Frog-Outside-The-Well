package com._oormthon.seasonthon.domain.todo.service;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.enums.TodoType;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoQueryService {

    private final TodoRepository todoRepository;

    public Todo getTodoById(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> {
                    log.warn("[ToDo 조회 실패] 존재하지 않는 ToDo Id: {}", todoId);
                    return new CustomException(ErrorCode.TODO_NOT_FOUND);
                });
    }

    public void validateTodoOwnership(Long userId, Long todoId) {
        if (!todoRepository.existsByIdAndUserId(todoId, userId)) {
            log.warn("[ToDo 수정 실패] ToDo Id: {}, User Id: {} - 권한 없음", todoId, userId);
            throw new CustomException(ErrorCode.TODO_ACCESS_DENIED);
        }
    }

    public List<Todo> getTodosByUserIdAndTodoTypeAndMonth(Long userId, TodoType todoType, LocalDate startDate, LocalDate endDate) {
        return todoRepository.findAllTodoByTodoTypeOverlappingPeriod(userId, todoType, startDate, endDate);
    }

    public List<Todo> getTodosByUserIdAndMonth(Long userId, LocalDate startDate, LocalDate endDate) {
        return todoRepository.findAllTodoOverlappingPeriod(userId, startDate, endDate);
    }
}
