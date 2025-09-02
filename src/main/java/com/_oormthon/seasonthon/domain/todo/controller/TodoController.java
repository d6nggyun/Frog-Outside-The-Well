package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.member.domain.Member;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.service.TaskPlannerService;
import com._oormthon.seasonthon.domain.todo.service.TodoService;
import com._oormthon.seasonthon.global.response.PageResponse;
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
    public ResponseEntity<PageResponse<TodoResponse>> findTodos(@AuthenticationPrincipal Member member) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findTodos(member));
    }

    // 스텝 목록 조회
    @GetMapping("/{todoId}/steps")
    public ResponseEntity<TodoStepResponse> getTodoSteps(@PathVariable Long todoId) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.getTodoSteps(todoId));
    }

    // ToDo 추가
    @PostMapping
    public ResponseEntity<Object> addTodo() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.addTodo());
    }

    // Step 계획 생성
    @GetMapping
    public ResponseEntity<Object> generatePlan() {
        return ResponseEntity.status(HttpStatus.OK).body(taskPlannerService.generatePlan());
    }

    // ToDo 수정
    @PutMapping
    public ResponseEntity<Object> updateTodo() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.updateTodo());
    }

    // 감정 기록
    @PostMapping
    public ResponseEntity<Object> addEmotion() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.addEmotion());
    }

    // 캘린더 해당 달 조회
    @GetMapping
    public ResponseEntity<Object> findTodoCalendar() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findTodoCalendar());
    }
}
