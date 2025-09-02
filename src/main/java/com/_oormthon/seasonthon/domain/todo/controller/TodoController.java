package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.member.domain.Member;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.service.TaskPlannerService;
import com._oormthon.seasonthon.domain.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todos")
public class TodoController implements TodoApiSpecification{

    private final TodoService todoService;
    private final TaskPlannerService taskPlannerService;

    // 업무 조회
    @GetMapping
    public ResponseEntity<List<TodoResponse>> findTodos(@AuthenticationPrincipal Member member) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findTodos(member));
    }

    // 스텝 목록 조회
    @GetMapping
    public ResponseEntity<> getTodoSteps() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.getTodoSteps());
    }

    // 업무 추가
    @PostMapping
    public ResponseEntity<> addTodo() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.addTodo());
    }

    // 할 일 나누기
    @GetMapping
    public ResponseEntity<> divideTodoStep() {
        return ResponseEntity.status(HttpStatus.OK).body(taskPlannerService.generatePlan());
    }

    // 업무 수정
    @PutMapping
    public ResponseEntity<> updateTodo() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.updateTodo());
    }

    // 감정 기록
    @PostMapping
    public ResponseEntity<> addEmotion() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.addEmotion());
    }

    // 캘린더 해당 달 조회
    @GetMapping
    public ResponseEntity<> findTodoCalendar() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findTodoCalendar());
    }
}
