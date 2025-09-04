package com._oormthon.seasonthon.domain.todo.service;

import com._oormthon.seasonthon.domain.todo.domain.Todo;
import com._oormthon.seasonthon.domain.todo.repository.TodoRepository;
import com._oormthon.seasonthon.global.exception.CustomException;
import com._oormthon.seasonthon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public void validateUserInTodo(Long userId, Long todoId) {
        if (!todoRepository.existsByIdAndUserId(todoId, userId)) {
            log.warn("[ToDo 수정 실패] ToDo Id: {}, User Id: {} - 권한 없음", todoId, userId);
            throw new CustomException(ErrorCode.TODO_ACCESS_DENIED);
        }
    }
}
