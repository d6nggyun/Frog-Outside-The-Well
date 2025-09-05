package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.step.service.TaskPlannerService;
import com._oormthon.seasonthon.domain.todo.service.TodoService;
import com._oormthon.seasonthon.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todos")
public class TodoController implements TodoApiSpecification{

    private final TodoService todoService;
    private final TaskPlannerService taskPlannerService;

    // ToDo 조회
    @GetMapping
    public ResponseEntity<PageResponse<TodoResponse>> findTodos(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findTodos(user));
    }

    // ToDo 추가
    @PostMapping
    public ResponseEntity<TodoResponse> addTodo(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody TodoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED ).body(todoService.addTodo(user, request));
    }

    // ToDo 삭제
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@AuthenticationPrincipal User user,
                                           @PathVariable Long todoId) {
        todoService.deleteTodo(user, todoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ToDo 완료
    @PutMapping("/{todoId}/complete")
    public ResponseEntity<TodoResponse> completeTodo(@AuthenticationPrincipal User user,
                                                     @PathVariable Long todoId) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.completeTodo(user, todoId));
    }

    // ToDo 목표 재설정
    @PutMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(@AuthenticationPrincipal User user,
                                                   @PathVariable Long todoId,
                                                   @Valid @RequestBody UpdateTodoRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.updateTodo(user, todoId, request));
    }
}

