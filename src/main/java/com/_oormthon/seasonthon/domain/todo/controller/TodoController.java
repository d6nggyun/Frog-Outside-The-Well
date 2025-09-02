package com._oormthon.seasonthon.domain.todo.controller;

import com._oormthon.seasonthon.domain.member.entity.User;
import com._oormthon.seasonthon.domain.todo.dto.req.TodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateStepRequest;
import com._oormthon.seasonthon.domain.todo.dto.req.UpdateTodoRequest;
import com._oormthon.seasonthon.domain.todo.dto.res.StepResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoResponse;
import com._oormthon.seasonthon.domain.todo.dto.res.TodoStepResponse;
import com._oormthon.seasonthon.domain.todo.service.TaskPlannerService;
import com._oormthon.seasonthon.domain.todo.service.TodoService;
import com._oormthon.seasonthon.global.response.PageResponse;
import jakarta.validation.Valid;
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

    // ToDo 조회
    @GetMapping
    public ResponseEntity<PageResponse<TodoResponse>> findTodos(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findTodos(user));
    }

    // 스텝 목록 조회
    @GetMapping("/{todoId}/steps")
    public ResponseEntity<TodoStepResponse> getTodoSteps(@PathVariable Long todoId) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.getTodoSteps(todoId));
    }

    // ToDo 추가
    @PostMapping
    public ResponseEntity<TodoResponse> addTodo(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody TodoRequest todoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED ).body(todoService.addTodo(user, todoRequest));
    }

    // ToDo 삭제
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(@AuthenticationPrincipal User user,
                                           @PathVariable Long todoId) {
        todoService.deleteTodo(user, todoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Step 계획 생성
//    @GetMapping
//    public ResponseEntity<Object> generatePlan() {
//        return ResponseEntity.status(HttpStatus.OK).body(taskPlannerService.generatePlan());
//    }

    // ToDo 목표 재설정
    @PutMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(@AuthenticationPrincipal User user,
                                                   @PathVariable Long todoId,
                                                   @Valid @RequestBody UpdateTodoRequest updateTodoRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.updateTodo(user, todoId, updateTodoRequest));
    }

    // 캘린더 해당 달 조회
    @GetMapping("/calendar")
    public ResponseEntity<Object> findTodoCalendar() {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.findTodoCalendar());
    }

    // Step 완료
    @PutMapping("/steps/{stepId}/complete")
    public ResponseEntity<List<StepResponse>> completeStep(@PathVariable Long stepId) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.completeStep(stepId));
    }

    // Step 수정
    @PutMapping("/steps/{stepId}")
    public ResponseEntity<List<StepResponse>> updateStep(@PathVariable Long stepId,
                                                         @Valid @RequestBody UpdateStepRequest updateStepRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.updateStep(stepId, updateStepRequest));
    }

    // Step 삭제
    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<List<StepResponse>> deleteStep(@PathVariable Long stepId) {
        return ResponseEntity.status(HttpStatus.OK).body(todoService.deleteStep(stepId));
    }
}

